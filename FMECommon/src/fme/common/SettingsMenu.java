/*============================================================================= 
 
   Name     : SettingsMenu.java
 
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsMenu extends Activity {
   public static final String STORE_UNSENT_REPORTS = "storeUnsentReports";
   protected static final String LOGIN_MODE = "login_mode";
   protected static final int SWITCH = 2001;
   protected static final int NEW_LOGIN = 2002;
   protected static final int LOGOUT = 2003;
   /** Called when the activity is first created. */
   static private FMEApplication application;
   /**
    * Use this constant to identify when the default distance should be used.
    */
   private static int DEFAULT_DISTANCE = -1;
   private static boolean HIGH_PRECISION_DEFAULT = true;
   private static boolean AUTO_REPORT_DEFAULT = false;

   private SharedPreferences appPrefs;
   private Editor editor;

   private LinearLayout accountInfo;
   private TextView serverText;
   private TextView usernameText;

   private TextView topicsSubscribedText;
   private TextView topicsSubscribedTextReport;
   private RelativeLayout topicsToSubscribe;
   private RelativeLayout topicsToSubscribeReport;

   private Switch autoReportLocation;
   private Switch useHighPrecision;
   private Switch saveUnsentReports;

   private TextView timeIntervalText;
   private TextView distanceFilterText;
   private LinearLayout filteringLayout;
   private LinearLayout timeReportLayout;
   private ProgressDialog verifyingNow;
   private NumberPicker distancePicker;
   private NumberPicker minutePicker;
   private NumberPicker secondPicker;
   private LinearLayout timeIntervalPicker;

   private boolean oldDistanceValue = false;
   private boolean oldTimeValue = false;

   private boolean isDialogShowing = false;

   public static void startActivity(Context context, FMEApplication app) {
      application = app;

      // Create and start intent for this activity
      Intent intent = new Intent(context, SettingsMenu.class);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      context.startActivity(intent);
   }

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_settings);

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      appPrefs = PreferenceManager
            .getDefaultSharedPreferences(this);
      editor = appPrefs.edit();
      
      // These are the saved labels
      usernameText = (TextView) findViewById(R.id.userNameLabel);
      serverText = (TextView) findViewById(R.id.serverNameLabel);
      accountInfo = (LinearLayout) findViewById(R.id.accountInfoLayout);

      topicsSubscribedText = (TextView) findViewById(R.id.numSelectedLabel);
      topicsSubscribedTextReport = (TextView) findViewById(R.id.numSelectedLabelReport);
      topicsToSubscribe = (RelativeLayout) findViewById(R.id.topicsToSubscribe);
      topicsToSubscribeReport = (RelativeLayout) findViewById(R.id.topicsToSubscribeReport);
      
      timeIntervalText = (TextView) findViewById(R.id.timeIntervalTextMin);
      distanceFilterText = (TextView) findViewById(R.id.distanceFilterText);
      useHighPrecision = (Switch) findViewById(R.id.highPrecisionCheckBox);
      autoReportLocation = (Switch) findViewById(R.id.AutoReportLocationCheckbox);

      filteringLayout = (LinearLayout) findViewById(R.id.filteringLayout);
      timeReportLayout = (LinearLayout) findViewById(R.id.timeIntervalLayout);

      saveUnsentReports = (Switch) findViewById(R.id.saveUnsentReports);
      
      setupPickers();

      saveUnsentReports.setChecked(appPrefs.getBoolean(STORE_UNSENT_REPORTS, true));
      
      hideAlertWidgetsIfNeeded();
      
      autoReportLocation
            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               public void onCheckedChanged(CompoundButton buttonView,
                     boolean isChecked) {
                  
                  boolean oldValue = appPrefs.getBoolean(getResources().getString(R.string.autoReportLocation),AUTO_REPORT_DEFAULT);
                  
                  if(oldValue!=isChecked){
                     setVisibleBasedOnAutoReport(isChecked);
                     editor.putBoolean(getResources().getString(R.string.autoReportLocation),
                        isChecked);
                     editor.commit();
                     application.setAutoUpdate(isChecked);
                  }
               }
            });

      useHighPrecision
            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
               public void onCheckedChanged(CompoundButton buttonView,
                     boolean isChecked) {
                  boolean oldValue = appPrefs.getBoolean(getResources().getString(R.string.high_precision),HIGH_PRECISION_DEFAULT);
                  
                  if(oldValue!=isChecked){
                     if(isChecked){
                        FMEApplication.getSuperInstance().isGPSEnabled(SettingsMenu.this);
                     }
                     editor.putBoolean(getResources().getString(R.string.high_precision),
                        isChecked);
                     editor.commit();
                     application.updateLocationRequest();
                  }
               }
            });

      topicsToSubscribe.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {

               launchTopics(true);
         }
      });
      
      topicsToSubscribeReport.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {

               launchTopics(false);
         }
      });
      
      accountInfo.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            createAccountDialog();
         }
      });
      
      saveUnsentReports.setOnCheckedChangeListener(new OnCheckedChangeListener(){
         public void onCheckedChanged(CompoundButton buttonView,
               boolean isChecked) {
            appPrefs.edit().putBoolean(STORE_UNSENT_REPORTS, isChecked).commit();
         }
      });
   }

   private void hideAlertWidgetsIfNeeded()
   {
      if (application.getClass().getName().equals("fme.reporter.FMEReporterApplication"))
      {
         findViewById(R.id.networkGroup).setVisibility(View.GONE);
         findViewById(R.id.topicsToSubscribe).setVisibility(View.GONE);
         findViewById(R.id.horizontalRuleAlert).setVisibility(View.GONE);
      }
   }

   private String addPadding(String input){
      String padding = "         ";
      return padding+input+padding;
   }
   
   /**
    * Initialize fields for time picker and distance picker.
    */
   private void setupPickers() {
      distancePicker = (NumberPicker) findViewById(R.id.distancePicker);
      minutePicker = (NumberPicker) findViewById(R.id.minutePicker);
      secondPicker = (NumberPicker) findViewById(R.id.secondPicker);
      timeIntervalPicker = (LinearLayout) findViewById(R.id.timeIntervalPicker);

      // Setup the Number Picker for Distance Interval
      distancePicker.setMaxValue(100);
      distancePicker.setMinValue(0);
      String[] displayedValues = new String[101];
      displayedValues[0] = "Disabled";
      for (int i = 1; i < 101; i++) {
         displayedValues[i] = addPadding(Integer.toString(i * 100)+ " Meters");
      }

      distancePicker.setDisplayedValues(displayedValues);

      // Setup the Number Picker for Seconds Interval
      secondPicker.setMinValue(0);
      secondPicker.setMaxValue(59);
      displayedValues = new String[60];

      for (int i = 0; i < 60; i++) {
         displayedValues[i] = addPadding(Integer.toString(i) + " Secs");
      }
      displayedValues[1] = "1 Sec";
      secondPicker.setDisplayedValues(displayedValues);

      // Setup the Number Picker for Minutes Interval
      minutePicker.setMinValue(0);
      minutePicker.setMaxValue(60);
      displayedValues = new String[61];

      for (int i = 0; i < 61; i++) {
         displayedValues[i] = addPadding(Integer.toString(i) + " Mins");
      }
      displayedValues[1] = "1 Min";
      minutePicker.setDisplayedValues(displayedValues);

      filteringLayout.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            toggleVisibility(distancePicker);
         }
      });

      timeReportLayout.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            toggleVisibility(timeIntervalPicker);

         }
      });

      distancePicker
            .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

               public void onValueChange(NumberPicker picker, int oldVal,
                     int newVal) {
                  distanceFilterText.setText(picker.getDisplayedValues()[newVal]
                        .toString().trim());
                  if (newVal == 0) {
                     if (minutePicker.getValue() == 0
                           && secondPicker.getValue() == 0) {
                        setTimeDefault(true);
                     }
                  } else if (oldTimeValue) {
                     setTimeDefault(false);
                  }
               }
            });
      minutePicker
            .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

               public void onValueChange(NumberPicker picker, int oldVal,
                     int newVal) {
                  updateTime();
               }
            });
      secondPicker
            .setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {

               public void onValueChange(NumberPicker picker, int oldVal,
                     int newVal) {
                  updateTime();

               }
            });
   }

   /**
    * Change the displayed time to match the pickers.
    */
   private void updateTime() {
      int m = minutePicker.getValue();
      int s = secondPicker.getValue();

      if (s == 0 && m == 0) {
         if (distancePicker.getValue() == 0) {
            setDistanceDefault(true);
         }
         timeIntervalText.setText("Disabled");

      } else {
         timeIntervalText.setText(getTimeToDisplay(m, s));
         if (oldDistanceValue) {
            setDistanceDefault(false);
         }
      }
   }

   /**
    * Set the distance filter to its default. Call this method when both time
    * and distance filter have been disabled and the user has changed distance
    * more recently.
    */
   private void setDistanceDefault(boolean setDefault) {
      oldDistanceValue = setDefault;
      if (setDefault) {
         Toast.makeText(
               this,
               "Both Time Interval and Distance Filter cannot be Disabled. Distance Filter will be set to the default value.",
               Toast.LENGTH_LONG).show();
         setDistance(DEFAULT_DISTANCE);
      } else {
         setDistance(0);
      }
   }

   /**
    * Set the interval to its default. Call this method when both time and
    * distance filter have been disabled and the user has changed the time.
    */
   private void setTimeDefault(boolean setDefault) {
      oldTimeValue = setDefault;
      if (setDefault) {
         Toast.makeText(
               this,
               "Both Time Interval and Distance Filter cannot be Disabled. Time Interval will be set to the default value.",
               Toast.LENGTH_LONG).show();
         minutePicker.setValue(1);
         timeIntervalText.setText("1 Minute (Default)");
      } else {
         minutePicker.setValue(0);
         timeIntervalText.setText("Disabled");
      }
   }

   /**
    * Convert the minutes and seconds selected by the picker to displayable
    * text.
    */
   private String getTimeToDisplay(int m, int s) {
      if (s == 0 && m == 0)
         return "Disabled";
      String minText = "";
      String secText = "";
      String space = "";
      if (m == 1)
         minText = "1 Min";
      else if (m != 0)
         minText = m + " Mins";
      if (s == 1)
         secText = "1 Sec";
      else if (s != 0)
         secText = s + " Secs";
      if (s != 0 && m != 0)
         space = " ";

      return minText + space + secText;
   }

   /**
    * Set the distance picker and the distance filter text to the appropriate
    * values based on given input (which is in meters). An input of
    * DEFAULT_DISTANCE (a negative constant) sets them to the default value.
    */
   private void setDistance(int distanceFilter) {
      if (distanceFilter == DEFAULT_DISTANCE) {
         distancePicker.setValue(5);
         distanceFilterText.setText("500 Meters (Default)");
      } else if (distanceFilter == 0) {
         distanceFilterText.setText("Disabled");
         distancePicker.setValue(0);
      } else {
         distanceFilterText.setText(Integer.toString(distanceFilter)
               + " Meters");
         distancePicker.setValue(distanceFilter / 100);
      }
   }

   private void toggleVisibility(final View view) {
      if (view.getVisibility() == View.VISIBLE) {
         Animation animationOut = AnimationUtils.loadAnimation(this,
               R.anim.fade_out);
         view.startAnimation(animationOut);
         new Handler().postDelayed(new Runnable() {
            public void run() {
               view.setVisibility(View.GONE);
            }
         }, 450);
      } else {
         Animation animationIn = AnimationUtils.loadAnimation(this,
               R.anim.fade_in);
         view.startAnimation(animationIn);
         view.setVisibility(View.VISIBLE);
      }
   }

   protected void launchMessageDetails() {
      Intent i = new Intent(this, MessageDetails.class);
      i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      startActivity(i);
   }

   class AccountDialogWrapper {
      public EditText host;
      public EditText userName;
      public EditText password;
      View base;

      AccountDialogWrapper(View base, Context context) {
         this.base = base;
         host = (EditText) base.findViewById(R.id.serverNameText);
         userName = (EditText) base.findViewById(R.id.userNameText);
         password = (EditText) base.findViewById(R.id.passwordText);

         SharedPreferences prefs = PreferenceManager
               .getDefaultSharedPreferences(context);

         String serverURL = prefs.getString(
               getResources().getString(R.string.host), null);
         String username = prefs.getString(
               getResources().getString(R.string.username), null);
         String passwordText = prefs.getString(
               getResources().getString(R.string.password), null);

         if (serverURL != null) {
            host.setText(serverURL, TextView.BufferType.EDITABLE);
         }
         if (username != null) {
            userName.setText(username, TextView.BufferType.EDITABLE);
         }
         if (passwordText != null) {
            password.setText(passwordText, TextView.BufferType.EDITABLE);
         }

      }

      String getServerName() {
         return host.getText().toString();
      }

      String getUserName() {
         return userName.getText().toString();
      }

      String getPassword() {
         return password.getText().toString();
      }
   }

   private static final String NETWORK_ERROR_TITLE = "Network Error";
   private static final String NETWORK_ERROR_MESSAGE = 
         "Please ensure your device is connected to the internet before continuing.";
   private static final String LOGIN_ERROR_TITLE = "Not Logged In";
   private static final String LOGIN_ERROR_MESSAGE = 
         "Please login before selecting a topic.";
   
   
   private void launchTopics(boolean alert) {
      if (usernameText.getText() == null || usernameText.getText() == "") {
         errorAlertDialog(LOGIN_ERROR_TITLE,LOGIN_ERROR_MESSAGE);
      } else if(!FMEApplication.getSuperInstance().isConnectedToNetwork()){
         errorAlertDialog(NETWORK_ERROR_TITLE,
               NETWORK_ERROR_MESSAGE);
      }
      else {
         Intent i = new Intent(this, FMETopics.class);
         i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         i.putExtra("alert", alert);
         startActivity(i);
      }      
   }

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);

   }

   @Override
   protected void onResume() {
      super.onResume();

      String host = appPrefs.getString(getResources().getString(R.string.host),
            null);
      String username = appPrefs.getString(
            getResources().getString(R.string.username), null);

      if (host != null) {
         serverText.setText(host, TextView.BufferType.NORMAL);
      }

      if (username != null) {
         usernameText.setText(username, TextView.BufferType.NORMAL);
      }

      int timeInterval = appPrefs.getInt(
            getResources().getString(R.string.Time_interval_label), 60);
      int distanceFilter = appPrefs.getInt(
            getResources().getString(R.string.Distance_filter_label), 500);

      setDistance(distanceFilter);

      int mins = timeInterval / 60;
      int secs = timeInterval % 60;
      timeIntervalText.setText(getTimeToDisplay(mins, secs));
      secondPicker.setValue(secs);
      minutePicker.setValue(mins);

      boolean highPrecisionOn = appPrefs.getBoolean(
            getResources().getString(R.string.high_precision), HIGH_PRECISION_DEFAULT);

         useHighPrecision.setChecked(highPrecisionOn);

      boolean autoReport = appPrefs.getBoolean(
            getResources().getString(R.string.autoReportLocation), AUTO_REPORT_DEFAULT);
      autoReportLocation.setChecked(autoReport);
      setVisibleBasedOnAutoReport(autoReport);

      int size = appPrefs.getInt(FMETopics.TOPIC_SIZE_ALERT, 0);
      int sizeReport = appPrefs.getInt(FMETopics.TOPIC_SIZE_REPORT, 0);
      topicsSubscribedText.setText(Integer.toString(size) + " selected");
      topicsSubscribedTextReport.setText(Integer.toString(sizeReport) + " selected");
   }

   
   @Override
   protected void onPause() {
      super.onPause();

      if (null != verifyingNow && verifyingNow.isShowing()) {
         verifyingNow.dismiss();
         verifyingNow = null;
      }

      int distanceFilter = distancePicker.getValue() * 100;
      int oldDistanceFilter =  appPrefs.getInt(getResources().getString(R.string.Distance_filter_label),
            Integer.MIN_VALUE);

      int m = minutePicker.getValue();
      int s = secondPicker.getValue();
      int timeValueEntered = m * 60 + s;
      
      int oldTimeInterval = appPrefs.getInt(getResources().getString(R.string.Time_interval_label),
            Integer.MIN_VALUE);
      
      
      if(oldDistanceFilter!=distanceFilter || oldTimeInterval!=timeValueEntered){
         editor.putInt(
               getResources().getString(R.string.Distance_filter_label),
               distanceFilter);
         editor.putInt(getResources().getString(R.string.Time_interval_label),
               timeValueEntered);
         editor.commit();
         application.updateLocationRequest();
      }
   }

   public void setVisibleBasedOnAutoReport(boolean autoUpdateOn) {

      filteringLayout.setVisibility(autoUpdateOn ? View.VISIBLE : View.GONE);
      timeReportLayout.setVisibility(autoUpdateOn ? View.VISIBLE : View.GONE);
   }

   public void createLogoutDialog() {
      if(!FMEApplication.getSuperInstance().isConnectedToNetwork()){
         errorAlertDialog(NETWORK_ERROR_TITLE,
               NETWORK_ERROR_MESSAGE);
      return;
      }
      
      AlertDialog accountDialog = new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage(
                  "Are you sure you want to logout? This will delete all your saved data.")
            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) {
                        PreferenceManager
                        .getDefaultSharedPreferences(getBaseContext()).edit()
                        .putInt(LOGIN_MODE, LOGOUT).commit();

                  FMEApplication.getSuperInstance().handleCancelSubscriptions(
                        SettingsMenu.this);
                  
               }
            }).setNegativeButton("No", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int whichButton) {
                  // ignore, just dismiss
               }
            }).setCancelable(true).create();
      accountDialog.show();
   }

   public void createAccountDialog() {

      final Context context = this;
      LayoutInflater inflater = LayoutInflater.from(this);
      View accountView = inflater.inflate(R.layout.account_info, null);
      final AccountDialogWrapper wrapper = new AccountDialogWrapper(
            accountView, this);

      AlertDialog accountDialog = new AlertDialog.Builder(this)
            .setTitle(R.string.fme_server_account_information)
            .setView(accountView)
            .setPositiveButton(R.string.save,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {

                        if(!FMEApplication.getSuperInstance().isConnectedToNetwork()){
                           errorAlertDialog(NETWORK_ERROR_TITLE,
                                 NETWORK_ERROR_MESSAGE);
                        return;
                        }
                        
                        SharedPreferences prefs = PreferenceManager
                              .getDefaultSharedPreferences(context);
                        String oldHost = prefs.getString(getResources()
                              .getString(R.string.host), null);
                        String oldUser = prefs.getString(getResources()
                              .getString(R.string.username), null);

                        final String newHost = wrapper.getServerName();
                        final String newUser = wrapper.getUserName();
                        final String newPass = wrapper.getPassword();

                        if ((oldHost != null && (newHost == null || oldHost
                              .compareTo(newHost) != 0))
                              || (oldUser != null && (newUser == null || oldUser
                                    .compareTo(newUser) != 0))) {
                           AlertDialog areYouSure = new AlertDialog.Builder(
                                 context)
                                 .setMessage(
                                       R.string.Save_and_Remove_Subscriptions)
                                 .setPositiveButton(R.string.save,
                                       new DialogInterface.OnClickListener() {
                                          public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                             
                                             PreferenceManager
                                                   .getDefaultSharedPreferences(
                                                         getBaseContext())
                                                   .edit()
                                                   .putInt(LOGIN_MODE,
                                                         SWITCH).commit();
                                             checkAccountInfo(newHost, newUser,
                                                   newPass);
                                          }
                                       })
                                 .setNegativeButton(R.string.dont_save,
                                       new DialogInterface.OnClickListener() {
                                          public void onClick(
                                                DialogInterface dialog,
                                                int whichButton) {
                                             // ignore, just dismiss
                                          }
                                       }).setCancelable(false).create();

                           areYouSure.show();
                        } else {
                           PreferenceManager
                                 .getDefaultSharedPreferences(getBaseContext())
                                 .edit().putInt(LOGIN_MODE, NEW_LOGIN)
                                 .commit();
                           checkAccountInfo(newHost, newUser, newPass);
                        }
                     }
                  })
            .setNegativeButton(R.string.cancel,
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int whichButton) {
                        // ignore, just dismiss
                     }
                  }).create();
      accountDialog
            .setOnDismissListener(new DialogInterface.OnDismissListener() {

               public void onDismiss(DialogInterface dialog) {
                  isDialogShowing = false;
               }
            });
      if (!isDialogShowing) {
         isDialogShowing = true;
         accountDialog.show();
      }
   }


   public void logout() {
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(this);
      switch (prefs.getInt(LOGIN_MODE, NEW_LOGIN)) {
      case NEW_LOGIN:
           
         break;
      case SWITCH:
         prefs.edit().putInt(FMETopics.TOPIC_SIZE_REPORT, 0).commit();
         prefs.edit().putInt(FMETopics.TOPIC_SIZE_ALERT, 0).commit();
         Toast.makeText(this, "Switched Users Successfully", Toast.LENGTH_SHORT).show();
         break;
      case LOGOUT:
         prefs.edit()
         .putInt(FMETopics.TOPIC_SIZE_REPORT, 0)
         .putInt(FMETopics.TOPIC_SIZE_ALERT, 0)
         .remove(getResources().getString(R.string.username))
         .remove(getResources().getString(R.string.host))
         .remove(getResources().getString(R.string.password)).commit();
         int[] deleted = FMEApplication.getSuperInstance().deleteUserData();
         String extraInfo;
         if(deleted!=null)
            extraInfo="\n"+deleted[0]+" Alerts, "+deleted[1]+" Reports, and "
                  +deleted[2]+" Unsent Reporst deleted from your device.";
         else
            extraInfo="";
         Toast.makeText(this, "Logout Success"+extraInfo, Toast.LENGTH_LONG).show();
         break;
      }
      redisplayAccountInfo();
   }
   
   

   /**
    * Retrieve account info from preferences and update textviews.
    */
   private void redisplayAccountInfo() {
     
      SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(this);
      String host = prefs.getString(getResources().getString(R.string.host),
            null);
      String username = prefs.getString(
            getResources().getString(R.string.username), null);
      if (host != null)
         serverText.setText(host, TextView.BufferType.NORMAL);
      else
         serverText.setText("", TextView.BufferType.NORMAL);
      if (username != null)
         usernameText.setText(username, TextView.BufferType.NORMAL);
      else
         usernameText.setText("", TextView.BufferType.NORMAL);
      
      int topicsAlert = prefs.getInt(FMETopics.TOPIC_SIZE_ALERT, 0);
      topicsSubscribedText.setText(topicsAlert + " selected");
      int topicsReport = prefs.getInt(FMETopics.TOPIC_SIZE_REPORT, 0);
      topicsSubscribedTextReport.setText(topicsReport + " selected");
   }

   private class VerificationTask extends FetchTopicsTask {
      
      @Override
      protected void onPostExecute(String result) {

         if (null != verifyingNow) {
            verifyingNow.dismiss();
            verifyingNow = null;
         }
         // Check if there was a problem during the background work
         if (result.isEmpty()) {
            String msg = "Account information verification failed. "
                  + "Please check your account information and try again.";
            errorAlertDialog("Login Unsuccessful", msg);
         } else {
            SharedPreferences.Editor editor = PreferenceManager
                  .getDefaultSharedPreferences(SettingsMenu.this).edit();

            editor.putString(getResources().getString(R.string.host), host);
            editor.putString(getResources().getString(R.string.username),
                  username);
            editor.putString(getResources().getString(R.string.password),
                  password);
            editor.commit();
            if (host != null) {
               serverText.setText(host, TextView.BufferType.NORMAL);
            }
            if (username != null) {
               usernameText.setText(username, TextView.BufferType.NORMAL);
            }

            String msg = "Successfully updated account information";
            Toast.makeText(SettingsMenu.this, msg, Toast.LENGTH_SHORT).show();

            if(PreferenceManager.getDefaultSharedPreferences(SettingsMenu.this).getInt(LOGIN_MODE, NEW_LOGIN)!=NEW_LOGIN)
            FMEApplication.getSuperInstance().handleCancelSubscriptions(
                  SettingsMenu.this);
         }
      }
   }

   public void errorAlertDialog(String title, String errorMsg) {
      if (null != verifyingNow) {
         verifyingNow.dismiss();
         verifyingNow = null;
      }

      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(title).setMessage(errorMsg).setCancelable(true);

      AlertDialog ad = builder.create();
      ad.show();
   }

   private void checkAccountInfo(String serverURL, String username,
         String password) {

      VerificationTask task = new VerificationTask();
      
      task.execute(new String[] { serverURL, username, password });
      createVerificationProgressDialog(task);
   }

   private void createVerificationProgressDialog(final VerificationTask task) {
      verifyingNow = new ProgressDialog(this);
      verifyingNow.setTitle("Verifying account information...");
      verifyingNow.setMessage("Please wait...");
      verifyingNow.setButton(ProgressDialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener(){
         public void onClick(DialogInterface arg0, int arg1) {
            task.cancel(true);
            arg0.dismiss();
         }
         
      });
      verifyingNow.setCancelable(false);
      verifyingNow.show();
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      // Create Settings menu item
      MenuItem item1 = menu.add(0, 1, 1, "About");
      item1.setIcon(android.R.drawable.ic_menu_info_details);
      item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

      MenuItem item2 = menu.add(0, 2, 2, "Logout");
      item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
      case 1:
         Intent i = new Intent(getBaseContext(), About.class);
         i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
         startActivity(i);
         return true;

      case 2:
         createLogoutDialog();
         return true;
      case android.R.id.home:
         return backToHome(getBaseContext());
      }
      return false;
   }

   /**
    * Clears all Activities on back stack except for Main Activity.
    */
   public static boolean backToHome(Context context) {
      PackageManager pm = context.getPackageManager();
      Intent intent = pm.getLaunchIntentForPackage(context.getPackageName());
      intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
      intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
      context.startActivity(intent);
      return true;
   }
   
   


   
}
