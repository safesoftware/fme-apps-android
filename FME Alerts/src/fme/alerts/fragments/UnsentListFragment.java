/*============================================================================= 
 
   Name     : UnsentListFragment.java
 
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import fme.alerts.AlertsContentProvider;
import fme.alerts.FMEAlertsApplication;
import fme.alerts.R;
import fme.alerts.AlertsContentProvider.TableColumns;
import fme.alerts.AlertsContentProvider.UnsentColumns;
import fme.alerts.fragments.AbstractListFragment.MySimpleCursorAdapter;
import fme.common.SettingsMenu;

public class UnsentListFragment extends AbstractListFragment {
   
   private static final String[] columns =
         new String[] {
      UnsentColumns._ID,
      UnsentColumns.TITLE,
      UnsentColumns.FIRST_NAME,
      UnsentColumns.LAST_NAME,
      UnsentColumns.EMAIL,
      UnsentColumns.SUBJECT,
      UnsentColumns.WEB_ADDRESS,
      UnsentColumns.DETAILS,
      UnsentColumns.LATITUDE,
      UnsentColumns.LONGITUDE,
      UnsentColumns.CREATED_DATE,
      UnsentColumns.USERNAME,
      UnsentColumns.POST_URL,
      UnsentColumns.POST_BODY
      };
   @Override
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
      return new CursorLoader(getActivity(),AlertsContentProvider.UNSENT_TABLE_URI,columns,null,null,null);
   }

   @Override
   protected String[] getResultSet(Cursor cursor) {
      String[] results = new String[14];
      results[0] = Long.toString(cursor.getLong(cursor.getColumnIndex(UnsentColumns._ID)));
      results[1] = cursor.getString(cursor.getColumnIndex(UnsentColumns.TITLE));
      results[2] = cursor.getString(cursor.getColumnIndex(UnsentColumns.FIRST_NAME));
      results[3] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LAST_NAME));
      results[4] = cursor.getString(cursor.getColumnIndex(UnsentColumns.EMAIL));
      results[5] = cursor.getString(cursor.getColumnIndex(UnsentColumns.SUBJECT));
      results[6] = cursor.getString(cursor.getColumnIndex(UnsentColumns.WEB_ADDRESS));
      results[7] = cursor.getString(cursor.getColumnIndex(UnsentColumns.DETAILS));
      results[8] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LATITUDE));
      results[9] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LONGITUDE));
      results[10] = cursor.getString(cursor.getColumnIndex(UnsentColumns.CREATED_DATE));
      results[11] = cursor.getString(cursor.getColumnIndex(UnsentColumns.USERNAME));
      results[12] = cursor.getString(cursor.getColumnIndex(UnsentColumns.POST_URL));
      results[13] = cursor.getString(cursor.getColumnIndex(UnsentColumns.POST_BODY));
      return results;
   }

  
   
   @Override
   protected void setMode() {
      this.mode = AbstractListFragment.UNSENT;
   }

   @Override
   protected void setTableUri() {
     this.tableUri = AlertsContentProvider.UNSENT_TABLE_URI;
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
         Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.custom_listview_unsent, container, false);
      view.findViewById(R.id.syncAllLayout).setOnClickListener(new View.OnClickListener() {
         public void onClick(View v) {
            final Cursor cursor = getActivity().getContentResolver().query(AlertsContentProvider.UNSENT_TABLE_URI, columns, null, null, null);
            final int n = cursor.getCount();
            String extra = "";
            if(!PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(SettingsMenu.STORE_UNSENT_REPORTS,true))
               extra = " \nWarning: If there is a network failure your unsent reports will be lost. Turn on 'Store Unsent Reports' to avoid this.";
            AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
            .setMessage("Send "+n+" unsent reports to the server?"+extra)
            .setPositiveButton("Yes", new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface arg0, int arg1) {
                  while(cursor.moveToNext()){
                     String[] results2 = getResultsForReSync(cursor);
                     FMEAlertsApplication.getInstance().createSpecificTaskType().execute(results2);
                     getActivity().getContentResolver().delete(AlertsContentProvider.UNSENT_TABLE_URI, TableColumns._ID+"="+results2[0], null);
                     }
                     mainActivity.makeToast( n+" unsent reports resent to the server");
               }
            })
            .setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
               public void onClick(DialogInterface dialog, int which) {
                  // Do nothing
               }
            }).create();
            alertDialog.show();
         }
      });
      return view;
   }
   
   private class UnsentSimpleCursorAdapter extends MySimpleCursorAdapter {

      public UnsentSimpleCursorAdapter(Context context, int layout, Cursor c,
            String[] from, int[] to, int flags) {
         super(context, layout, c, from, to, flags);
      }
      
      
      @Override
      public void bindView(View view, Context context, Cursor cursor) {
         super.bindView(view, context, cursor);
         
            view.findViewById(fme.alerts.R.id.vertical_divider_unsent).setVisibility(View.VISIBLE);
            ImageButton reloadButton = (ImageButton) view.findViewById(fme.alerts.R.id.reloadUnsent);
            reloadButton.setVisibility(View.VISIBLE);
            final String[] results2 = getResultsForReSync(cursor);
            final long id = cursor.getLong(cursor.getColumnIndex(TableColumns._ID));
            reloadButton.setOnClickListener(new View.OnClickListener() {
               
               public void onClick(View v) {
                  FMEAlertsApplication.getInstance().createSpecificTaskType().execute(results2);
                  int n = delete(id);
                  mainActivity.makeToast(n+" "+mode.toLowerCase()+" (#"+id+") resent to server");
               }
            });
      }
      
   }
   
   @Override
   protected SimpleCursorAdapter createSimpleCursorAdapter(){
      return new UnsentSimpleCursorAdapter(getActivity(), R.layout.report_row, null,FROM,TO,0);
   }
   
   private String[] getResultsForReSync(Cursor cursor) {
      String[] results = new String[15];
      results[0] = Long.toString(cursor.getLong(cursor.getColumnIndex(UnsentColumns._ID)));
      results[1] = cursor.getString(cursor.getColumnIndex(UnsentColumns.POST_URL));
      results[2] = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
            getResources().getString(R.string.username), null);
      results[3] = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString(
            getResources().getString(R.string.password), null);
      results[4] = cursor.getString(cursor.getColumnIndex(UnsentColumns.POST_BODY));
      results[5] = cursor.getString(cursor.getColumnIndex(UnsentColumns.TITLE));
      results[6] = cursor.getString(cursor.getColumnIndex(UnsentColumns.FIRST_NAME));
      results[7] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LAST_NAME));
      results[8] = cursor.getString(cursor.getColumnIndex(UnsentColumns.EMAIL));
      results[9] = cursor.getString(cursor.getColumnIndex(UnsentColumns.SUBJECT));
      results[10] = cursor.getString(cursor.getColumnIndex(UnsentColumns.WEB_ADDRESS));
      results[11] = cursor.getString(cursor.getColumnIndex(UnsentColumns.DETAILS));
      results[12] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LATITUDE));
      results[13] = cursor.getString(cursor.getColumnIndex(UnsentColumns.LONGITUDE));
      results[14] = cursor.getString(cursor.getColumnIndex(UnsentColumns.CREATED_DATE));
      return results;
   }
   
}

