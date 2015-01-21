/*============================================================================= 
 
   Name     : FetchTopicsTask.java
 
   System   : FMECommon
 
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

package fme.common;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import android.os.AsyncTask;


 class FetchTopicsTask extends AsyncTask<String, Void, String> {
		protected String errorStr = ""; 
		protected String host;
		protected String username;
		protected String password;
		
		@Override
		protected String doInBackground(String... params) {
		   InputStream inputStream = null;
			String response = "";
			host = params[0].toLowerCase();
			username = params[1];
			password = params[2];			
			String fullURLFormat = "%s/fmerest/v2/notifications/topics?accept=json&detail=low";
			String fullURL = String.format(fullURLFormat, host);
			NetworkUtil.authenticate(username, password);
			try{
			   inputStream = NetworkUtil.getURLConnection(fullURL).getInputStream();
			   response = NetworkUtil.readInputStream(inputStream);
			}catch(IOException e){
			   e.printStackTrace();
			   errorStr = e.getMessage();
			} catch (KeyManagementException e) {
            e.printStackTrace();
            errorStr = e.getMessage();
         } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            errorStr = e.getMessage();
         } finally {
			   if(inputStream!=null){
               try {
                  inputStream.close();
               } catch (IOException e) {
                  e.printStackTrace();
               }
			   }
			}
			return response;
		}
 }
