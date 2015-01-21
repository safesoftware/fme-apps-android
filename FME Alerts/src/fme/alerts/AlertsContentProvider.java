/*============================================================================= 
 
   Name     : AlertsContentProvider.java
 
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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class AlertsContentProvider extends ContentProvider {

   public static final String PROVIDER_NAME = "fme.alerts.provider";
   /**
    * URI used to access reporter table
    */
   public static final Uri REPORTS_TABLE_URI = Uri.parse("content://"
         + PROVIDER_NAME + "/reports");
   /**
    * URI used to access alerts table
    */
   public static final Uri ALERTS_TABLE_URI = Uri.parse("content://"
         + PROVIDER_NAME + "/alerts");
   /**
    * URI used to access unsent table
    */
   public static final Uri UNSENT_TABLE_URI = Uri.parse("content://"
         + PROVIDER_NAME + "/unsent"); 
   

   
   public static class TableColumns implements BaseColumns {
      /**
       * The title of the alert.
       * This is actually the topic reported/subscribed to.
       * <P>
       * <strong><strong>SQLite</strong></strong> DataType: TEXT <br/>
       * <strong><strong>Java</strong></strong> DataType: String
       * </P>
       */
      public static final String TITLE = "title";
      /**
       * The time stamp for when the report was received
       * <P>
       * <strong>SQLite</strong> DataType: TEST <br>
       * <strong>Java</strong> DataType: String
       * <strong>Java</strong> DataType: Long
       * </P>
       */
      public static final String CREATED_DATE = "date";

      /**
       * The report location latitude
       * <P>
       * <strong>SQLite</strong> DataType: REAL <br>
       * <strong>Java</strong> DataType: Double
       * </P>
       */
      public static final String LATITUDE = "lat";

      /**
       * The report location longitude
       * <P>
       * <strong>SQLite</strong> DataType: REAL <br>
       * <strong>Java</strong> DataType: Double
       * </P>
       */
      public static final String LONGITUDE = "lon";
   }
   
   
   
   
   // ======================================================================================//
   // Columns of Alerts Table
   // ======================================================================================>>

   public static class AlertColumns extends TableColumns {
      /**
       * The description itself
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String DESCRIPTION = "description";
   }
   // =======================================================================================||

   // ======================================================================================//
   // Columns of Reports Table
   // ======================================================================================>>

   /**
    * Names of the columns used in the Reports database table.
    * 
    * 
    */
   public static class ReporterColumns extends TableColumns {
      /**
       * First name (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String FIRST_NAME = "fname";
      /**
       * Last name (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String LAST_NAME = "lname";
      /**
       * Email (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String EMAIL = "email";
      /**
       * Message Subject (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String SUBJECT = "subject";
      /**
       * Web address (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String WEB_ADDRESS = "webaddress";
      /**
       * Message Details (from message written by user)
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String DETAILS = "details";
   }
   // =======================================================================================||
   
   
   // ======================================================================================//
   // Columns of Unsent Reports Table
   // ======================================================================================>>

   public static class UnsentColumns extends ReporterColumns {
      /**
       * The URL to send the post request to
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String POST_URL = "post_url";
      /**
       * The username associated with this post request.
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String USERNAME = "post_username";
      /**
       * The HTML Post request's body to be sent to the server
       * <P>
       * <strong>SQLite</strong> DataType: TEXT <br>
       * <strong>Java</strong> DataType: String
       * </P>
       */
      public static final String POST_BODY = "post_message";
   }
   // =======================================================================================||

   

   // ======================================================================================//
   // URI matcher: matches URI with table, or row
   // ======================================================================================>>
   static final int REPORTS = 1;
   static final int REPORTS_ID = 2;
   static final int ALERTS = 3;
   static final int ALERTS_ID = 4;
   static final int UNSENT = 5;

   static final UriMatcher uriMatcher;
   static {
      uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
      uriMatcher.addURI(PROVIDER_NAME, "reports", REPORTS);
      uriMatcher.addURI(PROVIDER_NAME, "reports/#", REPORTS_ID);
      uriMatcher.addURI(PROVIDER_NAME, "alerts", ALERTS);
      uriMatcher.addURI(PROVIDER_NAME, "alerts/#", ALERTS_ID);
      uriMatcher.addURI(PROVIDER_NAME, "unsent", UNSENT);
   }
   // =======================================================================================||

   // ======================================================================================//
   // Database specific constant declarations
   // ======================================================================================>>
   private SQLiteDatabase db;
   private static final String DATABASE_NAME = "FME";
   private static final String REPORTS_TABLE_NAME = "reports";
   private static final String ALERTS_TABLE_NAME = "alerts";
   private  static final String UNSENT_TABLE_NAME = "unset";
   private static final int DATABASE_VERSION = 9;
   private static final String CREATE_REPORTS_TABLE = 
         "CREATE TABLE IF NOT EXISTS " + REPORTS_TABLE_NAME + " ("
         + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
         + ReporterColumns.TITLE + " TEXT,"
         // + ReporterColumns.DESCRIPTION+ " TEXT,"
         + ReporterColumns.CREATED_DATE + " TEXT,"
         + ReporterColumns.LATITUDE + " REAL,"
         + ReporterColumns.LONGITUDE + " REAL, "
         + ReporterColumns.FIRST_NAME + " TEXT, "
         + ReporterColumns.LAST_NAME + " TEXT, "
         + ReporterColumns.EMAIL + " TEXT, "
         + ReporterColumns.SUBJECT + " TEXT, "
         + ReporterColumns.WEB_ADDRESS + " TEXT, "
         + ReporterColumns.DETAILS + " TEXT "+ ");";
    //     + ReporterColumns.TOPIC + " TEXT " + ");";
   
   private static final String CREATE_UNSENT_TABLE = 
         "CREATE TABLE IF NOT EXISTS " + UNSENT_TABLE_NAME + " ("
         + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
         + UnsentColumns.TITLE + " TEXT,"
         + UnsentColumns.CREATED_DATE + " TEXT,"
         + UnsentColumns.LATITUDE + " REAL,"
         + UnsentColumns.LONGITUDE + " REAL, "
         + UnsentColumns.FIRST_NAME + " TEXT, "
         + UnsentColumns.LAST_NAME + " TEXT, "
         + UnsentColumns.EMAIL + " TEXT, "
         + UnsentColumns.SUBJECT + " TEXT, "
         + UnsentColumns.WEB_ADDRESS + " TEXT, "
         + UnsentColumns.DETAILS + " TEXT, "
         + UnsentColumns.POST_URL+ " TEXT, "
         + UnsentColumns.USERNAME+ " TEXT, "
         + UnsentColumns.POST_BODY + " TEXT " + ");";

   private static final String CREATE_ALERTS_TABLE = 
         "CREATE TABLE IF NOT EXISTS "+ ALERTS_TABLE_NAME + " (" 
         + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
         + AlertColumns.TITLE + " TEXT," 
         + AlertColumns.DESCRIPTION + " TEXT,"
         + AlertColumns.CREATED_DATE + " TEXT," 
         + AlertColumns.LATITUDE + " REAL," 
         + AlertColumns.LONGITUDE + " REAL" + ");";
   
   

   // =======================================================================================||

   // ======================================================================================>>
   
   /**
    * Helper class that actually creates and manages the provider's underlying
    * data repository.
    */
   private static class DatabaseHelper extends SQLiteOpenHelper {
      DatabaseHelper(Context context) {
         super(context, DATABASE_NAME, null, DATABASE_VERSION);
      }

      @Override
      public void onCreate(SQLiteDatabase db) {
         db.execSQL(CREATE_REPORTS_TABLE);
         db.execSQL(CREATE_ALERTS_TABLE);
         db.execSQL(CREATE_UNSENT_TABLE);
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
         db.execSQL("DROP TABLE IF EXISTS " + REPORTS_TABLE_NAME);
         db.execSQL("DROP TABLE IF EXISTS " + ALERTS_TABLE_NAME);
         db.execSQL("DROP TABLE IF EXISTS " + UNSENT_TABLE_NAME);
  //       FMEAlertsApplication.getInstance().deleteDatabase("FMEAlerts");
         onCreate(db);
      }
   }

   // =======================================================================================||

   @Override
   public boolean onCreate() {
      Context context = getContext();

      DatabaseHelper dbHelper = new DatabaseHelper(context);
      // DataBase will get created automatically if it doesn't exist
      db = dbHelper.getWritableDatabase();
      return db != null;
   }

   @Override
   public int delete(Uri uri, String selection, String[] selectionArgs) {
      int count = 0;

      switch (uriMatcher.match(uri)) {
      case UNSENT:
         count = db.delete(UNSENT_TABLE_NAME, selection, selectionArgs);
         break;
      
      case REPORTS:
         count = db.delete(REPORTS_TABLE_NAME, selection, selectionArgs);
         break;
      case REPORTS_ID:
         String id = uri.getPathSegments().get(1);
         count = db.delete(REPORTS_TABLE_NAME, ReporterColumns._ID
               + " = "
               + id
               + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ')'
                     : ""), selectionArgs);
         break;
      case ALERTS:
         count = db.delete(ALERTS_TABLE_NAME, selection, selectionArgs);
         break;

      default:
         throw new IllegalArgumentException("Unknown URI " + uri);
      }

      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

   @Override
   public String getType(Uri uri) {
      switch (uriMatcher.match(uri)) {
      /**
       * Get all reports
       */
      case REPORTS:
         return "vnd.android.cursor.dir/vnd.reports";
         /**
          * Get a particular report
          */
      case REPORTS_ID:
         return "vnd.android.cursor.item/vnd.reports";
         /**
          * Get all alerts
          */
      case ALERTS:
         return "vnd.android.cursor.dir/vnd.alerts";
         /**
          * Get all unsent reports
          */
      case UNSENT:
         return "vnd.android.cursor.dir/vnd.unsent";
      default:
         throw new IllegalArgumentException("Unsupported URI: " + uri);
      }
   }

   @Override
   public Uri insert(Uri arg0, ContentValues arg1) {
      Uri _uri;
      long rowID = 0;
      switch (uriMatcher.match(arg0)) {
      case UNSENT:
         rowID = db.insert(UNSENT_TABLE_NAME, "", arg1);
         if (rowID > 0) {
            _uri = ContentUris.withAppendedId(REPORTS_TABLE_URI, rowID);
            break;
         } else
            throw new SQLException("Failed to add a report into " + arg0);
      case REPORTS:
         rowID = db.insert(REPORTS_TABLE_NAME, "", arg1);
         if (rowID > 0) {
            _uri = ContentUris.withAppendedId(REPORTS_TABLE_URI, rowID);
            break;
         } else
            throw new SQLException("Failed to add a report into " + arg0);
      case ALERTS:
         rowID = db.insert(ALERTS_TABLE_NAME, "", arg1);
         if (rowID > 0) {
            _uri = ContentUris.withAppendedId(ALERTS_TABLE_URI, rowID);
            break;
         } else
            throw new SQLException("Failed to add a report into " + arg0);
      default:
         throw new IllegalArgumentException("Unsupported URI: " + arg0);
      }
      getContext().getContentResolver().notifyChange(arg0, null);
      return _uri;
   }

   @Override
   public Cursor query(Uri uri, String[] projection, String selection,
         String[] selectionArgs, String sortOrder) {
      SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
      if(sortOrder==null)
      sortOrder = TableColumns.CREATED_DATE+" DESC";
      
      
      switch (uriMatcher.match(uri)) {
      case REPORTS:
         qb.setTables(REPORTS_TABLE_NAME);
         break;
      case ALERTS:
         qb.setTables(ALERTS_TABLE_NAME);
         break;
      case UNSENT:
         qb.setTables(UNSENT_TABLE_NAME);
         break;
         
      }
      int limit = PreferenceManager.getDefaultSharedPreferences(getContext()).getInt("numberOfItemsToDisplay", 20);
      String limitClause = Integer.toString(limit);
      
      Cursor c = qb.query(db, projection, selection, selectionArgs, null, null,
            sortOrder,limitClause);
      c.setNotificationUri(getContext().getContentResolver(), uri);
      return c;
   }

   
   
   @Override
   public int update(Uri uri, ContentValues values, String selection,
         String[] selectionArgs) {
      int count = 0;

      switch (uriMatcher.match(uri)) {
      case UNSENT:
         count = db
               .update(UNSENT_TABLE_NAME, values, selection, selectionArgs);
         break;
      case REPORTS:
         count = db
               .update(REPORTS_TABLE_NAME, values, selection, selectionArgs);
         break;
      case REPORTS_ID:
         count = db.update(REPORTS_TABLE_NAME, values,
               ReporterColumns._ID
                     + " = "
                     + uri.getPathSegments().get(1)
                     + (!TextUtils.isEmpty(selection) ? " AND (" + selection
                           + ')' : ""), selectionArgs);
         break;
      case ALERTS:
         count = db.update(ALERTS_TABLE_NAME, values, selection, selectionArgs);
         break;
      default:
         throw new IllegalArgumentException("Unknown URI " + uri);
      }
      getContext().getContentResolver().notifyChange(uri, null);
      return count;
   }

}
