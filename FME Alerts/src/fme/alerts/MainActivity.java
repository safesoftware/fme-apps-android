/*============================================================================= 
 
   Name     : MainActivity.java
 
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

import java.io.IOException;
import java.util.ArrayList;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import fme.alerts.AlertsContentProvider.ReporterColumns;
import fme.alerts.AlertsContentProvider.TableColumns;
import fme.alerts.fragments.AlertsListFragment;
import fme.alerts.fragments.MapPrefsFragment;
import fme.alerts.fragments.ReportsListFragment;
import fme.alerts.fragments.UnsentListFragment;
import fme.alerts.fragments.AbstractListFragment.OnCloseDrawerListener;
import fme.common.FMEApplication;
import fme.common.SettingsMenu;

public class MainActivity extends Activity implements
ConnectionCallbacks,OnConnectionFailedListener,
      LoaderManager.LoaderCallbacks<Cursor>, OnCloseDrawerListener {
   
   /**
    * The key for the fragment to re-create after the main activity is restarted.
    */
   private static final String IS_MAP = "isMap";
   private static final int ALERTS_CURSOR = 1;
   private static final int REPORTS_CURSOR = 0;
   private static final int UNSENT_CURSOR = 2;
   
   private static final int PLAY_SERVICE_RESOLUTION = 9000;
   private static final String APP_VERSION = "app_version";
   
   private static final String ALERT_TAG = "alert_tag";
   private static final String REPORT_TAG = "report_tag";
   private static final String UNSENT_TAG = "unsent_tag";
   private static final String PREF_TAG = "pref_tag";

   private int transparentBackground;
   
   private GoogleMap map;
   private Button sendButton;

   private EditText firstNameField;
   private EditText lastNameField;
   private EditText emailField;
   private EditText titleField;
   private EditText webAddressField;
   private EditText detailsField;

   private ViewSwitcher switcher;
   private DrawerLayout mDrawerLayout;
   private ActionBarDrawerToggle mDrawerToggle;

   private Fragment reportsFragment;
   private Fragment alertsFragment;
   private Fragment unsentFragment;
   private Fragment prefsFragment;
   private FragmentManager fragmentManager;
   

   private boolean showReports;
   private boolean showAlerts;
   private boolean showUnsent;
   
   private ArrayList<Marker> reportMarkers;
   private ArrayList<Marker> alertMarkers;
   private ArrayList<Marker> unsentMarkers;
   
   private SharedPreferences pref;
   private LocationClient locationClient;
   enum LocationRequestType{MoveCamera};
   private LocationRequestType locationRequestType;
   private Toast toast;

   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {

      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);
      locationClient = new LocationClient(this, this, this);
      pref = PreferenceManager
            .getDefaultSharedPreferences(this);
      
      TypedArray styledAttributes = obtainStyledAttributes(new int[] { android.R.attr.selectableItemBackground });
      transparentBackground = styledAttributes.getResourceId(0, 0);
      styledAttributes.recycle();
      
      registerDeviceWithGCM();
      setUpNavigationDrawer();

      reportMarkers = new ArrayList<Marker>();
      alertMarkers = new ArrayList<Marker>();
      unsentMarkers = new ArrayList<Marker>();
      reportsFragment = new ReportsListFragment();
      alertsFragment = new AlertsListFragment();
      unsentFragment = new UnsentListFragment();
      prefsFragment = new MapPrefsFragment();
      fragmentManager = getFragmentManager();

      if (setUpMapIfNeeded()) {
         if (savedInstanceState == null) {
            initMapCamera();
         }
      }

      initWidgets();
      initToggleButton();
      FMEAlertsApplication.getInstance().updateMainActivityHandle(this);
      initMessageScreen();
      initLoaders(savedInstanceState);
      FMEApplication.getSuperInstance().isGPSEnabled(this);
   }

   private void initWidgets(){
      switcher = (ViewSwitcher) findViewById(R.id.switcher);
      sendButton = (Button) findViewById(R.id.SendButton2);
      // Register handler for UI elements
      sendButton.setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            onSendClick(sendButton);
         }
      });
   }
   
   private void initLoaders(Bundle savedInstanceState){
      if(savedInstanceState==null){
         getLoaderManager().initLoader(ALERTS_CURSOR, null, this);
         getLoaderManager().initLoader(REPORTS_CURSOR, null, this);
         getLoaderManager().initLoader(UNSENT_CURSOR, null, this);
         } else {
//If activity not being started for the first time, restart loaders instead of initializing
            getLoaderManager().restartLoader(ALERTS_CURSOR, null, this);
            getLoaderManager().restartLoader(REPORTS_CURSOR, null, this);
            getLoaderManager().restartLoader(UNSENT_CURSOR, null, this);
         }
   }
   
   private void initMapCamera(){
      locationRequestType = LocationRequestType.MoveCamera;
      locationClient.connect();
   }
   
   private void setShowUnsentToggle(ImageButton v){
      switch(v.getId()){
      case R.id.toggleUnsentVisibility:
         if(showUnsent==false)
            v.setImageDrawable(getResources().getDrawable(R.drawable.red_circle_disabled));
         else
            v.setImageDrawable(getResources().getDrawable(R.drawable.red_circle));
         break;
      case R.id.toggleAlertVisibility:
         if(showAlerts==false)
            v.setImageDrawable(getResources().getDrawable(R.drawable.grn_circle_disabled));
         else
            v.setImageDrawable(getResources().getDrawable(R.drawable.grn_circle));
         break;
      case R.id.toggleReportVisibility:
         if(showReports==false)
            v.setImageDrawable(getResources().getDrawable(R.drawable.blue_circle_disabled));
         else
            v.setImageDrawable(getResources().getDrawable(R.drawable.blue_circle));
         break;
      }

   }
   
   /**
    * Called when a report fails to get sent.
    * Using this method will ensure that multiple
    * toasts won't get created at once.
    * 
    */
   public void updateToast(String msg){
      if(toast==null)
         toast = Toast.makeText(this, "", Toast.LENGTH_LONG);
      toast.setText(msg);
      toast.show();
   }
   
   public void onToggleReportVisibility(View v){
      showReports = !pref.getBoolean("showReports", true);
      pref.edit().putBoolean("showReports", showReports).commit();
      setShowUnsentToggle((ImageButton)v);
      getLoaderManager().restartLoader(REPORTS_CURSOR, null,
            MainActivity.this);
   }
   
   public void onToggleAlertVisibility(View v){
      showAlerts = !pref.getBoolean("showAlerts", true);
      pref.edit().putBoolean("showAlerts", showAlerts).commit();
      setShowUnsentToggle((ImageButton)v);
      getLoaderManager().restartLoader(ALERTS_CURSOR, null,
            MainActivity.this);
   }
   public void onToggleUnsentVisibility(View v){
      showUnsent = !pref.getBoolean("showUnsent", true);
      pref.edit().putBoolean("showUnsent", showUnsent).commit();
      setShowUnsentToggle((ImageButton)v);
      getLoaderManager().restartLoader(UNSENT_CURSOR, null,
            MainActivity.this);
   }
   
   private void initToggleButton() {
      showUnsent = pref.getBoolean("showUnsent", true);
      setShowUnsentToggle((ImageButton) findViewById(R.id.toggleUnsentVisibility));
      showAlerts = pref.getBoolean("showAlerts", true);
      setShowUnsentToggle((ImageButton) findViewById(R.id.toggleAlertVisibility));
      showReports = pref.getBoolean("showReports", true);
      setShowUnsentToggle((ImageButton) findViewById(R.id.toggleReportVisibility));
   }

   /**
    * Initialize fields for EditTexts of Message Screen and set
    * OnFocusChangeListeners.
    */
   private void initMessageScreen() {
      

      firstNameField = (EditText) findViewById(R.id.firstNameText);
      lastNameField = (EditText) findViewById(R.id.lastNameText);
      emailField = (EditText) findViewById(R.id.emailText);
      titleField = (EditText) findViewById(R.id.subjectText);
      webAddressField = (EditText) findViewById(R.id.webAddressText);
      detailsField = (EditText) findViewById(R.id.detailsText);

      firstNameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
               EditText e = (EditText) v;
               SharedPreferences.Editor edit = pref.edit();
               edit.putString(
                     getResources().getString(R.string.firstNameLabel), e
                           .getText().toString());
               edit.commit();
            }
         }
      });

      lastNameField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
               EditText e = (EditText) v;
               SharedPreferences.Editor edit = pref.edit();
               edit.putString(getResources().getString(R.string.lastNameLabel),
                     e.getText().toString());
               edit.commit();
            }
         }
      });

      emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
               EditText e = (EditText) v;
               SharedPreferences.Editor edit = pref.edit();
               edit.putString(getResources().getString(R.string.emailLabel), e
                     .getText().toString());
               edit.commit();
            }
         }
      });

      titleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
               EditText e = (EditText) v;
               SharedPreferences.Editor edit = pref.edit();
               edit.putString(getResources().getString(R.string.subjectLabel),
                     e.getText().toString());
               edit.commit();
            }
         }
      });

      webAddressField
            .setOnFocusChangeListener(new View.OnFocusChangeListener() {
               public void onFocusChange(View v, boolean hasFocus) {
                  if (!hasFocus) {
                     EditText e = (EditText) v;
                     SharedPreferences.Editor edit = pref.edit();
                     edit.putString(
                           getResources().getString(R.string.webAddressLabel),
                           e.getText().toString());
                     edit.commit();
                  }
               }
            });

      detailsField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         public void onFocusChange(View v, boolean hasFocus) {
            if (!hasFocus) {
               EditText e = (EditText) v;
               SharedPreferences.Editor edit = pref.edit();
               edit.putString(getResources().getString(R.string.detailsLabel),
                     e.getText().toString());
               edit.commit();
            }
         }
      });
   }

   private void setUpNavigationDrawer() {
      mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
      mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
            R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) {

         /** Called when a drawer has settled in a completely closed state. */
         public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
        //     getActionBar().setTitle(mTitle);
         }

         /** Called when a drawer has settled in a completely open state. */
         public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            // getActionBar().setTitle(mDrawerTitle);
         }
      };

      // Set the drawer toggle as the DrawerListener
      mDrawerLayout.setDrawerListener(mDrawerToggle);
      getActionBar().setDisplayHomeAsUpEnabled(true);
      getActionBar().setHomeButtonEnabled(true);
      
   }

   protected void launchClearPins() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder
            .setMessage(R.string.ClearAllPins)
            .setCancelable(true)
            .setPositiveButton("Remove All Pins",
                  new DialogInterface.OnClickListener() {
                     public void onClick(DialogInterface dialog, int id) {
                        map.clear();

                     }
                  })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  dialog.cancel();
               }
            });
      AlertDialog alert = builder.create();
      alert.show();
   }
   
   @Override
   public void onResume() {
      super.onResume();
      setUpMapIfNeeded();
      pref = PreferenceManager
            .getDefaultSharedPreferences(this);

      String firstName = pref.getString(
            getResources().getString(R.string.firstNameLabel), null);
      String lastName = pref.getString(
            getResources().getString(R.string.lastNameLabel), null);
      String email = pref.getString(
            getResources().getString(R.string.emailLabel), null);
      String subject = pref.getString(
            getResources().getString(R.string.subjectLabel), null);
      String webAddress = pref.getString(
            getResources().getString(R.string.webAddressLabel), null);
      String details = pref.getString(
            getResources().getString(R.string.detailsLabel), null);

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
   protected void onSaveInstanceState(Bundle outState) {
      if (reportsFragment.isAdded()) {
         outState.putBoolean("expandReports", true);
      }
      if (alertsFragment.isAdded()) {
         outState.putBoolean("expandAlerts", true);
      }
      if (unsentFragment.isAdded()) {
         outState.putBoolean("expandUnsent", true);
      }
      if (prefsFragment.isAdded()) {
         outState.putBoolean("expandPrefs", true);
      }
      super.onSaveInstanceState(outState);
      outState.putInt(IS_MAP, switcher.getDisplayedChild());
   }

   @Override
   protected void onRestoreInstanceState(Bundle savedInstanceState) {
      super.onRestoreInstanceState(savedInstanceState);
      
      int n = savedInstanceState.getInt(IS_MAP);
      if (n == 1)
         switcher.showNext();
      
      if (savedInstanceState.getBoolean("expandReports", false)) {
         hideOtherGroups(R.id.reportsList);
         findViewById(R.id.expandReportsFrameLayout).setBackgroundColor(
               getResources().getColor(R.color.BlackGrey));
         Fragment frag= fragmentManager.findFragmentByTag(REPORT_TAG);
         if(frag!=null)
            reportsFragment = frag;
      }
      if (savedInstanceState.getBoolean("expandAlerts", false)) {
         hideOtherGroups(R.id.alertsList);
         findViewById(R.id.expandAlertsFrameLayout).setBackgroundColor(
               getResources().getColor(R.color.BlackGrey));
         Fragment frag= fragmentManager.findFragmentByTag(ALERT_TAG);
         if(frag!=null)
            alertsFragment = frag;
      }
      if (savedInstanceState.getBoolean("expandUnsent", false)) {
         hideOtherGroups(R.id.unsentList);
         findViewById(R.id.expandUnsentFrameLayout).setBackgroundColor(
               getResources().getColor(R.color.BlackGrey));
         Fragment frag= fragmentManager.findFragmentByTag(UNSENT_TAG);
         if(frag!=null)
            unsentFragment = frag;
      }
      if (savedInstanceState.getBoolean("expandPrefs", false)) {
         hideOtherGroups(R.id.prefsList);
         findViewById(R.id.expandPrefsFrameLayout).setBackgroundColor(
               getResources().getColor(R.color.BlackGrey));
         Fragment frag= fragmentManager.findFragmentByTag(PREF_TAG);
         if(frag!=null)
            prefsFragment = frag;
      }
      
   }
   
   @Override
   protected void onPostCreate(Bundle savedInstanceState) {
      super.onPostCreate(savedInstanceState);
      // Sync the toggle state after onRestoreInstanceState has occurred.
      mDrawerToggle.syncState();
   }

   @Override
   public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      mDrawerToggle.onConfigurationChanged(newConfig);
   }

   public void onSendClick(View view) {
      // Do the following to ensure all messages get saved before a report
      // event, even if they are currently in focus
      view.setFocusable(true);
      view.setFocusableInTouchMode(true);
      view.requestFocus();
      view.setFocusable(false);
      view.setFocusableInTouchMode(false);

      boolean success = FMEAlertsApplication.getInstance().sendMessage(); 
      // Basic error checking
      if (!success) {
         insufficientInfoDialog();
      }
   }

   /**
    * Called when the delete icon is clicked in the reports row.
    */
   public void deleteAllReports(View view) {
      createDeleteDialog(AlertsContentProvider.REPORTS_TABLE_URI,"Report");
   }
   
   /**
    * Called when the delete icon is clicked in the unsent reports row.
    */
   public void deleteAllUnsentReports(View view){
      createDeleteDialog(AlertsContentProvider.UNSENT_TABLE_URI,"Unsent Report");
   }
   
   /**
    * Called when the delete icon is clicked in the alerts row.
    */
   public void deleteAllAlerts(View view){
      createDeleteDialog(AlertsContentProvider.ALERTS_TABLE_URI,"Alert");
   }
   
   /**
    * Create a dialog to confirm deletion of all items from selected table. 
    * 
    * @param table The URI of the table to delete all rows from.
    * @param item The name of the items being deleted. First letter(s) should be capitalized.
    * Should not be plural.
    */
   private void createDeleteDialog(final Uri table, final String item){
      AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
      builder.setMessage("This will delete all "+item.toLowerCase()+"s stored on your device")
      .setTitle("Delete All "+item+"s")
      .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
            int n = getContentResolver().delete(
                  table, null, null);
            if (n == 1) {
               Toast.makeText(MainActivity.this, "1 "+item.toLowerCase()+" deleted", Toast.LENGTH_SHORT).show();
            } else {
               Toast.makeText(MainActivity.this, n + " "+item.toLowerCase()+"s deleted", Toast.LENGTH_SHORT)
                     .show();
            }
         }
      })
      .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
         public void onClick(DialogInterface dialog, int which) {
            // Don't do anything
         }
      });
      builder.create().show();
   }

   /**
    * Launches the SettingsMenu activity to modify the global settings
    */
   protected void launchSettings() {
      SettingsMenu.startActivity(this, FMEAlertsApplication.getInstance());
   }

   protected boolean isRouteDisplayed() {
      return false;
   }

   public GoogleMap getMap() {
      return map;
   }

   private void insufficientInfoDialog() {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.insufficient_server_info)
            .setMessage(R.string.insufficient_info_details).setCancelable(true)
            .setPositiveButton("Close", new DialogInterface.OnClickListener() {
               public void onClick(DialogInterface dialog, int id) {
                  // do things
               }
            });
      AlertDialog alert = builder.create();
      alert.show();
   }

   // ==================================================================================================================//
   // Setup Menu and Handle Menu Option Clicks
   // ==================================================================================================================>>
   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      if (mDrawerToggle.onOptionsItemSelected(item)) {
         return true;
      } else
         return menuChoice(item);
   }

   private boolean menuChoice(MenuItem item) {
      switch (item.getItemId()) {
      case 3:
         if (switcher.getDisplayedChild() == 0) {
            switcher.showNext();
            item.setIcon(android.R.drawable.ic_menu_mapmode);
         } else {
            switcher.showPrevious();
            item.setIcon(android.R.drawable.ic_menu_edit);
            //Hide they keyboard when switching back to the map view
            switcher.postDelayed(new Runnable() {
               public void run() {
                  InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                  imm.hideSoftInputFromWindow(switcher.getWindowToken(), 0);
               }
            }, 50);
         }
         return true;
      case 2:
         launchSettings();
         return true;
      case 1:
         launchClearPins();
         return true;
      }
      return false;
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);
      // Create Settings menu item
      MenuItem item1 = menu.add(0, 2, 2, "Settings");
      item1.setIcon(android.R.drawable.ic_menu_manage);
      item1.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

      // Create Message/Map switcher menu item
      MenuItem item3 = menu.add(0, 3, 0, "Switch");
      if (switcher.getDisplayedChild() == 0)
         item3.setIcon(android.R.drawable.ic_menu_edit);
      else
         item3.setIcon(android.R.drawable.ic_menu_mapmode);
      item3.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
      return true;
   }
   // ==================================================================================================================||

   

   /**
    * Return false if map is null; true otherwise.
    * 
    */
   private boolean setUpMapIfNeeded() {
      // Do a null check to confirm that we have not already instantiated the
      // map.
      if (map == null) {
         // Try to obtain the map from the SupportMapFragment.
         map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
               .getMap();
         // Check if we were successful in obtaining the map.
         if (map != null) {
            map.getUiSettings().setZoomControlsEnabled(false);
            map.setMyLocationEnabled(true);
         }
         return true;
      } else
         return false;
   }

   // ==================================================================================================================//
   // Camera Update Based on Current Location (off by Default)
   // ==================================================================================================================>>
   public void onConnectionFailed(ConnectionResult arg0) {
   }

   public void onConnected(Bundle arg0) {
      Location lastKnownLocation = locationClient.getLastLocation();
      switch(locationRequestType){
      case MoveCamera:
         CameraUpdate cameraUpdate;
         if (null != lastKnownLocation) {
            LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),
                  lastKnownLocation.getLongitude());
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 14);
         } else {
            LatLng latLng = new LatLng(50, -100);
            cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 2);
         }
         map.moveCamera(cameraUpdate);
         locationRequestType=null;
         break;
      default:
         break;
      }
      locationClient.disconnect();
   }
   public void onDisconnected() {
   }

   // ==================================================================================================================||

   // ==================================================================================================================//
   // Methods Dealing with sub lists
   // ==================================================================================================================>>

   public void showListFragment(Fragment frag, int view, String tag) {
      if(!frag.isAdded()){
      FragmentTransaction fragmentTransaction = fragmentManager
            .beginTransaction();
      fragmentTransaction.add(view, frag, tag);
      fragmentTransaction.commit();
      }
      hideOtherGroups(view);
   }   

   public void hideListFragment(Fragment frag) {
      if(frag.isAdded()){
         
      FragmentTransaction fragmentTransaction = fragmentManager
            .beginTransaction();
      fragmentTransaction.remove(frag);
      fragmentTransaction.commit();
      }
      showAllGroups();
   }
   /**
    * This method gets called by the Reports LinearLayout during an onClick
    * event. If report list fragment is visible, destroy it; else create and
    * show it
    * 
    * @param view
    *           The view that calls the method
    */
   public void expandReports(View view) {
      if(view==null)
         view=findViewById(R.id.expandReportsFrameLayout);
      if (reportsFragment.isAdded()) {
         hideListFragment(reportsFragment);
         view.setBackgroundResource(transparentBackground);
      } else {
         showListFragment(reportsFragment, R.id.reportsList,REPORT_TAG);
         view.setBackgroundColor(getResources().getColor(R.color.BlackGrey));
      }
   }
   /**
    * This method gets called by the Unsent LinearLayout during an onClick event.
    * If unsent report list fragment is visible, destroy it; else create and show it
    * 
    * @param view
    *           The view that calls the method
    */
   public void expandUnsent(View view){
      if(view==null)
         view=findViewById(R.id.expandUnsentFrameLayout);
      if(unsentFragment.isAdded()){
         hideListFragment(unsentFragment);
         view.setBackgroundResource(transparentBackground);
      } else {
         showListFragment(unsentFragment, R.id.unsentList,UNSENT_TAG);
         view.setBackgroundColor(getResources().getColor(R.color.BlackGrey));
      }
   }
   

   /**
    * This method gets called by the Alerts LinearLayout during an onClick event.
    * If alert list fragment is visible, destroy it; else create and show it
    * 
    * @param view
    *           The view that calls the method
    */
   public void expandAlerts(View view) {
      if(view==null)
         view=findViewById(R.id.expandAlertsFrameLayout);
      if (alertsFragment.isAdded()) {
         hideListFragment(alertsFragment);
         view.setBackgroundResource(transparentBackground);
      } else {
         showListFragment(alertsFragment, R.id.alertsList,ALERT_TAG);
         view.setBackgroundColor(getResources().getColor(R.color.BlackGrey));
      }
   }
   
   /**
    * This method gets called by the Map Preferences LinearLayout during an onClick event.
    * If alert list fragment is visible, destroy it; else create and show it
    * 
    * @param view
    *           The view that calls the method
    */
   public void expandPrefs(View view) {
      if(view==null)
         view=findViewById(R.id.expandPrefsFrameLayout);
      if (prefsFragment.isAdded()) {
         hideListFragment(prefsFragment);
         //There appears to be a bug in the Android framework
         // that resets your xml attributes, i.e. padding
         // when setBackgroundResource method called
         // Solution: Set padding manually
         int topPadding = view.getPaddingTop();
         int sidePadding = view.getPaddingLeft();
         view.setBackgroundResource(transparentBackground);
         view.setPadding(sidePadding, topPadding, sidePadding, topPadding);
      } else {
         showListFragment(prefsFragment, R.id.prefsList,PREF_TAG);
         view.setBackgroundColor(getResources().getColor(R.color.BlackGrey));
      }
   }
   
   /**
    * Hides all 
    * @param id
    */
   private void hideOtherGroups(int id){
      showAllGroups();
      if(id!=R.id.alertsList)
         findViewById(R.id.alertGroup).setVisibility(View.GONE);
      if(id!=R.id.reportsList)
         findViewById(R.id.reportGroup).setVisibility(View.GONE);
      if(id!=R.id.unsentList)
         findViewById(R.id.unsentGroup).setVisibility(View.GONE);
      if(id!=R.id.prefsList)
         findViewById(R.id.prefGroup).setVisibility(View.GONE);
   }
   
   private void showAllGroups(){
         findViewById(R.id.alertGroup).setVisibility(View.VISIBLE);
         findViewById(R.id.reportGroup).setVisibility(View.VISIBLE);
         findViewById(R.id.unsentGroup).setVisibility(View.VISIBLE);
         findViewById(R.id.prefGroup).setVisibility(View.VISIBLE);
   }
   // ==================================================================================================================||

   

   // ==================================================================================================================//
   // Load Data from database and display on map if required
   // ==================================================================================================================>>

   /**
    * Draws the given point onto the map
    * 
    * @param point
    *           The point to draw to the map
    */
   private void drawMarker(LatLng point, int id) {
      int icon;
      ArrayList<Marker> markerList;
      switch (id) {
      case ALERTS_CURSOR:
         icon = R.drawable.grn_circle;
         markerList = alertMarkers;
         break;
      case UNSENT_CURSOR:
         icon = R.drawable.red_circle;
         markerList = unsentMarkers;
         break;
      default:
         icon = R.drawable.blue_circle;
         markerList = reportMarkers;
         break;
      }
      
      MarkerOptions markerOptions = new MarkerOptions();
      markerOptions.position(point).icon(
            BitmapDescriptorFactory.fromResource(icon));
      Marker marker = map.addMarker(markerOptions);
      markerList.add(marker);
   }
   
   /**
    * Remove all markers from the map that are in the given ArrayList
    * @param markerList the markers to remove from the map
    */
   private void clearMarkers(ArrayList<Marker> markerList){
      for(Marker m: markerList){
         m.remove();
      }
      markerList.clear();
   }

   public Loader<Cursor> onCreateLoader(int id, Bundle args) {
      String[] columns = new String[] { TableColumns.LATITUDE,
            TableColumns.LONGITUDE, TableColumns.CREATED_DATE };
      switch (id) {
      case REPORTS_CURSOR:
         return new CursorLoader(this, AlertsContentProvider.REPORTS_TABLE_URI,
               columns, null, null, null);
          case UNSENT_CURSOR:
             return new CursorLoader(this, AlertsContentProvider.UNSENT_TABLE_URI,columns,null,null,null);
      default:
         return new CursorLoader(this, AlertsContentProvider.ALERTS_TABLE_URI,
               columns, null, null, null);
      }
   }

   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
      int count = arg1.getCount();
      boolean showMarkers = true;
      switch (arg0.getId()) {
      case ALERTS_CURSOR:
         ((TextView) findViewById(R.id.alertsTextViewNaviDrawer))
               .setText("Alerts (" + count + ")");
         showMarkers = showAlerts;
         clearMarkers(alertMarkers);
         break;
      case REPORTS_CURSOR:
         ((TextView) findViewById(R.id.reportsTextViewNaviDrawer))
               .setText("Reports (" + count + ")");
         showMarkers = showReports;
         clearMarkers(reportMarkers);
         break;
      case UNSENT_CURSOR:
         ((TextView) findViewById(R.id.unsentTextViewNaviDrawer))
         .setText("Unsent Reports (" + count + ")");
         showMarkers = showUnsent;
         clearMarkers(unsentMarkers);
         break;
      }
      if (showMarkers) {
         double lat = 0;
         double lng = 0;
         while (arg1.moveToNext()) {
            lat = arg1.getDouble(arg1.getColumnIndex(ReporterColumns.LATITUDE));
            lng = arg1
                  .getDouble(arg1.getColumnIndex(ReporterColumns.LONGITUDE));
            LatLng point = new LatLng(lat, lng);
            drawMarker(point, arg0.getId());
         }
      }
   }

   public void onLoaderReset(Loader<Cursor> arg0) {
      // Since cursor doesn't bind with anything, don't need to do anything here.
   }
   
   // ==================================================================================================================||
   

   public void closeNavigationalDrawer() {
      mDrawerLayout.closeDrawers();
   }

   public void makeToast(String message){
      updateToast(message);
   }
  

   // ==================================================================================================================//
   // Register with GCM if required and inform user if Google Play Services disabled 
   // ==================================================================================================================>>
   
   private boolean checkPlayServices() {
      int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
      if(resultCode != ConnectionResult.SUCCESS) {
         if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
            GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION).show();
         } else {
            finish();
         }
         return false;
      }
      return true;
   }
   
   @Override
   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch(requestCode){
         case PLAY_SERVICE_RESOLUTION:
       //     if(resultCode==ConnectionResult.SUCCESS)  
            break;
      }
   }
   
   private void registerDeviceWithGCM() {
      if (checkPlayServices()) {
         String regId = getRegistrationId();
         if (regId.isEmpty()) {
            new RegisterInBackground().execute();
         }
      }
   }
   
   private String getRegistrationId(){
      String registrationId = pref.getString(ServerUtilities.REGISTRATION_ID, "");
      if(registrationId.isEmpty()){
         return "";
      }
      
      // Check if app was updated, if so register with GCM again
      // because Registration ID it may no longer be valid
      int registeredVersion = pref.getInt(APP_VERSION, Integer.MIN_VALUE);
      int currentVersion = getAppVersion(this);
      if (registeredVersion != currentVersion) {
          return "";
      }
      return registrationId;
   }
   
   /**
    * @return Current version of the application
    */
   private static int getAppVersion(Context context) {
       try {
           PackageInfo packageInfo = context.getPackageManager()
                   .getPackageInfo(context.getPackageName(), 0);
           return packageInfo.versionCode;
       } catch (NameNotFoundException e) {
           throw new RuntimeException("Could not get package name: " + e);
       }
   }
   
   private class RegisterInBackground extends AsyncTask<Void,Void,String> {

      @Override
      protected String doInBackground(Void... params) {
         String msg = "";
         try{
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getBaseContext());
            String regId = gcm.register(ServerUtilities.SENDER_ID);
            msg = "Device successfully registered with Google Cloud Messaging.";
            pref.edit().putString(ServerUtilities.REGISTRATION_ID, regId).commit();
            pref.edit().putInt(APP_VERSION, getAppVersion(getBaseContext())).commit();
            
         } catch (IOException e) {
            msg = "Error: "+e.getMessage()+". Please check your internet connection and try again later.";
         }   
         return msg;
      }
      
      @Override
      protected void onPostExecute(String result) {
      //   Toast.makeText(getBaseContext(), result, Toast.LENGTH_LONG).show();
      }
      
   }
   // ==================================================================================================================||

   
   
}
   
   



