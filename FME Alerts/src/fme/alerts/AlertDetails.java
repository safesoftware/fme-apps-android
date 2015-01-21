/*============================================================================= 
 
   Name     : AlertDetails.java
 
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
import java.util.Date;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

public class AlertDetails extends Activity {

   public static final String KEY_MODE = "mode";
   public static final String KEY_RESULTS = "results";
   
   private GoogleMap map;
   private String mode;
   private LatLng latLng;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      getActionBar().setDisplayHomeAsUpEnabled(true);

      setContentView(R.layout.alert_details);
      setUpAlertInfo();

      setUpMapIfNeeded();
   }

   private void setUpMap() {

      BitmapDescriptor icon = BitmapDescriptorFactory
            .fromResource(R.drawable.grn_circle);
      map.addMarker(new MarkerOptions().position(latLng).title(mode)
            .icon(icon));
      map.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16));
      map.setMyLocationEnabled(true);

   }

   private void setUpAlertInfo() {
      TextView date = (TextView) findViewById(R.id.detailDate);
      TextView title = (TextView) findViewById(R.id.detailHeading);
      TextView description = (TextView) findViewById(R.id.AlertDetails);
      Intent sender = getIntent();
      String[] results = sender.getExtras().getStringArray(KEY_RESULTS);
      title.setText(deNull(results[1]).replace('_', ' '));

      if (sender.getExtras().getString(KEY_MODE, "alert")
            .equalsIgnoreCase("alert")) {
         Long now;
         try {
            now = Long.parseLong(results[5]);
         } catch (NumberFormatException e) {
            now = (long) Double.parseDouble(results[5]);
         }
         Date timeCreated = new Date(now);
         date.setText(DateFormat.getDateTimeInstance().format(timeCreated));
         Double lat = Double.parseDouble(results[3]);
         Double lng = Double.parseDouble(results[4]);
         latLng = new LatLng(lat, lng);
         description.setText(results[2]);

      } else {
         Date timeCreated = new Date(Long.parseLong(results[10]));
         date.setText(DateFormat.getDateTimeInstance().format(timeCreated));
         Double lat = Double.parseDouble(results[8]);
         Double lng = Double.parseDouble(results[9]);
         latLng = new LatLng(lat, lng);

         StringBuilder messageText = new StringBuilder();
         String firstName = deNull(results[2]);
         String lastName = deNull(results[3]);
         String email = deNull(results[4]);
         String subject = deNull(results[5]);
         String webAddress = deNull(results[6]);
         String details = deNull(results[7]);
         
         if(!firstName.isEmpty())
            messageText.append("First Name: ").append(firstName+"\n");
         if(!lastName.isEmpty())
            messageText.append("Last Name: ").append(lastName+"\n");
         if(!email.isEmpty())
            messageText.append("Email: ").append(email+"\n");
         if(!subject.isEmpty())
            messageText.append("Subject: ").append(subject+"\n");
         if(!webAddress.isEmpty())
            messageText.append("Web Address: ").append(webAddress+"\n");
         if(!details.isEmpty())
            messageText.append("Details: ").append(details+"\n");
         description.setText(messageText);
      }
   }

   /**
    * If a string is null, the empty String is returned, 
    * otherwise the String is trimmed and returned to the caller
    * 
    * @param value The string to de-null or trim
    * 
    */
   private static String deNull(String value) {
      if (value == null)
         return "";
      else
         return value.trim();

   }

   private void setUpMapIfNeeded() {
      if (map == null) {
    	  map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
         if (map != null) {
            setUpMap();
         }
      }
   }
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case android.R.id.home:
         return fme.common.SettingsMenu.backToHome(getBaseContext());
      }
      return false;
   }

}
