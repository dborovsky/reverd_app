package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSAddToWhitelist extends WSBase {

	private static final String TAG = LogConfig.genLogTag("WSAddToWhitelist");
	private boolean mError;

	public WSAddToWhitelist(Context context, final WSParameterContainer c) {
		super(context, WSName.WHITELIST_ADD, c);
	}

	public void parseJSON(String response) {

		try {
			JSONObject jsMain = new JSONObject(response);
            mError = jsMain.getBoolean("error");
			if (mError) {
                Log.w(TAG, "call failed");
            }
		} catch (Exception e) {
			Log.e(TAG, "parseJSON", e);
		}
	}

	public boolean getStatus(){
		return mError;
	}
}
