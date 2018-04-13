package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;

import org.json.JSONArray;
import org.json.JSONObject;

public class WSCountryAreaList extends WSBase {

    private static final String TAG = LogConfig.genLogTag("WSCountryAreaList");
    private boolean status;

    public WSCountryAreaList(final Context context, final WSParameterContainer c) {
        super(context, WSName.COUNTRIES, c);
    }
    
    public void parseJSON(String response){
        try {
            JSONObject jsMain = new JSONObject(response);
            status = jsMain.getBoolean("error");
            if (!status) {
                
                ReverdApp reverdApp = (ReverdApp) getContext().getApplicationContext();
                reverdApp.getDatabase().deleteCountry();
                reverdApp.getDatabase().deleteArea();
                
                JSONArray jsCountries = jsMain.optJSONArray("countries");
                for(int i=0;i<jsCountries.length();i++){

                    final JSONObject jsCountry = jsCountries.getJSONObject(i);
                    final String countryName = jsCountry.getString("country");
                    final String countryCode = jsCountry.getString("country_code");
                    final int callingCode = jsCountry.getInt("calling_code");
                    final JSONArray jsStates = jsCountry.optJSONArray("states");
                    
                    reverdApp.getDatabase().insertCountry(callingCode, countryCode, countryName,false,i);
                    
                    for(int j=0;j<jsStates.length();j++){

                        final JSONObject jsArea = jsStates.getJSONObject(j);
                        final String areaName = jsArea.getString("state");
                        final String areaCode = jsArea.getString("area_code");
                        
                        reverdApp.getDatabase().insertArea(countryCode,countryName, areaCode, areaName,false);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "WSCountryAreaList", e);
        }
        
    }

    /*
    public void execute() {
        final String url = getWSUri(WSName.COUNTRIES);
        String response = new WebServiceUtil().postMethod(url,null);
        parseJSON(response);
    }
    */
}
