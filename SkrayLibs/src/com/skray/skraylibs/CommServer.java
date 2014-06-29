package com.skray.skraylibs;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpConnection;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import com.skray.skraylibs.utils.SkrayUtils;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

/**
 * @author skullacy
 * Communicate Server with JSON
 * 
 * 
 * 
 */
public class CommServer {
	static final String DEFAULT_SITE_URL = "http://skullacytest.cafe24.com";
	static final int SET_TIMEOUT_DEFAULT = 10000;
	static final int SET_TIMEOUT_SHORT = 5000;
	static final int SET_TIMEOUT_LONG = 15000;
	
	static final boolean SET_DEBUG_MODE = false;
	static final String TAG = "CommServer";
	
	private int timeout;
	private Context context;
	private String serverUrl;
	private List<NameValuePair> params;
	private Boolean showProgressDialog;
	private String id;
	
	public interface onGetResponse{
		void getResponse(String result);
	}
	private onGetResponse resCallback;
	
	public interface onProgressResponse{
		void onProgress();
		void onComplete();
	}
	private onProgressResponse progCallback;
	
	
	ProgressDialog pDialog;
	AlertDialog.Builder alertDialog;
		
	/**
	 * Constructors with context
	 */
	public CommServer(Context context, String url, int timeout){
		init(context, url, timeout);
	}
	public CommServer(Context context, String url){
		init(context, url, SET_TIMEOUT_DEFAULT);
	}
	public CommServer(Context context){
		init(context, DEFAULT_SITE_URL, SET_TIMEOUT_DEFAULT);
	}
	public void init(Context context, String url, int timeout){
		this.context = context;
		this.timeout = timeout;
		
		pDialog = new ProgressDialog(context);
		pDialog.setTitle("");
		pDialog.setMessage("로딩중...");
		
		params = new ArrayList<NameValuePair>();
		showProgressDialog = true;
		
		if(SkrayUtils.isNullOrEmpty(getServerUrl())){
			setServerUrl(url);
		}
	}
	
	
	/**
	 * Constructors without context
	 */
	public CommServer(String url) {
		initWithoutContext(url, null);
	}
	public CommServer(String url, ArrayList<NameValuePair> params) {
		initWithoutContext(url, params);
	}
	public void initWithoutContext(String url, ArrayList<NameValuePair> params) {
		showProgressDialog = false;
		params = (params != null) ? params : new ArrayList<NameValuePair>();
		
		setServerUrl(url);
	}
	
	
	/**
	 * Print Logs
	 */
	public static void skraylog(String msg){
		if(SET_DEBUG_MODE) SkrayUtils.skraylog(TAG, msg);
		else return;
	}
	
	
	/**
	 * Methods 
	 */
	
	
	public String getServerUrl() {
		return serverUrl;
	}
	public void setServerUrl(String serverUrl) {
		this.serverUrl = serverUrl;
	}
	
	
	public CommServer setId(String id){
		this.id = id;
		return this;
	}
	
	public String getId(){
		String result = "";
		if(!SkrayUtils.isNullOrEmpty(this.id))
			result = this.id;
		
		return result;
	}
	
	public CommServer setTimeout(int timeout){
		this.timeout = timeout;
		return this;
	}
	
	
	
	public CommServer setParam(String query, String value){
		params.add(new BasicNameValuePair(query, value));
		return this;
	}

	public void insertImage(Bitmap image){
		insertImage(image, "image");
	}
	public void insertImage(Bitmap image, String param){
		insertImage(image, param, SkrayUtils.ImageType.jpg);
	}
	public void insertImage(Bitmap image, String param, SkrayUtils.ImageType type){
		insertImage(image, param, type, 90);
	}
	public void insertImage(Bitmap image, String param, SkrayUtils.ImageType type, int quality){
		setParam(param, SkrayUtils.convertBitmapToBase64String(image, type, quality));
		setParam("ext", type.name());
	}
	
	
	public void showProgressDialog(){
		pDialog.show();
	}
	public void hideProgressDialog(){
		pDialog.dismiss();
	}
	public void handleProgressStart(){
		if(progCallback != null){
			progCallback.onProgress();
		}
		else{
			if(this.showProgressDialog) showProgressDialog();
		}
	}
	public void handleProgressComplete(){
		if(progCallback != null){
			progCallback.onComplete();
		}
		else{
			if(this.showProgressDialog) hideProgressDialog();
		}
	}
	
