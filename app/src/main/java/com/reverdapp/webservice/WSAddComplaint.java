package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class WSAddComplaint extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSAddComplaint");
    private boolean status;
    private String message = "";
    
    public WSAddComplaint(final Context context, final WSParameterContainer c) {
        super(context, WSName.ADDCOMPLAINT, c);
    }

    /*
    private HashMap<String, String> generateJSON(String callerPhone,String callerCountry,String callerName,String complaint) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        addDefaultsToParameters(parameters, false);
        parameters.put("caller_phone", callerPhone);
        parameters.put("caller_country", callerCountry);
        parameters.put("caller_name", callerName);
        parameters.put("complaint", complaint);
        parameters.put("langcode", Locale.getDefault().getLanguage());

        return parameters;
    }
    */

    public void parseJSON(final String response) {

        try {
            final JSONObject jsMain = new JSONObject(response);
            status = jsMain.getBoolean("error");
            message = jsMain.getString("message");
            if (status) {
                Log.w(TAG, "failed");
            }
            
        } catch (Exception e) {
            Log.e(TAG, "failed", e);
        }
    }
    
    public boolean getStatus(){
        return status;
    }

    public String getMessage(){
        return message;
    }

    /*
    public void execute(final String callerPhone,
                        final String callerCountry,
                        final String callerName,
                        final String complaint) {
        final String url = getWSUri(WSName.ADDCOMPLAINT);
        final String response = WebServiceUtil.postMethod(url,
                generateJSON(callerPhone,callerCountry,callerName,complaint));
        parseJSON(response);
    }
    */
}
