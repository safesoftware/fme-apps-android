/*============================================================================= 
 
   Name     : AlertsListFragment.java
 
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

import fme.alerts.AlertsContentProvider;
import fme.alerts.AlertsContentProvider.AlertColumns;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;

public class AlertsListFragment extends AbstractListFragment {
   
   @Override
   public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
      String[] columns =  new String[] {
            AlertColumns._ID,
            AlertColumns.TITLE,
            AlertColumns.DESCRIPTION,
            AlertColumns.LATITUDE,
            AlertColumns.LONGITUDE,
            AlertColumns.CREATED_DATE
            };
      return new CursorLoader(getActivity(),AlertsContentProvider.ALERTS_TABLE_URI,columns,null,null,null);
   }

   @Override
   protected String[] getResultSet(Cursor cursor) {
      String[] results = new String[6];
      results[0] = Long.toString(cursor.getLong(cursor.getColumnIndex(AlertColumns._ID)));
      results[1] = cursor.getString(cursor.getColumnIndex(AlertColumns.TITLE));
      results[2] = cursor.getString(cursor.getColumnIndex(AlertColumns.DESCRIPTION));
      results[3] = cursor.getString(cursor.getColumnIndex(AlertColumns.LATITUDE));
      results[4] = cursor.getString(cursor.getColumnIndex(AlertColumns.LONGITUDE));
      results[5] = cursor.getString(cursor.getColumnIndex(AlertColumns.CREATED_DATE));
      return results;
   }

   @Override
   protected void setMode() {
      this.mode = AbstractListFragment.ALERT;
   }

   @Override
   protected void setTableUri() {
     this.tableUri = AlertsContentProvider.ALERTS_TABLE_URI;
   }
   


}
