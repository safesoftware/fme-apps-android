/*============================================================================= 
 
   Name     : ReportsListFragment.java
 
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


import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;
import fme.alerts.AlertsContentProvider;
import fme.alerts.AlertsContentProvider.ReporterColumns;



public class ReportsListFragment extends AbstractListFragment {
   SimpleCursorAdapter mAdapter;
  
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
      
      String[] columns =  new String[] {
            ReporterColumns._ID,
            ReporterColumns.TITLE,
            ReporterColumns.FIRST_NAME,
            ReporterColumns.LAST_NAME,
            ReporterColumns.EMAIL,
            ReporterColumns.SUBJECT,
            ReporterColumns.WEB_ADDRESS,
            ReporterColumns.DETAILS,
            ReporterColumns.LATITUDE,
            ReporterColumns.LONGITUDE,
            ReporterColumns.CREATED_DATE
            };
      return new CursorLoader(getActivity(),AlertsContentProvider.REPORTS_TABLE_URI,columns,null,null,null);
   }

   
   
  protected String[] getResultSet(Cursor cursor){
     String[] results = new String[11];
     results[0] = Long.toString(cursor.getLong(cursor.getColumnIndex(ReporterColumns._ID)));
     results[1] = cursor.getString(cursor.getColumnIndex(ReporterColumns.TITLE));
     results[2] = cursor.getString(cursor.getColumnIndex(ReporterColumns.FIRST_NAME));
     results[3] = cursor.getString(cursor.getColumnIndex(ReporterColumns.LAST_NAME));
     results[4] = cursor.getString(cursor.getColumnIndex(ReporterColumns.EMAIL));
     results[5] = cursor.getString(cursor.getColumnIndex(ReporterColumns.SUBJECT));
     results[6] = cursor.getString(cursor.getColumnIndex(ReporterColumns.WEB_ADDRESS));
     results[7] = cursor.getString(cursor.getColumnIndex(ReporterColumns.DETAILS));
     results[8] = cursor.getString(cursor.getColumnIndex(ReporterColumns.LATITUDE));
     results[9] = cursor.getString(cursor.getColumnIndex(ReporterColumns.LONGITUDE));
     results[10] = cursor.getString(cursor.getColumnIndex(ReporterColumns.CREATED_DATE));
     return results;
  }



@Override
protected void setMode() {
   this.mode = AbstractListFragment.REPORT;
   
}



@Override
protected void setTableUri() {
   this.tableUri = AlertsContentProvider.REPORTS_TABLE_URI;
   
}


}
