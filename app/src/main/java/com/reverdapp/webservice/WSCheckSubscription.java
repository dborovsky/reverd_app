package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.json.JSONObject;

public class WSCheckSubscription extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSCheckSubscription");
	private boolean mSubscribed;
	private boolean mError;
    private String mExpires;
    private int mRecurring;

	public WSCheckSubscription(final Context context, final WSParameterContainer c) {
		super(context, WSName.SUBSCRIPTION_CHECK, c);

        mSubscribed = false;
        mError = false;
        mExpires = "";
        mRecurring = 0;
	}

    public void parseJSON(final String response) {

        try {
			final JSONObject jsMain = new JSONObject(response);
            mError = jsMain.getBoolean("error");

            if (mError) {
                Log.w(TAG, "failed");
            }
            else {
                mSubscribed = jsMain.getBoolean("subscribed");
                if (mSubscribed) {
                    mExpires = jsMain.getString("expires");
                    mRecurring = jsMain.getInt("recurring");
                }
            }
			
		} catch (Exception e) {
			Log.e(TAG, "failed", e);
		}
	}
	
	public boolean getError(){
		return mError;
	}

	public String getExpires(){
		return mExpires;
	}

    public boolean isSubscribed() {
        return mSubscribed;
    }

    public boolean isRecurring() {
        return mRecurring == 1;
    }
}
