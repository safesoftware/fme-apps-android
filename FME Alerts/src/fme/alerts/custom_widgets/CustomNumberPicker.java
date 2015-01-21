/*============================================================================= 
 
   Name     : CustomNumberPicker.java
 
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

package fme.alerts.custom_widgets;

import fme.alerts.AlertsContentProvider;
import fme.alerts.R;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

public class CustomNumberPicker extends DialogPreference {
   
   private static final int DEFAULT_MIN_VALUE = 0;
   private static final int DEFAULT_MAX_VALUE = 100;
   private static final int DEFAULT_MULTIPLIER_VALUE = 1;
   private static final int DEFAULT_VALUE = 10;
   
   private  String pref_key;;
   
   private TextView numberItemsTextView;
   private NumberPicker numberPicker;
   private  int defaultDisplayValue;
   private  int minValue;
   private  int maxValue;
   private  int multiplier;
   private SharedPreferences prefs;
   
   
   public CustomNumberPicker(Context context, AttributeSet attrs, int defStyle) {
      super(context, attrs, defStyle);
      initAttributes(attrs);
   }
   
   public CustomNumberPicker(Context context, AttributeSet attrs) {
      super(context, attrs);
      initAttributes(attrs);
   }
   
   private void initAttributes(AttributeSet attrs){
      TypedArray a = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.NumPicker, 0, 0);
      minValue = a.getInt(R.styleable.NumPicker_min, DEFAULT_MIN_VALUE);
      maxValue = a.getInteger(R.styleable.NumPicker_max, DEFAULT_MAX_VALUE);
      multiplier = a.getInteger(R.styleable.NumPicker_multiplier, DEFAULT_MULTIPLIER_VALUE);
      defaultDisplayValue = a.getInteger(R.styleable.NumPicker_defaultValue, DEFAULT_VALUE);
      a.recycle();
      pref_key  = getKey();
   }
   
   @Override
   protected void onBindView(View view) {
      super.onBindView(view);
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      numberItemsTextView = (TextView)view.findViewById(R.id.numberOfItemsTextView);
      numberItemsTextView.setText(Integer.toString(prefs.getInt(pref_key, defaultDisplayValue)));
   }
   
   @Override
   protected View onCreateDialogView() {
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      numberPicker = new NumberPicker(getContext());
      numberPicker.setMinValue(minValue);
      numberPicker.setMaxValue(maxValue);
      String[] displayedValues =new String[maxValue-minValue+1];
      for(int i=minValue;i<=maxValue;i++){
         displayedValues[i-minValue]=Integer.toString(i*multiplier);
      }
      numberPicker.setDisplayedValues(displayedValues);
      numberPicker.setValue(prefs.getInt(pref_key, defaultDisplayValue)/multiplier);
      return numberPicker;
   }

   @Override
   protected void onDialogClosed(boolean positiveResult) {
      int newNumber = pickerValueToDisplay(numberPicker.getValue());
      int oldNumber = prefs.getInt(pref_key, defaultDisplayValue);
      if(positiveResult && newNumber!=oldNumber){
         prefs.edit().putInt(pref_key, newNumber).commit();
         
         //This is a hack. There appears to be a bug in the framework which isn't allowing
         //an external TextView to be updated more than once unless an event occurs between updates
         setEnabled(false);
         setEnabled(true);
         
          numberItemsTextView.setTextKeepState(Integer.toString(newNumber));
          getContext().getContentResolver().notifyChange(AlertsContentProvider.ALERTS_TABLE_URI, null);
          getContext().getContentResolver().notifyChange(AlertsContentProvider.REPORTS_TABLE_URI, null);
          getContext().getContentResolver().notifyChange(AlertsContentProvider.UNSENT_TABLE_URI, null);
      }
     
   }

   
   /**
    * Takes in a value that the picker uses and converts 
    * it into a value to be displayed. i.e. Multiplies it by 10.
    * 
    * @param n The value that the picker is set to.
    */
   private int pickerValueToDisplay(int n){
      return n*multiplier;
   }
   
}
