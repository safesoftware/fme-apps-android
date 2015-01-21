/*============================================================================= 
 
   Name     : MessageDetails.java
 
   System   : FME Common
 
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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MessageDetails extends Activity {
	EditText firstNameField;
	EditText lastNameField;
	EditText emailField;
	EditText titleField;
	EditText webAddressField;
	EditText detailsField;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.message_settings);
		
		ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);
		
		firstNameField = (EditText) findViewById(R.id.firstNameText);
		lastNameField = (EditText) findViewById(R.id.lastNameText);
		emailField = (EditText) findViewById(R.id.emailText);
		titleField = (EditText) findViewById(R.id.subjectText);
		webAddressField = (EditText) findViewById(R.id.webAddressText);
		detailsField = (EditText) findViewById(R.id.detailsText);

		//Clear away any funny focusing
      LinearLayout overallLayout = (LinearLayout) findViewById(R.id.mainLinearLayout);
      overallLayout.requestFocus();
		
		
final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
		
		firstNameField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.firstNameLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });

		lastNameField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.lastNameLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });
		
		emailField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.emailLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });
		
		titleField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.subjectLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });
		
		webAddressField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.webAddressLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });
		
		detailsField.setOnFocusChangeListener(new View.OnFocusChangeListener()
	    {
	        public void onFocusChange(View v, boolean hasFocus)
	        {
	            if(!hasFocus)
	            {
	                EditText e = (EditText)v;
	                SharedPreferences.Editor edit = pref.edit();
	                    edit.putString(getResources().getString(R.string.detailsLabel),e.getText().toString());
	                    edit.commit();
	            }
	        }
	    });

	}
	@Override
	public void onResume() {
        super.onResume();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String firstName = prefs.getString(getResources().getString(R.string.firstNameLabel),null);
		String lastName = prefs.getString(getResources().getString(R.string.lastNameLabel),null);
		String email = prefs.getString(getResources().getString(R.string.emailLabel),null);
		String subject = prefs.getString(getResources().getString(R.string.subjectLabel),null);
		String webAddress = prefs.getString(getResources().getString(R.string.webAddressLabel),null);
		String details = prefs.getString(getResources().getString(R.string.detailsLabel),null);
		
		if (firstName != null) {
			firstNameField.setText(firstName, TextView.BufferType.NORMAL);
		}
		if (lastName != null) {
			lastNameField.setText(lastName, TextView.BufferType.NORMAL);
		}
		if (email != null) {
			emailField.setText(email, TextView.BufferType.NORMAL);
		}
		if (subject != null) {
			titleField.setText(subject, TextView.BufferType.NORMAL);
		}
		if (webAddress != null) {
			webAddressField.setText(webAddress, TextView.BufferType.NORMAL);
		}
		if (details != null) {
			detailsField.setText(details, TextView.BufferType.NORMAL);
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
	
}