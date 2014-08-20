package com.net;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class ConnectionHelper {
	
	public static final String CONNECTION = "Keep-Alive";
	public static final String CONTENT_TYPE = "application/x-www-form-urlencoded";
	public static final String ACCEPT = "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
	public static final String ACCEPT_LANGUAGE = "zh-tw,en-us;q=0.7,en;q=0.3";
	public static final String ACCEPT_CHARSE = "Big5,utf-8;q=0.7,*;q=0.7";
	public static final String USER_AGENT = "Mozilla/5.0 (Windows; U; Windows NT 6.0; zh-TW; rv:1.9.1.2) " +
			"Gecko/20090729 Firefox/3.5.2 GTB5 (.NET CLR 3.5.30729)";
	
	public static final String CHARSET = "utf-8";
	
	
	
	
	
	private static void setHttpsURLConnection() {
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
          
			public X509Certificate[] getAcceptedIssuers() {
              return null;
            }
			@Override
			public void checkClientTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void checkServerTrusted(X509Certificate[] arg0, String arg1)
					throws CertificateException {
				// TODO Auto-generated method stub
				
			}
		}};
		
		HostnameVerifier dummyHostnameVerifier = new HostnameVerifier() {   
			@Override
			public boolean verify(String arg0, SSLSession arg1) {
				// TODO Auto-generated method stub
				return false;
			}       
		}; 
		SSLContext sc;
		try {
			sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(dummyHostnameVerifier);
		} catch (KeyManagementException e) {  
			e.printStackTrace(); 
		} catch (NoSuchAlgorithmException e) {  
			e.printStackTrace();  
		}
	}
	
	
	
	
	public static boolean setCommonRequestProperties(HttpURLConnection URLConn, String cookie, String referer) {
		if(URLConn == null) 
			return false;
		
		URLConn.setRequestProperty("Connection", CONNECTION) ;
		URLConn.setRequestProperty("Content-Type", CONTENT_TYPE);
		URLConn.setRequestProperty("User-agent", USER_AGENT);
		URLConn .setRequestProperty("Accept", ACCEPT);
		URLConn.setRequestProperty("Accept-Language", ACCEPT_LANGUAGE);
		URLConn.setRequestProperty("Accept-Charse", ACCEPT_CHARSE); 
		
		if(cookie != null)
	        URLConn.setRequestProperty("Cookie", cookie);
		if(referer != null) 
	        URLConn.setRequestProperty("Referer", referer);
		return true;
	}
	
	public static HttpURLConnection doGet(String sURL, String cookie, String referer) {
		try {
			setHttpsURLConnection();
			URL url = new URL(sURL);
			HttpURLConnection URLConn = (sURL.contains("https")) ? 
					(HttpsURLConnection)url.openConnection() : (HttpURLConnection)url.openConnection();
			URLConn.setDoInput(true);
			URLConn.setDoOutput(true);
			URLConn.setInstanceFollowRedirects(false);
			setCommonRequestProperties(URLConn, cookie, referer);
			URLConn.connect();
			URLConn.getOutputStream().flush();
			return URLConn;
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public static HttpURLConnection doPost(String sURL, String data, String cookie, String referer) { 
	    try { 
	    	setHttpsURLConnection();
	    	URL url = new URL(sURL);
	    	HttpURLConnection URLConn = (sURL.contains("https")) ? 
					(HttpsURLConnection)url.openConnection() : (HttpURLConnection)url.openConnection(); 
	    	URLConn.setDoOutput(true); 
	    	URLConn.setDoInput(true); 
	    	((HttpURLConnection) URLConn).setRequestMethod("POST"); 
	    	URLConn.setUseCaches(false); 
	    	URLConn.setAllowUserInteraction(true); 
	    	URLConn.setInstanceFollowRedirects(false);
	    	setCommonRequestProperties(URLConn, cookie, referer);
	    	URLConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
	    	URLConn.setRequestProperty("Content-Length", String.valueOf(data.getBytes().length));
	    	DataOutputStream dos = new DataOutputStream(URLConn.getOutputStream()); 
	    	dos.writeBytes(data);
	    	return URLConn;

	    } catch (Exception e) { 
	    	e.printStackTrace();
	    }
	    
	    return null; 
	}
	
	
	
	public static String getCookie(HttpURLConnection URLConn) {
		String setCookie = URLConn.getHeaderField("Set-Cookie");
		if(setCookie == null)
			return null;
		return setCookie.substring(0, setCookie.indexOf(';'));
	}
	
	public static Map<String, String> getInputValueMap(HttpURLConnection URLConn) {
		
		HashMap<String, String> map = new HashMap<String, String>();
		String line;
		int beginIndex1, beginIndex2, endIndex1, endIndex2;
		try {
			BufferedReader buf = new BufferedReader(
					new InputStreamReader(URLConn.getInputStream(), CHARSET));
			while((line = buf.readLine()) != null) {
				if(line.contains("<input") && line.contains("value=")) {
					beginIndex1 = line.indexOf("name=\"") + 6;
					beginIndex2 = line.indexOf("value=\"") + 7;
					endIndex1 = line.indexOf("\"", beginIndex1);
					endIndex2 = line.indexOf("\"", beginIndex2);
					map.put(line.substring(beginIndex1 ,  (endIndex1 > beginIndex1) ? endIndex1 : line.length()), 
							line.substring(beginIndex2, (endIndex2 > beginIndex2) ? endIndex2 : line.length()));
				} else if(line.contains("<select name=")) {
					beginIndex1 = line.indexOf("name=\"") + 6;
					endIndex1 = line.indexOf("\"", beginIndex1);
					String name=line.substring(beginIndex1 ,  (endIndex1 > beginIndex1) ? endIndex1 : line.length());
					if(((line = buf.readLine()) != null) && line.contains("<option selected")) {
						beginIndex2 = line.indexOf("value=\"") + 7;
						endIndex2 = line.indexOf("\"", beginIndex2);
						map.put(name, line.substring(beginIndex2, (endIndex2 > beginIndex2) ? endIndex2 : line.length()));
					}
					
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return map;
	}
	
	
}
