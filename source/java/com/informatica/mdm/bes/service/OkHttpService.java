package com.informatica.mdm.bes.service;

import java.io.IOException;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.fasterxml.jackson.databind.ObjectMapper;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.OkHttpClient.Builder;

/**
 * 
 * @author Matthew Dromazos
 *
 */
public class OkHttpService {
	
	int SSL_APPLICATION_PORT = 8443;
	
	X509TrustManager TRUST_ALL_CERTS = new X509TrustManager() {
	    @Override
	    public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) {
	    }

	    @Override 
	    public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) {
	    }

	    @Override
	    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
	        return new java.security.cert.X509Certificate[] {};
	    }
	};
	
	/**
	 * This method creates a OkHttp Builder that trusts all endpoints
	 * @return
	 */
	public Builder createTrustAllBuilder(OkHttpClient client) {
	    Builder builder = client.newBuilder();
	    
	    try {
	    	SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, new TrustManager[] { TRUST_ALL_CERTS }, new java.security.SecureRandom());
		    builder.sslSocketFactory(sslContext.getSocketFactory(), (X509TrustManager) TRUST_ALL_CERTS);
		    builder.hostnameVerifier(new HostnameVerifier() {
		        @Override
		        public boolean verify(String hostname, SSLSession session) {
		            return true;
		        }
		    });
		} catch (KeyManagementException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    
	    return builder;
	}

}
