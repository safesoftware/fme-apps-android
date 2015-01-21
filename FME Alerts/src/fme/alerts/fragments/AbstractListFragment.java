/*============================================================================= 
 
   Name     : AbstractListFragment.java
 
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

package fme.alerts.fragments;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.SimpleCursorAdapter.ViewBinder;
import android.widget.TextView;
import fme.alerts.AlertsContentProvider.TableColumns;
import fme.alerts.AlertDetails;
import fme.alerts.FMEAlertsApplication;
import fme.alerts.R;



public abstract class AbstractListFragment extends ListFragment implements LoaderManager.LoaderCallbacks<Cursor> {
   SimpleCursorAdapter mAdapter;
   protected static final String REPORT = "Report";
   protected static final String UNSENT = "Unsent Report";
   protected static final String ALERT = "Alert";
   
   static final String[] FROM = new String[] { TableColumns.TITLE,TableColumns.CREATED_DATE };
   static final int[] TO = new int[] { R.id.reportTitleText,R.id.reportTitleTime };
   
   protected String mode;
   protected Uri tableUri;
   protected abstract void setMode();
   protected abstract void setTableUri();
   
   protected OnCloseDrawerListener mainActivity;
   
   public AbstractListFragment(){
      setMode();
      setTableUri();
   }
   
   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      ViewGroup view  = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
      ViewGroup listView = ((ViewGroup)view.findViewById(android.R.id.list));
      if(listView!=null)
         listView.setMotionEventSplittingEnabled(false);
      return view; 
   }
   
   
   public interface OnCloseDrawerListener{
      
      /**
       * Interface method used by fragments to close the navigation drawer
       */
      public void closeNavigationalDrawer();
      
      /**
       * Use this method to make a toast.
       */
      public void makeToast(String message);
   }
   
   @Override
   public void onAttach(Activity activity) {
      super.onAttach(activity);
      try{
      mainActivity = (OnCloseDrawerListener) activity;
      } catch(ClassCastException e) {
         throw new ClassCastException(activity.toString()
               + " must implement OnCloseDrawerListener");
      }
   }
   
   @Override
   public void onActivityCreated(Bundle savedInstanceState) {
      super.onActivityCreated(savedInstanceState);
      mAdapter = createSimpleCursorAdapter();
      
      mAdapter.setViewBinder(new MyViewBinder());
      
      setListAdapter(mAdapter);
      getLoaderManager().initLoader(0, null, this);
   }
   
   protected SimpleCursorAdapter createSimpleCursorAdapter(){
      return new MySimpleCursorAdapter(getActivity(), R.layout.report_row, null,FROM,TO,0);
   }
   
   
 //================================================================================================//
 // Cursor Loader Methods
 //================================================================================================>>   
   public abstract Loader<Cursor> onCreateLoader(int arg0, Bundle arg1);

   public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
      mAdapter.swapCursor(arg1); 
      
   }

   public void onLoaderReset(Loader<Cursor> arg0) {
      mAdapter.swapCursor(null);

   }
 //================================================================================================||
   
   private class MyViewBinder implements ViewBinder {

      public boolean setViewValue(View aView, Cursor aCursor, int aColumnIndex) {
         

               if (aColumnIndex == aCursor.getColumnIndex(TableColumns.CREATED_DATE)) { // R.id.date
                  long date = aCursor.getLong(aColumnIndex);
                  CharSequence relativeFormat = "";
                  try {
                     Time now = new Time();
                     now.setToNow();
                     long nowTime = now.toMillis(false);
                     relativeFormat = DateUtils.getRelativeTimeSpanString(
                           date, nowTime, 0,
                           DateUtils.FORMAT_ABBREV_RELATIVE);
                  } catch (Exception e) {
                     Log.e("NumberFormatting", e.getMessage());
                  }
                  TextView textView = (TextView) aView;
                  textView.setText(relativeFormat);
                  return true;
               }
               return false;
            }
   }
   
   protected class MySimpleCursorAdapter extends SimpleCursorAdapter {

      public MySimpleCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to, int flags) {
         super(context, layout, c, from, to, flags);
      }
 

      
      @Override
      public void bindView(View view, Context context, Cursor cursor) {
         super.bindView(view, context, cursor);
         ImageButton deleteButton = (ImageButton) view.findViewById(R.id.reportDeleteButton);
         final long id = cursor.getLong(cursor.getColumnIndex(TableColumns._ID));
         deleteButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("promtuser", true))
                  createDeleteDialog(id);
               else
                delete(id);
            }
         });
         
         
         final String[] results = getResultSet(cursor);
         LinearLayout textView = (LinearLayout) view.findViewById(R.id.reportTitle);
         textView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
               Intent i = new Intent(getActivity(), AlertDetails.class);
               i.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
               i.putExtra("mode",mode);
               i.putExtra("results", results);
               startActivity(i);
            }
         });
         
         final GoogleMap mMap = FMEAlertsApplication.getInstance().getMainActivityHandle().getMap();
         final LatLng location = new LatLng(cursor.getDouble(cursor.getColumnIndex(TableColumns.LATITUDE)),
               cursor.getDouble(cursor.getColumnIndex(TableColumns.LONGITUDE)));
         ImageButton locateReport = (ImageButton) view.findViewById(R.id.locateMarker);
         locateReport.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {
               mMap.animateCamera(CameraUpdateFactory.newLatLng(location));
               if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean("autoclosenavidrawer",true))
                  mainActivity.closeNavigationalDrawer();
            }
         });
     }
   }
   
   int delete(long id){
      return getActivity().getContentResolver()
            .delete(tableUri, TableColumns._ID+"="+id, null);
   }
    
  protected abstract String[] getResultSet(Cursor cursor);
  
  private void createDeleteDialog(final long id){
     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
     builder
     .setMessage("Delete 1 "+mode)
     .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
           int n = delete(id);
           if(n>0)
              mainActivity.makeToast(n+" "+mode.toLowerCase()+" (#"+id+") deleted");
        }
     })
     .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
           // Don't do anything
        }
     });
     builder.create().show();
  }
}
