package com.informatica.mdm.bes.test_utils;

import java.io.IOException;
import java.net.URISyntaxException;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
	public static Response makePostCall(String url, String requestBodyString) throws IOException, URISyntaxException {
		System.out.println(url);
		System.out.println(requestBodyString);
		OkHttpClient client = new OkHttpClient();
		RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), requestBodyString);

		String credential = Credentials.basic("admin", "admin");
		Request request = new Request.Builder()
		   .url(url)
		   .header("Authorization", credential)
		   .header("accept", "application/json")
		   .post(requestBody)
		   .build(); // defaults to GET

		Response response;
		return client.newCall(request).execute();
	}
}
