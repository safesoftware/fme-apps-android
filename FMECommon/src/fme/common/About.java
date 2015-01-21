/*============================================================================= 
 
   Name     : About.java
 
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

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class About extends Activity {

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.about_alerts);

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      TextView version = (TextView) findViewById(R.id.versionID);
      try {
         String versionName = this.getPackageManager().getPackageInfo(
               this.getPackageName(), 0).versionName;
         int versionCode = this.getPackageManager().getPackageInfo(
               this.getPackageName(), 0).versionCode;
         version.setText(versionName + "." + versionCode);
      } catch (NameNotFoundException e) {
         e.printStackTrace();
      }

      LinearLayout aboutSafe = (LinearLayout) findViewById(R.id.aboutSafeLayout);
      LinearLayout aboutServer = (LinearLayout) findViewById(R.id.aboutServerLayout);
      LinearLayout aboutService = (LinearLayout) findViewById(R.id.aboutServiceLayout);

      aboutSafe.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                  .parse("http://www.safe.com"));
            startActivity(browserIntent);
         }
      });

      aboutServer.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                  .parse("http://fme.ly/2du"));
            startActivity(browserIntent);
         }
      });

      aboutService.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri
                  .parse("http://fme.ly/dpd"));
            startActivity(browserIntent);
         }
      });
   }

   public void onClick_ExpandCopyright(View v){
	   TextView view = (TextView)v;
	   String currentText = view.getText().toString();
	   if(currentText.equals(getResources().getString(R.string.copyright)))
			view.setText(R.string.copyright_expanded);
	   else
		   view.setText(R.string.copyright);
   }
   
   
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case android.R.id.home:
         return SettingsMenu.backToHome(getBaseContext());
      }
      return false;
   }

}
