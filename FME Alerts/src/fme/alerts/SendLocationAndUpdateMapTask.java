/*============================================================================= 
 
   Name     : SendLocationAndUpdateMapTask.java
 
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

import java.text.DateFormat;

import com.google.android.gms.internal.ee;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Point;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import fme.alerts.AlertsContentProvider.ReporterColumns;
import fme.alerts.AlertsContentProvider.UnsentColumns;
import fme.common.PostLocationTask;
import fme.common.SettingsMenu;

public class SendLocationAndUpdateMapTask extends PostLocationTask {
   GoogleMap mMap = null;
   MainActivity mainActivity = null;
   FMEAlertsApplication main = null;
   Toast toast;

   public SendLocationAndUpdateMapTask() {
      super();
   }

   @Override
   protected void onPostExecute(String[] results) {
      String result = results[0];
      
      main = FMEAlertsApplication.getInstance();
      if(main!=null)
         mainActivity = main.getMainActivityHandle();
      if(mainActivity!=null)
         mMap = mainActivity.getMap();
      if (mMap != null) {
         long currentTime = Long.parseLong(results[14]);
         String timestamp = DateFormat.getDateTimeInstance()
               .format(currentTime);
         double endLat = Double.parseDouble(results[12]);
         double endLng = Double.parseDouble(results[13]);
         LatLng endLatLng = new LatLng(endLat, endLng);
         Projection projection = mMap.getProjection();
         int alignWithPoint = projection.toScreenLocation(endLatLng).x;
         int pointDistanceFromTop = projection.toScreenLocation(endLatLng).y;
         LatLng startLatLng = projection.fromScreenLocation(new Point(
               alignWithPoint, 0));
         MarkerOptions point = new MarkerOptions().position(startLatLng);
         if ("" == result) {
            point.title(FMEAlertsApplication.getInstance().getResources()
                  .getString(R.string.LocationReported));
            point.snippet(timestamp);
            point.icon(BitmapDescriptorFactory
                  .fromResource(R.drawable.blue_circle));

         } else {
            // Show the user what went wrong (in a toast pop-up)
            String errorMsg;
            if(main.isConnectedToNetwork())
               errorMsg = "Unable to report location to server. " + result;
            else
               errorMsg = "Unable to report location to server. Your device is not connected to the internet.";
            
            mainActivity.updateToast(errorMsg);
            
            point.title(FMEAlertsApplication.getInstance().getResources()
                  .getString(R.string.UnableToReport));
            point.snippet(result);
            point.icon(BitmapDescriptorFactory
                  .fromResource(R.drawable.red_circle));
         }
         WindowManager wm = (WindowManager) mainActivity
               .getSystemService(Context.WINDOW_SERVICE);
         Display display = wm.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         int height = size.y;
         int deltaHeight = (int) ((pointDistanceFromTop > 1.5 * height ? 1.5 * height
               : pointDistanceFromTop) - height / 2);

         Marker marker = mMap.addMarker(point);
         animateMarker(marker, endLatLng, point, results, deltaHeight);
      } else {
         insertIntoDatabase(results);
      }
   }

   private void insertIntoDatabase(String[] results) {
      ContentValues values = new ContentValues();
      values.put(ReporterColumns.TITLE, results[5]);
      values.put(ReporterColumns.FIRST_NAME, results[6]);
      values.put(ReporterColumns.LAST_NAME, results[7]);
      values.put(ReporterColumns.EMAIL, results[8]);
      values.put(ReporterColumns.SUBJECT, results[9]);
      values.put(ReporterColumns.WEB_ADDRESS, results[10]);
      values.put(ReporterColumns.DETAILS, results[11]);
      values.put(ReporterColumns.LATITUDE, Double.valueOf(results[12]));
      values.put(ReporterColumns.LONGITUDE, Double.valueOf(results[13]));
      values.put(ReporterColumns.CREATED_DATE, results[14]);
      if ("" == results[0]) {
         FMEAlertsApplication.getInstance().getContentResolver()
               .insert(AlertsContentProvider.REPORTS_TABLE_URI, values);
      } else if (PreferenceManager.getDefaultSharedPreferences(main)
            .getBoolean(SettingsMenu.STORE_UNSENT_REPORTS, true)) {
         values.put(UnsentColumns.POST_URL, results[1]);
         values.put(UnsentColumns.USERNAME, results[2]);
         values.put(UnsentColumns.POST_BODY, results[4]);
         FMEAlertsApplication.getInstance().getContentResolver()
               .insert(AlertsContentProvider.UNSENT_TABLE_URI, values);
      }
   }

   /**
    * Make the marker fly towards toPosition on map. Sets the position of the
    * MarkerOption point to toPosition.
    * 
    * @param marker
    * @param toPosition
    * @param point
    * @param deltaHeight
    */
   public void animateMarker(final Marker marker, final LatLng toPosition,
         final MarkerOptions point, final String[] results, int deltaHeight) {
      final Handler handler = new Handler();
      final long start = SystemClock.uptimeMillis();
      Projection proj = mMap.getProjection();
      Point startPoint = proj.toScreenLocation(marker.getPosition());

      final LatLng startLatLng = proj.fromScreenLocation(startPoint);
      final long duration = 500 + (deltaHeight > 0 ? deltaHeight : 0);
      final Interpolator interpolator = new LinearInterpolator();
      handler.post(new Runnable() {
         public void run() {
            long elapsed = SystemClock.uptimeMillis() - start;
            float t = interpolator.getInterpolation((float) elapsed / duration);
            double lng = t * toPosition.longitude + (1 - t)
                  * startLatLng.longitude;
            double lat = t * toPosition.latitude + (1 - t)
                  * startLatLng.latitude;
            marker.setPosition(new LatLng(lat, lng));
            if (t < 1.0) {
               // Post again 16ms later.
               handler.postDelayed(this, 16);
            } else {
               point.position(toPosition);
               marker.setPosition(toPosition);
               marker.remove();
               insertIntoDatabase(results);
            }
         }
      });
      if (PreferenceManager.getDefaultSharedPreferences(mainActivity)
            .getBoolean("automovecamera", false))
         mMap.animateCamera(CameraUpdateFactory.newLatLng(toPosition));
   }

}
