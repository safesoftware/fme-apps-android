/*============================================================================= 
 
   Name     : GCMIntentService.java
 
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

import com.google.android.gms.gcm.GoogleCloudMessaging;

import fme.alerts.AlertsContentProvider.AlertColumns;
import fme.common.FMEApplication;
import android.app.IntentService;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends IntentService {

   public GCMIntentService() {
      super("GCMIntentService");
   }
   
    @Override
    protected void onHandleIntent(Intent intent) {
       Bundle extras = intent.getExtras();
       GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
       String messageType = gcm.getMessageType(intent);
       if(!extras.isEmpty()){
          if(GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)){
             Log.i("GCMIntentService", "Received message");
             String details = intent.getStringExtra( "alert" ) ;
             String title = intent.getStringExtra( "title" ) ;
             String[] location = parseLocation(intent.getStringExtra("location"));
             addAlert(title, details, location[1],location[0]);
             generateNotification(this,title, details,location[1],location[0]);
          }
       }
       GCMBroadcastReceiver.completeWakefulIntent(intent);
    }


   
   /**
    * Issues a notification to inform the user that server has sent a message.
    */
   @SuppressWarnings("deprecation")//getNotification() deprecated in API 16, 
                                   //but it's replacement not available in API 14 & 15
   private static void generateNotification(Context context,String title, String message,String lat,String lon) {
       int icon = R.drawable.ic_launcher;
       long when = System.currentTimeMillis();
       NotificationManager notificationManager = (NotificationManager)
               context.getSystemService(Context.NOTIFICATION_SERVICE);
       // This String Array is required by AlertDetails to display information
       String[] results = new String[6];
       results[0] = "";
       results[1] = title;
       results[2] = message;
       results[3] = lat;
       results[4] = lon;
       results[5] = Long.toString(when);
       
       Intent intent = new Intent(context,AlertDetails.class)
       .putExtra(AlertDetails.KEY_RESULTS, results)
       .putExtra(AlertDetails.KEY_MODE, "alert");
       
       PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
       
       Notification.Builder builder = new Builder(context)
       .setSmallIcon(icon)
       .setContentTitle(context.getString(R.string.app_name))
       .setWhen(when)
       .setContentText(message)
       .setContentIntent(pendingIntent)
       .setAutoCancel(true);
       notificationManager.notify(0, builder.getNotification());
   }
    
   /**
    * Parse location. Return Default location if error occurs
    * @return Latitude in index 0 and Longitude in index 1
    */
   private String[] parseLocation(String location) {
      String[] tokens = new String[2];
      try{
         String locationOnly = location.replace("POINT", "")
               .replace("(", "").replace(")", "").replace(",", " ").trim();
         tokens = locationOnly.split("[ ]+");
         //this is done to ensure both tokens are doubles
         Double.parseDouble(tokens[0]);
         Double.parseDouble(tokens[1]);
         // We will use the String value though
      } catch (Exception e) {
         tokens[0] = FMEApplication.DEFAULT_LAT;
         tokens[1] = FMEApplication.DEFAULT_LONG;
      }
      return tokens;
   }
   /**
    * Add an alert to the database
    * @param title
    * @param alert
    * @param lat
    * @param lon
    */
   void addAlert(String title, String alert, String lat,String lon) {
      if (title == null) {
         title = "Untitled";
      }
      ContentValues cv = new ContentValues();
      cv.put(AlertColumns.TITLE, title);
      cv.put(AlertColumns.DESCRIPTION, alert);
      cv.put(AlertColumns.CREATED_DATE, Long.toString(System.currentTimeMillis()));
      cv.put(AlertColumns.LATITUDE, lat);
      cv.put(AlertColumns.LONGITUDE, lon);
      getContentResolver().insert(AlertsContentProvider.ALERTS_TABLE_URI, cv);
   }
}
