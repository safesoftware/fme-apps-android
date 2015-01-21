/*============================================================================= 
 
   Name     : FMEApplication.java
 
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

import java.net.URLEncoder;
import java.util.Set;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

import fme.common.R;
import fme.common.PostLocationTask;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

public abstract class FMEApplication<TaskType extends PostLocationTask> extends
		Application implements GooglePlayServicesClient.ConnectionCallbacks,
      GooglePlayServicesClient.OnConnectionFailedListener, LocationListener {
   
   public static final String DEFAULT_LAT = "49.138111";
   public static final String DEFAULT_LONG = "-122.857649";
   
   private static final long DEFAULT_TIME_INTERVAL_MINIMUM = 1000*60; //milli-sec
   private static final long DEFAULT_TIME_INTERVAL_FASTEST = 1000*5;//milli-sec
   
   SharedPreferences prefs;
	protected boolean autoUpdate;
	
	static private FMEApplication<?> singleton;
	
	private TopicsListener topicsListener;
	private String reportKeyword;
	private String gcmRegIDString;
	private SubscriptionCanceller subscriptionCanceller;
	
	LocationClient locationClient;
	
	private boolean queueSend;
	private Toast toast;
	
	private boolean servicesConnected(){
	   int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	   if (ConnectionResult.SUCCESS == resultCode){
	      Log.i("Location updated", "Google Play services available");
	      return true;
	   } else {
	      return false;
	      }
	   }
	   
	   
	public void onConnected(Bundle arg0) {
	   locationClient.requestLocationUpdates(getLocationRequest(), this);
	   if(queueSend) {
	      queueSend = false;
	      if(!sendMessage())
	         Toast.makeText(this, R.string.insufficient_info_details, Toast.LENGTH_LONG).show();
	   }
	}
	
	public void onDisconnected() {
	   Log.i("Location Service", "client disconnected");
	   
	}
	
	public void onConnectionFailed(ConnectionResult result) {
	   if(result.hasResolution())
         try {
            result.getResolution().send(result.getErrorCode());
         } catch (Exception e) {
            e.printStackTrace();
         }
	   Log.i("Location Services", "Connection Failed. Error code: "+result.getErrorCode());
	}
	
	public void updateLocationRequest(){
	   if(locationClient.isConnected()){
	      locationClient.requestLocationUpdates(getLocationRequest(), this);
	   } else if(!locationClient.isConnected() && !locationClient.isConnecting())
	      locationClient.connect();
	}
	
	public void onLocationChanged(Location arg0) {
	   if(autoUpdate)
	      sendMessage();
	}
	
	private LocationRequest getLocationRequest() {
	   boolean highPrecisionOn = prefs.getBoolean(
            getResources().getString(R.string.high_precision), false);
	   autoUpdate = prefs.getBoolean(
            getResources().getString(R.string.autoReportLocation), false);
         int timeValueEntered = prefs.getInt(
               getResources().getString(R.string.Time_interval_label), 60000);
         int distanceFilter = prefs
               .getInt(getResources().getString(
                     R.string.Distance_filter_label), 500);

	   LocationRequest locationRequest = LocationRequest.create();
	   
	   if(highPrecisionOn)
	      locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	   else
	      locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
	   if(autoUpdate){
	      locationRequest.setInterval(timeValueEntered*1000);
         locationRequest.setFastestInterval(timeValueEntered*1000);
         locationRequest.setSmallestDisplacement(distanceFilter);
	   } else {
	      locationRequest.setInterval(DEFAULT_TIME_INTERVAL_MINIMUM);
         locationRequest.setFastestInterval(DEFAULT_TIME_INTERVAL_FASTEST);
	   }
      return locationRequest;
	}
	
	public TopicsListener getTopicsListener() {
		return topicsListener;
	}

	public void setTopicsListener(TopicsListener topicsListener) {
		this.topicsListener = topicsListener;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		queueSend = false;
		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		
		if(servicesConnected()){
		locationClient = new LocationClient(this, this, this);
		locationClient.connect();
		}
	}
	
	/**
	 * Retrieve the last known location if available otherwise null.
	 */
	public Location getLastKnownLocation() {
	   Location location = null;
	   if(locationClient.isConnected())
	      location = locationClient.getLastLocation();
	   return location;
	}
	
	public void setAutoUpdate(boolean autoUpdate) {
		this.autoUpdate = autoUpdate;
		   updateLocationRequest();
	}

	public void handleUpdatedTopicsList(Set<String> oldTopics) {
		if (topicsListener != null) {
			topicsListener.handleUpdatedTopicsList(oldTopics);
		}
	}
	
	public void handleCancelSubscriptions(SettingsMenu settingsMenu)
	{
		if(subscriptionCanceller != null)
		{
			subscriptionCanceller.cancelAllSubscriptions(settingsMenu);
		}
	}
	
	public boolean sendMessage() {
	   
	   if(!locationClient.isConnected()){
	      queueSend = true;
	      if(!locationClient.isConnecting())
	         locationClient.connect();
	      return true;
	   }
	   
		String serverURL = prefs.getString(
				this.getResources().getString(R.string.host), null);
		String username = prefs.getString(
				this.getResources().getString(R.string.username), null);
		String password = prefs.getString(
				this.getResources().getString(R.string.password), null);
		
		String firstName = prefs.getString(getResources().getString(R.string.firstNameLabel),null);
		String lastName = prefs.getString(getResources().getString(R.string.lastNameLabel),null);
		String email = prefs.getString(getResources().getString(R.string.emailLabel),null);
		String subject = prefs.getString(getResources().getString(R.string.subjectLabel),null);
		String webAddress = prefs.getString(getResources().getString(R.string.webAddressLabel),null);
		String details = prefs.getString(getResources().getString(R.string.detailsLabel),null);
		
		int numTopics = prefs.getInt(FMETopics.TOPIC_SIZE_REPORT, 0);

		if (null != serverURL && serverURL.length() > 0 && null != username
				&& username.length() > 0 && null != password
				&& password.length() > 0 && numTopics > 0) {
			for (int currTopicId = 0; currTopicId < numTopics; currTopicId++) {

				String topic = prefs.getString(FMETopics.TOPIC_REPORT + currTopicId, ""); // Current
																				// topic
																				// to
																				// send
																				// to
				if (topic.length() > 0) {
					String topicSafe = URLEncoder.encode(topic);
					String fullURLFormat = "%s/fmerest/v2/notifications/topics/%s/message/map";
					String fullURL = String.format(fullURLFormat, serverURL,
							topicSafe);
					Time now = new Time();
					now.setToNow();

					Location mostRecentLocation = locationClient.getLastLocation();
					Long lastSentTime = now.toMillis(true);
					String timeNow = now.format3339(false);
					
					String requestBody = String.format("\"fns_op\" : \""
							+ reportKeyword + "\",\n"
							+ "\"fns_version\" : \"1.0\","
							+ "\"fns_type\" : \"android\",\n"
							+ "\"fns_sent\" : \"" + timeNow + "\",\n"
							+ "\"fns_wkt_geom\" : \"POINT (%f %f)\"",
							(mostRecentLocation == null ? Double.parseDouble(DEFAULT_LONG)
									: mostRecentLocation.getLongitude()),
							(mostRecentLocation == null ? Double.parseDouble(DEFAULT_LAT)
									: mostRecentLocation.getLatitude()));

					if(firstName != null)
					{
						requestBody += ",\n\"msg_first_name\" : \"" + firstName + "\"";
					}
					if(lastName != null)
					{
						requestBody += ",\n\"msg_last_name\" : \"" + lastName + "\"";
					}
					if(email != null)
					{
						requestBody += ",\n\"msg_from\" : \"" + email + "\"";
					}
					if(subject != null)
					{
						requestBody += ",\n\"msg_subject\" : \"" + subject + "\"";
					}
					if(webAddress != null)
					{
						requestBody += ",\n\"msg_url\" : \"" + webAddress + "\"";
					}
					if(details != null)
					{
						requestBody += ",\n\"msg_content\" : \"" + details + "\",\n"
								+ "\"msg_content_type\": \"text/plain\"";
					}
					
					
					if (null != FMEApplication.getSuperInstance().getGCMRegID()) {
						requestBody = requestBody + ",\n\"gcm_id\" : \""
								+ FMEApplication.getSuperInstance().getGCMRegID() + "\"";
					}
						TaskType task = createSpecificTaskType();
						// The default is Safe Software Inc.
						String lat = DEFAULT_LAT;
						String lng = DEFAULT_LONG;
						try {
						lat = Double.toString(mostRecentLocation.getLatitude());
						lng = Double.toString(mostRecentLocation.getLongitude());
						} catch(NullPointerException e){
						   makeToast(this,"Your location could not be determined." +
				               " Please ensure that locations services are on." +
				               " A report with the default location will be sent.");
						}
						
						task.execute(new String[] { "results", fullURL, username,
								password, requestBody,topic,firstName,lastName,email,subject,webAddress,details,lat,lng,Long.toString(lastSentTime)});

				}

			}
			return true;
		} else {
			return false; // When we're in the context of the main page, we'll
							// display an error
		}
	}

	private void makeToast(Context context, String msg){
	   if(toast==null) {
	      toast = Toast.makeText(context,"", Toast.LENGTH_LONG);
	   }
	   toast.setText(msg);
	   toast.show();
	}
	
	/**
	 * Delete all user data from database. Should call this during logout.
	 * 
	 * @return Number of Delete Items i.e.{alerts, reports, unsent reports}
	 * or null if nothing was deleted.
	 */
	public abstract int[] deleteUserData();
	
   public abstract TaskType createSpecificTaskType();

	public void setReportKeyword(String reportKeyword) {
		this.reportKeyword = reportKeyword;
	}

	public String getGCMRegID() {
		return gcmRegIDString;
	}

	public void setGCMRegID(String gcmRegIDString) {
		this.gcmRegIDString = gcmRegIDString;
	}

	public SubscriptionCanceller getSubscriptionCanceller() {
		return subscriptionCanceller;
	}

	public void setSubscriptionCanceller(
			SubscriptionCanceller subscriptionCanceller) {
		this.subscriptionCanceller = subscriptionCanceller;
	}

	public static FMEApplication<?> getSuperInstance() {
		return singleton;
	}

	public static void setSuperInstance(FMEApplication<?> singleton) {
		FMEApplication.singleton = singleton;
	}
	
	public boolean isConnectedToNetwork() {
	    ConnectivityManager connMgr = (ConnectivityManager) 
	        getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
	    if (networkInfo != null && networkInfo.isConnected())
	        return true;
	    else
	        return false;
	}
	
   /**
    * Checks if GPS is enabled by user. If not a dialog informing the user is displayed
    * @return
    */
   public boolean isGPSEnabled(Context context){
      LocationManager locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
      boolean isGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
      boolean isNetwork = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
      if(!isGPS || !isNetwork) {
         String message = "Please enable GPS to get accurate location while outside.";
         if(!isNetwork)
            message = "Please enable Network Location for accurate location reading.";
         if(!isNetwork && !isGPS)
            message = "Please enable Location Services.";
      new AlertDialog.Builder(context).setMessage(message)
      .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
         
         public void onClick(DialogInterface dialog, int which) {
            try{
               startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
               .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
           } catch(Exception e) {
               e.printStackTrace(); //If exception thrown, do nothing
            }
         }
      })
      .setNeutralButton("Dismiss", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
            // Do nothing
         }
      }).create().show();
      return true;
      } else 
         return false;
   }
}