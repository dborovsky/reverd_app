package com.reverdapp.webservice;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSAddToBlacklist extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSAddToBlacklist");
    private boolean mError;
    private String message = "";

    public WSAddToBlacklist(final Context context, final WSParameterContainer c) {
        super(context, WSName.BLACKLIST_ADD, c);
    }

    /*
    private List<NameValuePair> generateJSON(final String addToBlackListNumber,
                                             final String country,
                                             final String name,
                                             final String notes) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        addDefaultsToParameters(nameValuePairs, false);
        nameValuePairs.add(new BasicNameValuePair("phone_bl", addToBlackListNumber));
        nameValuePairs.add(new BasicNameValuePair("country", country));
        nameValuePairs.add(new BasicNameValuePair("name", name));
        nameValuePairs.add(new BasicNameValuePair("notes", notes));
        nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));

        Log.d(TAG, "nameValuePairs = " + nameValuePairs.toString());

        return nameValuePairs;
    }
    */

    public void parseJSON(final String response) {

        try {
            JSONObject jsMain = new JSONObject(response);
            mError = jsMain.getBoolean("error");
            message = jsMain.getString("message");
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

    public String getMessage(){
        return message;
    }

    /*
    public void execute(final String addToBlackListNumber,
                        final String country,
                        final String name,
                        final String notes) {

        final String url = getWSUri(WSName.BLACKLIST_ADD);

        final String response = new WebServiceUtil().postMethod(url, generateJSON(addToBlackListNumber, country, name, notes));
        parseJSON(response);
    }
    */
    
}
