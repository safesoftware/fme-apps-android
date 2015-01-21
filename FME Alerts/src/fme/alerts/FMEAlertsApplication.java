/*============================================================================= 
 
   Name     : FMEAlertsApplication.java
 
   System   : FME Alerts
 
   Language : Java
 
   Purpose  : TBD
 
         Copyright (c) 2013 - 2014, Safe Software Inc. All rights reserved. 
 
   Redistribution and use of this sample code in source and binary forms, with  
   or without modification, are permitted provided that the following  
   conditions are met: 
   * Redistributions of source code must retain the above copyright notice,  
     this list of conditions and the following disclaimer. 
   * Redistributions in binary form must reproduce the above copyright notice,  
     this list of conditions and the following disclaimer in the documentation  
     and/or other materials provided with the distribution. 
 
   THIS SAMPLE CODE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS  
   "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED  
   TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR  
   PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR  
   CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,  
   EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,  
   PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;  
   OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,  
   WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR  
   OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SAMPLE CODE, EVEN IF  
   ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 
=============================================================================*/ 

package fme.alerts;

import java.util.HashSet;
import java.util.Set;

import fme.common.FMEApplication;
import fme.common.SettingsMenu;
import fme.common.TopicsListener;
import fme.common.SubscriptionCanceller;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

public class FMEAlertsApplication extends
		FMEApplication<SendLocationAndUpdateMapTask> {

   private MainActivity mainActivityHandle;
	private static FMEAlertsApplication singleton;
	private Set<AsyncTask<Void, Void, Void>> mRegisterTasks;

	static public FMEAlertsApplication getInstance() {
		return singleton;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		singleton = this;
		super.setSuperInstance(this);
		super.setTopicsListener(new HandleTopicsUpdateListener(
				getApplicationContext()));
		super.setSubscriptionCanceller(new AlertSubscriptionCanceller(
				getApplicationContext()));
		super.setReportKeyword("update");

		mRegisterTasks = new HashSet<AsyncTask<Void, Void, Void>>();
	}

	
	@Override
	public SendLocationAndUpdateMapTask createSpecificTaskType() {
		return new SendLocationAndUpdateMapTask();
	}

	class HandleTopicsUpdateListener implements TopicsListener {
		private Context parent;

		HandleTopicsUpdateListener(Context parent) {
			this.parent = parent;
		}

		public void handleUpdatedTopicsList(Set<String> oldTopics) {
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(parent);

			Set<String> newTopics = new HashSet<String>();

			int numSelected = prefs.getInt("topics_size", 0);
			if (numSelected > 0) {
				for (int i = 0; i < numSelected; i++) {
					String topicSelected = prefs.getString("topics_" + i, null);

					if (null != topicSelected) {
						newTopics.add(topicSelected);
					}
				}
			}

			// Dead topics are the old ones not in the new set
			Set<String> deadTopics = new HashSet<String>(oldTopics);
			deadTopics.removeAll(newTopics);

			Set<String> newbieTopics = new HashSet<String>(newTopics);
			newbieTopics.removeAll(oldTopics);

			for (String topic : deadTopics) {
				subscribeOrUnsubscribeFromTopic(topic, false);
			}

			for (String topic : newbieTopics) {
				subscribeOrUnsubscribeFromTopic(topic, true);
			}
			return;
		}
	}

	public void subscribeOrUnsubscribeFromTopic(final String topic,
			final boolean subscribe) {

		final Context context = getApplicationContext();
		AsyncTask<Void, Void, Void> currTask = new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {

				boolean registered = ServerUtilities
						.provideRegistrationIdToServer(context, topic,
								subscribe);
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				mRegisterTasks.remove(this);
			}

		};
		mRegisterTasks.add(currTask);
		currTask.execute();
	}

	class AlertSubscriptionCanceller implements SubscriptionCanceller {
		private Context parent;

		public AlertSubscriptionCanceller(Context parent) {
			this.parent = parent;
		}

		public void cancelAllSubscriptions(final SettingsMenu settingsMenu) {

			final Context context = getApplicationContext();
			AsyncTask<Void, Void, Void> currTask = new AsyncTask<Void, Void, Void>() {

				@Override
				protected Void doInBackground(Void... params) {
					boolean registered = ServerUtilities
							.notifyServerOfSubscriptions(context,
									 false);
					return null;
				}

				@Override
				protected void onPostExecute(Void result) {
					mRegisterTasks.remove(this);
					settingsMenu.logout();
				}
			};

			mRegisterTasks.add(currTask);
			currTask.execute();
		}


	}
	
	public int[] deleteUserData(){
	   int alerts = getContentResolver().delete(AlertsContentProvider.ALERTS_TABLE_URI, null, null);
	   int reports = getContentResolver().delete(AlertsContentProvider.REPORTS_TABLE_URI, null, null);
	   int unsent = getContentResolver().delete(AlertsContentProvider.UNSENT_TABLE_URI, null, null);
	   if (alerts+reports+unsent<1) return null;
	   return new int[]{alerts,reports,unsent};
	}
	
	public MainActivity getMainActivityHandle() {
      return mainActivityHandle;
   }

   public void updateMainActivityHandle(MainActivity mainActivityHandle) {
      this.mainActivityHandle = mainActivityHandle;
   }
	
}
