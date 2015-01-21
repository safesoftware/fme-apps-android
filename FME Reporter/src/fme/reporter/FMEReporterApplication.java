/*============================================================================= 
 
   Name     : FMEReporterApplication.java
 
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

import fme.common.FMEApplication;
import fme.common.SettingsMenu;
import fme.common.SubscriptionCanceller;

public class FMEReporterApplication extends
      FMEApplication<SendLocationAndUpdateMapTask> {
   private static FMEReporterApplication singleton;

   private MainActivity mainActivityHandle;

   static public FMEReporterApplication getInstance() {
      return singleton;
   }

   @Override
   public void onCreate() {
      super.onCreate();
      singleton = this;
      super.setSuperInstance(this);
      super.setTopicsListener(null);
      super.setReportKeyword("report");
      super.setGCMRegID(null);
      super.setSubscriptionCanceller(new ReporterSubscriptionCanceller());

   }

   public MainActivity getMainActivityHandle() {
      return mainActivityHandle;
   }

   public void updateMainActivityHandle(MainActivity mainActivityHandle) {
      this.mainActivityHandle = mainActivityHandle;
   }

   @Override
   public SendLocationAndUpdateMapTask createSpecificTaskType() {
      return new SendLocationAndUpdateMapTask();
   }

   private class ReporterSubscriptionCanceller implements SubscriptionCanceller {

      public void cancelAllSubscriptions(SettingsMenu settingsMenu) {
         
            settingsMenu.logout();
      }

   }
   
   public int[] deleteUserData(){
      return null;
   }
   
}
