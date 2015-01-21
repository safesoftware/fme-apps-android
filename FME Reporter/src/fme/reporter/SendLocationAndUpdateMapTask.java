/*============================================================================= 
 
   Name     : SendLocationAndUpdateMapTask.java
 
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

import java.text.DateFormat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.graphics.Point;
import android.location.Location;
import android.os.Handler;
import android.os.SystemClock;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;


import fme.common.PostLocationTask;

public class SendLocationAndUpdateMapTask extends PostLocationTask {
	GoogleMap map;
	MainActivity mainActivity;

	public SendLocationAndUpdateMapTask() {
		super();
	}

	@Override
	protected void onPostExecute(String[] results) {
	   String result = results[0];
		FMEReporterApplication main = FMEReporterApplication.getInstance();
		mainActivity = main.getMainActivityHandle();
		Location lastKnownLocation = FMEReporterApplication.getSuperInstance()
				.getLastKnownLocation();
		if (null != lastKnownLocation) {

			String timestamp = DateFormat.getDateTimeInstance().format(
					System.currentTimeMillis());
			LatLng endLatLng = new LatLng(lastKnownLocation.getLatitude(),
					lastKnownLocation.getLongitude());

			map = main.getMainActivityHandle().getMap();
			Projection projection = map.getProjection();
			LatLng startLatLng = projection.fromScreenLocation(new Point(0, 0));
			MarkerOptions point = new MarkerOptions().position(startLatLng);

			if ("" == result) // If result has a value, it means failure
			{
				point.title(FMEReporterApplication.getInstance().getResources()
						.getString(R.string.LocationReported));
				point.snippet(timestamp);
				point.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.grn_circle));

			} else {
				// Show the user what went wrong (in a toast pop-up)
				String errorMsg = "Unable to report location to server: "
						+ result;
				Toast.makeText(main.getApplicationContext(), errorMsg,
						Toast.LENGTH_LONG).show();
				point.title(FMEReporterApplication.getInstance().getResources()
						.getString(R.string.UnableToReport));
				point.snippet(result);
				point.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.red_circle));
			}

			Marker marker = map.addMarker(point);
			animateMarker(marker, endLatLng, point);
		}
	}

	// Method taken from StackOverFlow (Author: GrlsHu) and edited by Navpreet
	/**
	 * Make the marker fly towards toPosition on map. Sets the position of the
	 * MarkerOption point to toPosition.
	 * 
	 * @param marker
	 * @param toPosition
	 * @param point
	 */
	public void animateMarker(final Marker marker, final LatLng toPosition,
			final MarkerOptions point) {
		final Handler handler = new Handler();
		final long start = SystemClock.uptimeMillis();
		Projection proj = map.getProjection();
		Point startPoint = proj.toScreenLocation(marker.getPosition());
		final LatLng startLatLng = proj.fromScreenLocation(startPoint);
		final long duration = 500;
		final Interpolator interpolator = new LinearInterpolator();
		handler.post(new Runnable() {
			public void run() {
				long elapsed = SystemClock.uptimeMillis() - start;
				float t = interpolator.getInterpolation((float) elapsed
						/ duration);
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
					marker.setVisible(true);
					mainActivity.addMarkerToList(point);
				}
			}
		});
		map.animateCamera(CameraUpdateFactory.newLatLng(toPosition));
	}

}
