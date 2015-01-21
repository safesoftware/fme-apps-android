/*============================================================================= 
 
   Name     : FMETopics.java
 
   System   : FMECommon
 
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

package fme.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CheckedTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class FMETopics extends ListActivity {

	protected ProgressDialog cancelDialog;
	private static boolean isActivityRunning;

	public static final String TOPIC_SIZE_REPORT = "report_topics_size";
	public static final String TOPIC_SIZE_ALERT = "topics_size";
	public static final String TOPIC_REPORT = "report_topics_";
	public static final String TOPIC_ALERT = "topics_";
	
	private static final String TAG_NAME = "name";
	private static final String TAG_DESC = "description";

	private boolean successfullyRendered;
	private boolean isAlert;
	
	private String topics;
   private String topicSize;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		 ActionBar actionBar = getActionBar();
	    actionBar.setDisplayHomeAsUpEnabled(true);
		
	    isAlert = getIntent().getBooleanExtra("alert", true);

       if(isAlert){
          topics = TOPIC_ALERT;
          topicSize = TOPIC_SIZE_ALERT;
       } else {
          topics = TOPIC_REPORT;
          topicSize = TOPIC_SIZE_REPORT;
       }
	    
		successfullyRendered = false;
		reloadTopics();
	}

	public void errorAlertDialog(String errorMsg)
	{
	   
		if (null != cancelDialog && cancelDialog.isShowing()) {
			cancelDialog.dismiss();
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		  builder.setTitle("Error")
		  .setOnCancelListener(new DialogInterface.OnCancelListener() {

         public void onCancel(DialogInterface arg0) {
            finish();
            
         }
      })
		      .setMessage("Please check your account information and your network connection and try again later.")
		      .setCancelable(true);
		  AlertDialog ad = builder.create();
		  ad.show();  
	}
	
	private class ReloadTopicsAndUpdateTask extends FetchTopicsTask {
	
		@Override
		protected void onPostExecute(String result) {
		   
		   if (null != cancelDialog && cancelDialog.isShowing()) {
            cancelDialog.dismiss();
         }
		   
			//Check if there was a problem during the background work
			if(result.isEmpty())
			{
			   if(isActivityRunning)
				errorAlertDialog(errorStr);
				errorStr = "";
				return;
			}
			
			List<Map<String, String>> items = new ArrayList<Map<String, String>>();

			try {
				JSONArray topicList = new JSONArray(result);
				
				for (int i = 0; i < topicList.length(); i++) {
					JSONObject c = topicList.getJSONObject(i);
					// Storing each json item in variable
					String description = c.getString(TAG_DESC);
					String name = c.getString(TAG_NAME);
					Map<String, String> map = new HashMap<String, String>();
					map.put(TAG_DESC, description);
					map.put(TAG_NAME, name);
					items.add(map);
				}
			} catch (JSONException e) {
				e.printStackTrace();
				if(isActivityRunning)
				errorAlertDialog(e.toString());
			}

			ListView lv = getListView();

			SimpleAdapterWithCheckBox adapter = new SimpleAdapterWithCheckBox(getApplicationContext(),
					items, R.layout.listitem_single_check, new String[] {
				TAG_NAME, TAG_DESC }, new int[] { R.id.tv_MainText,
				R.id.tv_SubText });
			
			lv.setAdapter(adapter);
			lv.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
			lv.setItemsCanFocus(false);			
			successfullyRendered = true;
		}
	}
	

	private void reloadTopics() {
		
		
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		String serverURL = prefs.getString(
				getResources().getString(R.string.host), null);
		String username = prefs.getString(
				getResources().getString(R.string.username), null);
		String password = prefs.getString(
				getResources().getString(R.string.password), null);
		
		AsyncTask<String, Void, String> task = new ReloadTopicsAndUpdateTask();
		task = task.execute(new String[] {serverURL, username, password});
		createCancelProgressDialog(task);
	}

	private void createCancelProgressDialog(final AsyncTask<String, Void, String> task) {
		cancelDialog = new ProgressDialog(this);
		cancelDialog.setTitle("Loading Topics");
		cancelDialog.setMessage("Please wait...");
		cancelDialog.setButton(ProgressDialog.BUTTON_NEGATIVE,"Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				finish();
				task.cancel(true);
			}
		});
		cancelDialog.setCancelable(false);
		cancelDialog.show();
	}


	@Override
	protected void onStart() {
	   super.onStart();
	   isActivityRunning=true;
	}
	
	@Override
	protected void onStop() {
	   super.onStop();
	   isActivityRunning=false;
	}
	
	

	@Override
	protected void onPause() {
		super.onPause();
		ListView lv = getListView();

		if(null != lv)
		{
			Set<String> oldTopics = new HashSet<String>();

			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(this);

			int numSelected = prefs.getInt(topicSize, 0);
			if (numSelected > 0) {
				for (int i = 0; i < numSelected; i++) {
					String topicSelected = prefs.getString(topics + i, null);

					if (null != topicSelected) {
						oldTopics.add(topicSelected);
					}
				}
			}
			
			SimpleAdapterWithCheckBox adapter = (SimpleAdapterWithCheckBox) lv.getAdapter();
			if (successfullyRendered && null != adapter) {
				boolean[] checkedArray = adapter.getChecked();
				
				SharedPreferences.Editor editor = prefs.edit();
				numSelected = 0;
				for (int i = 0; i < checkedArray.length; i++) { //this is by position, not by 
					
					if (checkedArray[i]) {
						CharSequence topicName = adapter.getTopicAt(i);
						editor.putString(topics + numSelected,
								(String) topicName);
						numSelected++;
					}
				}

				editor.putInt(topicSize, numSelected);

				editor.commit();
				
				// Now we've saved the newer set, pass on the info here
				if(isAlert)
				   FMEApplication.getSuperInstance().handleUpdatedTopicsList(oldTopics);
			}
		}
	}
	
	@Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case android.R.id.home:
         return SettingsMenu.backToHome(getBaseContext());
      }
      return false;
   }
	

	private class SimpleAdapterWithCheckBox extends SimpleAdapter {

		private LayoutInflater mInflater;
		private final List<? extends Map<String, ?>> mData;
		private boolean[] mChecked;

		public SimpleAdapterWithCheckBox(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mData = data;
			mChecked = new boolean[data.size()];


			
			SharedPreferences prefs = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());

			
			
			//TODO check the sorting, may be a problem
			int currSelected = 0;
			int numSelected = prefs.getInt(topicSize, 0);
			if (numSelected > 0) {
				String topicSelected = prefs.getString(topics + 0, null);
				for (int i = 0; i < mData.size() && currSelected < numSelected; i++) {
					String currTopic = getTopicAt(i);
					int compVal = currTopic.compareTo(topicSelected);
					if (compVal >= 0) // Time to advance
					{
						if (compVal == 0) {
							getChecked()[i] = true;
						}
						currSelected++;
						topicSelected = prefs.getString(topics
								+ currSelected, "");
					}
				}
			}
		}

		public String getTopicAt(int i) {
			HashMap localMap = (HashMap) mData.get(i);
			String currTopic = localMap.get(TAG_NAME).toString();
			return currTopic;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = mInflater.inflate(R.layout.listitem_single_check,
					null);

			final int localPos = position;
			CheckedTextView main = (CheckedTextView) rowView
					.findViewById(R.id.tv_MainText);
			LinearLayout outerLayout = (LinearLayout) main.getParent();
			main.setOnClickListener(new View.OnClickListener()
			{

				public void onClick(View v) {
					CheckedTextView tt = (CheckedTextView) v.findViewById(R.id.tv_MainText);
					tt.toggle();
					getChecked()[localPos] = tt.isChecked();
					
					LinearLayout parent = (LinearLayout) tt.getParent();
					if(getChecked()[localPos])
						parent.setBackgroundColor(getResources().getColor(R.color.BlueTransparent));
					else 
						parent.setBackgroundColor(getResources().getColor(android.R.color.transparent));
				}
				
			});
			
			TextView secondary = (TextView) rowView
					.findViewById(R.id.tv_SubText);
			ImageButton topicInfo = (ImageButton) rowView.findViewById(R.id.topicMoreInfo);
			final String subText = mData.get(position).get(TAG_DESC).toString();
			topicInfo.setOnClickListener(new View.OnClickListener()
			{

				public void onClick(View v) {
					TextView tv = (TextView) v.findViewById(R.id.tv_SubText);
					Intent i = new Intent(getApplicationContext(), TopicDetails.class);
					i.putExtra(TAG_DESC, subText);
					startActivity(i);
				}
				
			});
			
			HashMap localMap = (HashMap) mData.get(position);
			String name = localMap.get(TAG_NAME).toString();
			String desc = localMap.get(TAG_DESC).toString();
			if (name != null) {
				
				main.setText(name.replace('_', ' '));
			}
			if (desc != null) {
				secondary.setText(desc);
			}

			if (getChecked()[position]) {
				main.setChecked(true);
				outerLayout.setBackgroundColor(getResources().getColor(R.color.BlueTransparent));
				
			} else {
				outerLayout.setBackgroundColor(getResources().getColor(android.R.color.transparent));
			}
			return rowView;
		}

		public boolean[] getChecked() {
			return mChecked;
		}

	}
}

