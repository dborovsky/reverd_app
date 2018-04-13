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

public class WSBlacklistToWhiteList extends WSBase {
    private static final String TAG = LogConfig.genLogTag("WSBlacklistToWhiteList");

    private boolean status = true;
    private String message = "";

    public WSBlacklistToWhiteList(final Context context, final WSParameterContainer c) {
        super(context, WSName.MOVE_FROM_LIST_TO_ANOTHER, c);
    }

    /*
    private List<NameValuePair> generateJSON(ArrayList<NumberListModel> blackLists) {
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        addDefaultsToParameters(nameValuePairs, false);
        nameValuePairs.add(new BasicNameValuePair("langcode", Locale.getDefault().getLanguage()));
        
        JSONArray jsMain = new JSONArray();
        for (int i=0;i<blackLists.size();i++) {
            if (blackLists.get(i).isSelected()) {
                JSONObject jsSelectedNumber = new JSONObject();
                try {
                    jsSelectedNumber.put("caller", blackLists.get(i).getCaller());
                    jsSelectedNumber.put("country", blackLists.get(i).getCountry());
                    jsSelectedNumber.put("country_code", blackLists.get(i).getCountryCode());
                    jsSelectedNumber.put("phone", blackLists.get(i).getPhoneNumber());
                } catch (JSONException e) {
                    Log.e(TAG, "generateJSON", e);
                }
                jsMain.put(jsSelectedNumber);
            }
            
        }
        nameValuePairs.add(new BasicNameValuePair("phones_to_wl", jsMain.toString()));

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
    public void execute(ArrayList<NumberListModel> blackLists) {

        final String url = getWSUri(WSName.MOVE_FROM_LIST_TO_ANOTHER);
        String response = new WebServiceUtil().postMethod(url, generateJSON(blackLists));
        parseJSON(response);

    }
    */
}
