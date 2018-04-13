package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.json.JSONObject;

public class WSSubscribe extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSSubscribe");

    private boolean mError;
    private String mMessage;

    public WSSubscribe(final Context context, final WSParameterContainer c) {
        super(context, WSName.SUBSCRIBE, c);
        mMessage = "";
        mError = false;
    }

    public void parseJSON(final String response) {

        try {
            final JSONObject jsMain = new JSONObject(response);
            mError = jsMain.getBoolean("error");

            if (mError) {
                Log.w(TAG, "failed");
            }
            else {
                mMessage = jsMain.getString("message");
            }
        } catch (Exception e) {
            Log.e(TAG, "failed", e);
            mError = true;
        }
    }

    public boolean getError(){
        return mError;
    }

    public String getMessage(){
        return mMessage;
    }

}
