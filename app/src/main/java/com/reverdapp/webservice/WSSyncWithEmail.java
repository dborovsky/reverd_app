package com.reverdapp.webservice;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSSyncWithEmail extends WSBase {

	private boolean status;
	private String message = "";
	
	public WSSyncWithEmail(final Context context, final WSParameterContainer c) {
        super(context, WSName.SYNC, c);
	}

	/*
	private List<NameValuePair> generateJSON(String email) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
		addDefaultsToParameters(nameValuePairs, false);
		nameValuePairs.add(new BasicNameValuePair("email", email));
		nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));
		return nameValuePairs;
	}
	*/
	
	public void parseJSON(String response) {
		
		try{
			
			JSONObject jsMain = new JSONObject(response);
			status = jsMain.getBoolean("error");
			message = jsMain.getString("message");
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	public boolean getStatus(){
		return status;
	}
	public String getMessage(){
		return message;
	}

    /*
	public void execute(final String email) {

        final String url = getWSUri(WSName.SYNC);
		final String response = new WebServiceUtil().postMethod(url,
				generateJSON(email));
		parseJSON(response);

	}
	*/
}
