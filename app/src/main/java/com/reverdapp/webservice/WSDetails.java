package com.reverdapp.webservice;

import android.content.Context;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WSDetails extends WSBase {

	private boolean status;
	private String message = "";
	
	private String name;
	private String note;
	
	public WSDetails(final Context context, final WSParameterContainer c) {
        super(context, WSName.DETAILS, c);
	}

    /*
	private List<NameValuePair> generateJSON(String phoneNumber) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
		nameValuePairs.add(new BasicNameValuePair("phone", phoneNumber));

		return nameValuePairs;
	}
	*/
	
	public void parseJSON(String response) {
		try {
			
			JSONObject jsMain = new JSONObject(response);
			status = jsMain.getBoolean("error");
			if(!status){
				
				JSONObject jsDetail = jsMain.getJSONObject("phone_details");
				name = jsDetail.optString("caller");
				note = jsDetail.optString("complaint");
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean getStatus(){
		return status;
	}
	
	public String getName(){
		return name;
	}
	
	public String getNote(){
		return note;
	}

	/*
	public void execute(String phoneNumber) {

        final String url = getWSUri(WSName.DETAILS);
		String response = new WebServiceUtil().postMethod(url,
				generateJSON(phoneNumber));
		parseJSON(response);
	}
	*/
}
