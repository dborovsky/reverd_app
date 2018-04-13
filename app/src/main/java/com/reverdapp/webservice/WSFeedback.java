package com.reverdapp.webservice;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSFeedback extends WSBase {

	private boolean status;
	private String message = "";
	
	public WSFeedback(final Context context, final WSParameterContainer c) {
        super(context, WSName.FEEDBACK, c);
	}

	/*
	private List<NameValuePair> generateJSON(final String feedback) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        addDefaultsToParameters(nameValuePairs, false);
		nameValuePairs.add(new BasicNameValuePair("feedback", feedback));
		nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));

		return nameValuePairs;
	}
	*/
	
	public void parseJSON(String response) {
		try {
		
			JSONObject jsMain = new JSONObject(response);
			status = jsMain.getBoolean("error");
			message = jsMain.getString("message");
			if(!status){
				
			}
		}catch (Exception e) {
		}
	}
	
	public String getMessage(){
		return message;
	}

    /*
	public void execute(final String feedback) {

        final String url = getWSUri(WSName.FEEDBACK);
		String response = new WebServiceUtil().postMethod(url,
                generateJSON(feedback));
		parseJSON(response);

	}
	*/
}