	/**
	 * @brief set response callback method
	 */
	public CommServer setOnGetResponse(onGetResponse cusCallback){
		this.resCallback = cusCallback;
		return this;
	}
	
	public CommServer setOnProgressResponse(onProgressResponse cusCallback){
		this.progCallback = cusCallback;
		return this;
	}
	
	/**
	 * @brief remove callback method
	 */
	
	public CommServer removeOnGetResponse(){
		this.resCallback = null;
		return this;
	}
	
	public CommServer removeOnProgressResponse(){
		this.progCallback = null;
		return this;
	}
	
	/**
	 * @deprecated
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public Bitmap getImage() throws UnsupportedEncodingException{
		Bitmap bitmap = null;
		URL url;
		try {
			url = new URL(getServerUrl());
			URLConnection conn = url.openConnection();
			conn.connect();
			BufferedInputStream bis = new BufferedInputStream(conn.getInputStream());
			bitmap = BitmapFactory.decodeStream(bis);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return bitmap;
	}

	/**
	 * @brief Connect Server and get ResponseData (must be in AsyncTask (CommServerTask)
	 * @return responseData
	 */
	public String getData() throws Exception
	{
		StringBuffer sb = new StringBuffer();
		InputStream is = null;
		
		HttpClient httpclient = new DefaultHttpClient();
		try{
			int timeout = this.timeout;
//			HttpParams httpparams = httpclient.getParams();
//			HttpConnectionParams.setConnectionTimeout(httpparams, 5000);
//			HttpConnectionParams.setSoTimeout(httpparams, 5000);
			httpclient.getParams().setParameter("http.protocol.expect-continue", false);
			httpclient.getParams().setParameter("http.connection.timeout", timeout);
			httpclient.getParams().setParameter("http.socket.timeout", timeout);
			
			
			HttpPost httppost = new HttpPost(getServerUrl());
			UrlEncodedFormEntity entityRequest = new UrlEncodedFormEntity(params, "utf-8");
			httppost.setEntity(entityRequest);
			
			HttpResponse response = httpclient.execute(httppost);
			HttpEntity entityResponse = response.getEntity();
			is = entityResponse.getContent();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
			String line = null;
			while((line = reader.readLine()) != null){
				sb.append(line);
			}
			is.close();
			
		}
		catch(Exception e){
			skraylog("EXCEPTION");
			throw e;
		}
		finally{
			httpclient.getConnectionManager().shutdown();
		}

		return sb.toString();
	}

	/**
	 * @deprecated
	 * @param url
	 * @param params
	 * @return
	 */
	private static InputStream getInputStreamFromUrl(String url, List<NameValuePair> params){
		InputStream contentStream = null;
		try{
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost post = new HttpPost(url);
			UrlEncodedFormEntity ent = new UrlEncodedFormEntity(params, HTTP.UTF_8);
			post.setEntity(ent);
			HttpResponse responsePost = httpclient.execute(post);

			contentStream = responsePost.getEntity().getContent();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		return contentStream;
	}
	
	
	/**
	 * @brief use getData() in CommServerTast(AsyncTask)
	 */
	public void getResponseString(){
		new CommServerTask().execute(this);
	}


	public class CommServerTask extends AsyncTask<CommServer, Integer, String>{
		
		@Override
		protected void onPreExecute(){
			handleProgressStart();
			super.onPreExecute();
		}

		@Override
		protected String doInBackground(CommServer... params) {
			String data = null;
			try{
				data = params[0].getData();
			}
			catch(Exception e){
				skraylog("EXCEPTION in ASYNCTASK");
				cancel(true);
			}
			return data;
		}
		
		@Override
		protected void onPostExecute(String result){
			handleProgressComplete();
			if(resCallback != null){
				resCallback.getResponse(result);
			}
			super.onPostExecute(result);
			
		}
		
		@Override
		protected void onCancelled(){
			super.onCancelled();
			skraylog("CANCELLED");
		}
		
		@Override
		protected void onProgressUpdate(Integer... status){
			
		}
		
		

	}
	


}

