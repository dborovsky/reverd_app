package com.reverdapp.webservice;


import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSRegistration extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSRegistration");
    private boolean status;
    private String message = "";
    
    public WSRegistration(final Context context, final WSParameterContainer c) {
        super(context, WSName.REGISTER, c);
    }
    
    /*
    private List<NameValuePair> generateJSON() {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(3);
        addDefaultsToParameters(nameValuePairs, false);
        nameValuePairs.add(new BasicNameValuePair("version", Build.VERSION.RELEASE+""));
        nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));
        
        return nameValuePairs;
    }
    */
    
    public void parseJSON(String response) {

        Log.d(TAG, "response = " + response);

        try {
            JSONObject jsMain = new JSONObject(response);
            status = jsMain.getBoolean("error");
            message = jsMain.getString("message");
            
            //if(!status){

            //final AppPreferences ap = new AppPreferences(getContext());
            //ap.set(AppPreferences.INITIALIZED, true);

            //}
            
        } catch (Exception e) {
            Log.e(TAG, "register", e);
        }
        
    }
    
    public String getMessage() {

        return message;
    }

    /*
    public void execute() {
        final String url = getWSUri(WSName.REGISTER);
        String response = new WebServiceUtil().postMethod(url, generateJSON());
        parseJSON(response);
        
    }
    */
    
}
