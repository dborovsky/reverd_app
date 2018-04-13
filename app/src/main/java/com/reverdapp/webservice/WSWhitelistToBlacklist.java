package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSWhitelistToBlacklist extends WSBase {

	private static final String TAG = LogConfig.genLogTag("WSWhitelistToBlacklist");
	private boolean status = true;
	private String message = "";

	public WSWhitelistToBlacklist(final Context context, final WSParameterContainer c) {
        super(context, WSName.MOVE_FROM_LIST_TO_ANOTHER, c);
	}

    /*
	private List<NameValuePair> generateJSON(ArrayList<NumberListModel> whiteLists) {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		addDefaultsToParameters(nameValuePairs, false);
		nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));

		JSONArray jsMain = new JSONArray();
		for(int i=0;i<whiteLists.size();i++){

			if(whiteLists.get(i).isSelected()){
				JSONObject jsSelectedNumber = new JSONObject();
				try {
					jsSelectedNumber.put("caller", whiteLists.get(i).getCaller());
					jsSelectedNumber.put("country", whiteLists.get(i).getCountry());
					jsSelectedNumber.put("country_code", whiteLists.get(i).getCountryCode());
					jsSelectedNumber.put("phone", whiteLists.get(i).getPhoneNumber());
				} catch (JSONException e) {
                    Log.e(TAG, "generateJSON", e);
				}
				jsMain.put(jsSelectedNumber);
			}
			
		}
		
		nameValuePairs.add(new BasicNameValuePair("phones_to_bl", jsMain.toString()));

        Log.d(TAG, "Parameters: " + nameValuePairs.toString());

		return nameValuePairs;
	}

    */

	public void parseJSON(String response) {

		try {
			JSONObject jsMain = new JSONObject(response);
			status = jsMain.getBoolean("error");
			message = jsMain.getString("message");
		} catch (Exception e) {
			Log.e(TAG, "parseJSON", e);
		}
	}
	
	public boolean getStatus(){
		return status;
	}

	public String getMessage(){
		return message;
	}

	/*
	public void execute(ArrayList<NumberListModel> whiteLists) {

		final String url = getWSUri(WSName.MOVE_FROM_LIST_TO_ANOTHER);
		String response = new WebServiceUtil().postMethod(url, generateJSON(whiteLists));
		parseJSON(response);
	}
	*/
}
