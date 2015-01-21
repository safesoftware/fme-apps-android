/*============================================================================= 
 
   Name     : ServerUtilities.java
 
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

import fme.common.FMEApplication;
import fme.common.NetworkUtil;
import fme.common.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import java.io.IOException;
import java.util.Random;
import org.json.JSONObject;

/**
 * Helper class used to communicate with the demo server.
 */
public final class ServerUtilities {

   /**
    * The shared preference key where the GCM registration id is stored.
    */
   public static final String REGISTRATION_ID = "registration_id";
   private static final String FNS_OP = "fns_op";
	private static final String URI_POST_TOPIC = "/fmerest/v2/notifications/topics/";
	private static final String SUBSCRIBE   = "subscribe";
	private static final String UNSUBSCRIBE = "unsubscribe";
	private static final String PARAMATERES = "/message/map?detail=low";
   static final String HTTP = "http://";

	/**
	 * Google API project id registered to use GCM.
	 */
	static final String SENDER_ID = "1046114377099";
	
    //This is unique to the project, needs to be used server-side as well.
	static final String API_KEY = "AIzaSyDu7skAiWbu8iNDaFU7lEaaMZytRzyNR4E";

	/**
	 * Intent used to display a message in the screen.
	 */
	static final String DISPLAY_MESSAGE_ACTION = "com.google.android.gcm.demo.app.DISPLAY_MESSAGE";

	private static final int MAX_ATTEMPTS = 2;
	private static final int BACKOFF_MILLI_SECONDS = 2000;
	private static final Random random = new Random();

	/**
	 * Register this account/device pair within the server.
	 * 
	 * @return whether the registration succeeded or not.
	 */
	static boolean notifyServerOfSubscriptions(final Context context, boolean register) {
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		
		boolean allSucceeded = true;
		
		int numSelected = prefs.getInt("topics_size", 0);
		if (numSelected > 0) {
			for (int i = 0; i < numSelected; i++) 
			{
					String topicSelected = prefs.getString("topics_"+ i, null);
					
					if(null != topicSelected)
					{
						allSucceeded &= provideRegistrationIdToServer(context, topicSelected, register);	
					}
			}
		}
		
		return allSucceeded;
	}

	public static boolean provideRegistrationIdToServer(final Context context, String topic, boolean register) {
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		final String regId = prefs.getString(REGISTRATION_ID, "");
		
		String host = prefs.getString(
				context.getResources().getString(R.string.host), null);
		
		String serverUrl = host + URI_POST_TOPIC + topic + PARAMATERES;
		long backoff = BACKOFF_MILLI_SECONDS + random.nextInt(1000);
		// Once GCM returns a registration id, we need to register it in the
		// demo server. As the server might be down, we will retry it a couple
		// times.
		
		String errorMessage = null;
		
		for (int i = 1; i <= MAX_ATTEMPTS; i++) {
			Log.d("Alerts", "Attempt #" + i + " to register");
			try {

				JSONObject body = new JSONObject();
				
				body.put("fns_type", "android");
				
				if(register)
				{
					body.put(FNS_OP, SUBSCRIBE);
				}
				else
				{
					body.put(FNS_OP, UNSUBSCRIBE);
				}
				body.put("fns_version", "1.0");
				body.put("gcm_id", regId);
				
				Location mostRecentLocation = FMEApplication.getSuperInstance().getLastKnownLocation();

				if(mostRecentLocation != null)
				{
					Time now = new Time();
					now.setToNow();
					
					String timeNow = now.format3339(false);

					String pointLocation = String.format("POINT (%f %f)",
							mostRecentLocation.getLongitude(), mostRecentLocation.getLatitude());
					body.put("fns_wkt_geom", pointLocation);
					body.put("fns_sent", timeNow);
				}
				
				String bodyContent = body.toString();
				
				String username = prefs.getString(
						context.getResources().getString(R.string.username), null);
				
				String password = prefs.getString(
						context.getResources().getString(R.string.password), null);
				
				NetworkUtil.post(serverUrl, bodyContent, username, password);

				return true;
			} 
			catch (IOException e) {
				// Here we are simplifying and retrying on any error; in a real
				// application, it should retry only on unrecoverable errors
				// (like HTTP error code 503).
				Log.e("Alerts", "Failed to register on attempt " + i, e);
				if (i == MAX_ATTEMPTS) {
					break;
				}
				try {
					Log.d("Alerts", "Sleeping for " + backoff + " ms before retry");
					Thread.sleep(backoff);
				} catch (InterruptedException e1) {
					// Activity finished before we complete - exit.
					Log.d("Alerts", "Thread interrupted: abort remaining retries!");
					Thread.currentThread().interrupt();
					break;
				}
				// increase backoff exponentially
				backoff *= 2;
				errorMessage = e.getMessage();
			} catch (Exception e) {
				e.printStackTrace();
				
				errorMessage = e.getMessage();
			}
		}
		 if(errorMessage != null)
		 {
			 String failedTo = "Failed to " + (register ? SUBSCRIBE : UNSUBSCRIBE) + " from topic " + topic + ": " + errorMessage;
		 }
		return false;
	}
}