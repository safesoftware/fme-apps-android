/*============================================================================= 
 
   Name     : MainActivity.java
 
   System   : FME Reporter
 
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

package fme.reporter;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import fme.common.SettingsMenu;

public class MainActivity extends FragmentActivity implements
		ConnectionCallbacks, OnConnectionFailedListener,
		OnMyLocationButtonClickListener, LocationListener {

	private static final String BUNDLE_MARKERS = "bundleMarkers";
	private static final String MARKER_LIST = "markerList";
	private static final String IS_MAP = "isMap";

	private ArrayList<MarkerOptions> markers = new ArrayList<MarkerOptions>();

	private GoogleMap mMap;
	private LocationClient mLocationClient;

	private Button sendButton;

	private EditText firstNameField;
	private EditText lastNameField;
	private EditText emailField;
	private EditText titleField;
	private EditText webAddressField;
	private EditText detailsField;

	private ViewSwitcher switcher;

	private static final LocationRequest REQUEST = LocationRequest.create()
			.setInterval(5000) // 5 seconds
			.setFastestInterval(16) // 16ms = 60fps
			.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		switcher = (ViewSwitcher) findViewById(R.id.switcher);

		if (setUpMapIfNeeded()) {
			if (savedInstanceState == null) {

				Location lastKnownLocation = FMEReporterApplication
						.getSuperInstance().getLastKnownLocation();
				CameraUpdate cameraUpdate;
				if (null != lastKnownLocation) {
					LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),
							lastKnownLocation.getLongitude());
					cameraUpdate = CameraUpdateFactory
							.newLatLngZoom(latLng, 14);
				} else {
					LatLng latLng = new LatLng(50, -100);
					cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 2);
				}
				mMap.moveCamera(cameraUpdate);
			} else {
				markers = savedInstanceState.getBundle(BUNDLE_MARKERS)
						.getParcelableArrayList(MARKER_LIST);
				for (MarkerOptions marker : markers) {
					mMap.addMarker(marker);
				}
			}
		}

		sendButton = (Button) findViewById(R.id.SendButton2);
		// Register handler for UI elements
		sendButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				onSendClick(sendButton);
			}
		});
		FMEReporterApplication.getInstance().updateMainActivityHandle(this);

		firstNameField = (EditText) findViewById(R.id.firstNameText);
		lastNameField = (EditText) findViewById(R.id.lastNameText);
		emailField = (EditText) findViewById(R.id.emailText);
		titleField = (EditText) findViewById(R.id.subjectText);
		webAddressField = (EditText) findViewById(R.id.webAddressText);
		detailsField = (EditText) findViewById(R.id.detailsText);

		final SharedPreferences pref = PreferenceManager
				.getDefaultSharedPreferences(this);

		firstNameField
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							EditText e = (EditText) v;
							SharedPreferences.Editor edit = pref.edit();
							edit.putString(
									getResources().getString(
											R.string.firstNameLabel), e
											.getText().toString());
							edit.commit();
						}
					}
				});

		lastNameField
				.setOnFocusChangeListener(new View.OnFocusChangeListener() {
					public void onFocusChange(View v, boolean hasFocus) {
						if (!hasFocus) {
							EditText e = (EditText) v;
							SharedPreferences.Editor edit = pref.edit();
							edit.putString(
									getResources().getString(
											R.string.lastNameLabel), e
											.getText().toString());
							edit.commit();
						}
					}
				});

		emailField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					EditText e = (EditText) v;
					SharedPreferences.Editor edit = pref.edit();
					edit.putString(getResources()
							.getString(R.string.emailLabel), e.getText()
							.toString());
					edit.commit();
				}
			}
		});

		titleField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					EditText e = (EditText) v;
					SharedPreferences.Editor edit = pref.edit();
					edit.putString(
							getResources().getString(R.string.subjectLabel), e
									.getText().toString());
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
									getResources().getString(
											R.string.webAddressLabel), e
											.getText().toString());
							edit.commit();
						}
					}
				});

		detailsField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (!hasFocus) {
					EditText e = (EditText) v;
					SharedPreferences.Editor edit = pref.edit();
					edit.putString(
							getResources().getString(R.string.detailsLabel), e
									.getText().toString());
					edit.commit();
				}
			}
		});

	}

	protected void launchClearPins() {
		AlertDialog alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(R.string.ClearAllPins);
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(R.string.ClearAllPins)
				.setCancelable(true)
				.setPositiveButton("Remove All Pins",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								mMap.clear();

							}
						})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
		AlertDialog alert = builder.create();
		alert.show();

	}

	@Override
	public void onPause() {
		super.onPause();
		setUpMapIfNeeded();
		if (mLocationClient != null) {
			mLocationClient.disconnect();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		// Uncomment to enable location tracker, which will move camera to
		// current location
		// based on time/distance interval specified in REQUEST
		// mLocationClient.connect();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		String firstName = prefs.getString(
				getResources().getString(R.string.firstNameLabel), null);
		String lastName = prefs.getString(
				getResources().getString(R.string.lastNameLabel), null);
		String email = prefs.getString(
				getResources().getString(R.string.emailLabel), null);
		String subject = prefs.getString(
				getResources().getString(R.string.subjectLabel), null);
		String webAddress = prefs.getString(
				getResources().getString(R.string.webAddressLabel), null);
		String details = prefs.getString(
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

	public void onSendClick(View view) {
		//Do the following to ensure all messages get saved before a report event,
		//even in they are currently in focus
		view.setFocusable(true);
		view.setFocusableInTouchMode(true);
		view.requestFocus();
		view.setFocusable(false);
		view.setFocusableInTouchMode(false);

		boolean success = FMEReporterApplication.getInstance().sendMessage(); // Basic
																				// error
																				// checking
		if (!success) {
			insufficientInfoDialog();
		}

	}

	/**
	 * Launches the SettingsMenu activity to modify the global settings
	 */

	protected void launchSettings() {
		SettingsMenu.startActivity(this, FMEReporterApplication.getInstance());
	}

	protected boolean isRouteDisplayed() {
		return false;
	}

	public GoogleMap getMap() {
		return mMap;
	}

	private void insufficientInfoDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.insufficient_server_info)
				.setMessage(R.string.insufficient_info_details)
				.setCancelable(true)
				.setPositiveButton("Close",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// do things
							}
						});
		AlertDialog alert = builder.create();
		alert.show();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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
				switcher.postDelayed(new Runnable() {
					public void run() {
						InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(switcher.getWindowToken(),
								0);
					}
				}, 50);
			}
			return true;
		case 2:
			Log.d("Alerts", "Settings button clicked.");
			launchSettings();
			return true;

		case 1:
			Log.d("Alerts", "Clear alerts clicked.");
			launchClearPins();
			return true;
		}

		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		createMenu(menu);

		return true;
	}

	private void createMenu(Menu menu) {
		// Create Clear All menu item
		MenuItem item2 = menu.add(0, 1, 1, "Delete");
		item2.setIcon(android.R.drawable.ic_menu_delete);
		item2.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);

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

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt(IS_MAP, switcher.getDisplayedChild());
		// outState.putParcelable("markers", markers.get(0));
		Bundle markerBundle = new Bundle();
		markerBundle.putParcelableArrayList(MARKER_LIST, markers);
		outState.putBundle(BUNDLE_MARKERS, markerBundle);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		int n = savedInstanceState.getInt(IS_MAP);
		if (n == 1)
			switcher.showNext();

	}

	/**
	 * Return false if map is null; true otherwise.
	 * 
	 */
	private boolean setUpMapIfNeeded() {
		// Do a null check to confirm that we have not already instantiated the
		// map.
		if (mMap == null) {
			// Try to obtain the map from the SupportMapFragment.
			mMap = ((SupportMapFragment) getSupportFragmentManager()
					.findFragmentById(R.id.map)).getMap();
			// Check if we were successful in obtaining the map.
			if (mMap != null) {
			   mMap.getUiSettings().setZoomControlsEnabled(false);
				mMap.setMyLocationEnabled(true);
				mMap.setOnMyLocationButtonClickListener(this);
			}
			return true;
		} else
			return false;
	}

	private void setUpLocationClientIfNeeded() {
		if (mLocationClient == null) {
			mLocationClient = new LocationClient(getApplicationContext(), this, // ConnectionCallbacks
					this); // OnConnectionFailedListener
		}
	}

	public boolean onMyLocationButtonClick() {
		// TODO Auto-generated method stub
		return false;
	}

	public void onLocationChanged(Location location) {
		LatLng latLng = new LatLng(location.getLatitude(),
				location.getLongitude());
		CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLng(latLng);
		mMap.animateCamera(cameraUpdate);

	}

	public void onConnectionFailed(ConnectionResult arg0) {
		// TODO Auto-generated method stub

	}

	public void onConnected(Bundle arg0) {
		mLocationClient.requestLocationUpdates(REQUEST, this);

	}

	public void onDisconnected() {
		// TODO Auto-generated method stub

	}

	public void addMarkerToList(MarkerOptions point) {
		markers.add(point);
	}

}
