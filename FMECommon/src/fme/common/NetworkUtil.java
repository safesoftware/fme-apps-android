/*============================================================================= 
 
   Name     : NetworkUtil.java
 
   System   : FME Common
 
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

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class NetworkUtil {
   public static final String HTTP = "http://";
   public static final String HTTPS = "https://";

   /**
    * A HostnameVerifier that does not check for certificates.
    */
   public static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
      public boolean verify(String hostname, SSLSession session) {
         return true;
      }
   };

   public static String readInputStream(InputStream inputStream)
         throws IOException {
      BufferedReader reader = new BufferedReader(new InputStreamReader(
            inputStream, "UTF-8"));
      StringBuilder builder = new StringBuilder();
      for (String line = null; (line = reader.readLine()) != null;) {
         builder.append(line);
      }
      reader.close();
      return builder.toString();
   }

   /**
    * Send an authenticated post request to a server
    * 
    * @param body
    *           The content to post to the sever
    * @return response The response from the server
    * @throws IOException
    * @throws NoSuchAlgorithmException
    * @throws KeyManagementException
    */
   public static String post(String host, String body, String username,
         String password) throws IOException, KeyManagementException,
         NoSuchAlgorithmException {
      String response = "Internal error"; // This error shouldn't happen
      authenticate(username, password);
      HttpURLConnection conn = getURLConnection(host);
      conn.setRequestMethod("POST");
      conn.setDoOutput(true);
      conn.setRequestProperty("Content-Type", "application/json");
      conn.setRequestProperty("charset", "utf-8");
      DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
      wr.writeBytes(body);
      wr.flush();
      wr.close();
      int status = conn.getResponseCode();
      if (200 > status || status >= 300) {
         response = readInputStream(conn.getErrorStream());
      } else
    	  response = "";
      conn.disconnect();
      return response;
   }

   public static void authenticate(final String username, final String password) {
      Authenticator.setDefault(new Authenticator() {
         protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(username, password.toCharArray());
         }
      });
   }

   /**
    * Call this method before connecting to a server using HTTPS. Currently no
    * verification happens.
    * 
    * @throws NoSuchAlgorithmException
    * @throws KeyManagementException
    */
   public static void verifyHost() throws NoSuchAlgorithmException,
         KeyManagementException {
      TrustManager[] trustAllCertificates = new TrustManager[] { new X509TrustManager() {
         public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return new java.security.cert.X509Certificate[] {};
         }

         public void checkClientTrusted(X509Certificate[] chain, String authType)
               throws CertificateException {
         }

         public void checkServerTrusted(X509Certificate[] chain, String authType)
               throws CertificateException {
         }
      } };
      SSLContext context = SSLContext.getInstance("TLS");
      context
            .init(null, trustAllCertificates, null);
      HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());
   }

   /**
    * Checks to see what protocol is being used. If there is no protocol use
    * HTTP.
    */
   public static String formatHostname(String host) {
      if (!host.startsWith(HTTPS)) {
         host = host.startsWith(HTTP) ? host : HTTP + host;
      }
      return host;
   }

   /**
    * Returns a URL connection based on whether or not HTTPS is being used.
    * 
    * @param fullURL
    * @return
    * @throws IOException
    * @throws NoSuchAlgorithmException
    * @throws KeyManagementException
    */
   public static HttpURLConnection getURLConnection(String fullURL)
         throws IOException, KeyManagementException, NoSuchAlgorithmException {
      URL url = new URL(formatHostname(fullURL));
      if (url.getProtocol().equalsIgnoreCase("https")) {
         verifyHost();
         HttpsURLConnection https = (HttpsURLConnection) url.openConnection();
         https.setHostnameVerifier(NetworkUtil.DO_NOT_VERIFY);
         return https;
      } else {
         return (HttpURLConnection) url.openConnection();
      }
   }
   
   /**
    *  Use this method in place of 'getURLConnection' if connecting to a cloud instance or if the HTTPS server has a valid certificate
    */
   public static HttpURLConnection getSecureURLConnection(String fullURL)
         throws IOException, KeyManagementException, NoSuchAlgorithmException {
      URL url = new URL(formatHostname(fullURL));
      return (HttpURLConnection) url.openConnection();
   }

}
