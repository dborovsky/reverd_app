package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

// Base class for WS used to delete items.
public class WSDeleteBase extends WSBase {

     private final String mTAG;
     private boolean mError;

     public WSDeleteBase(final Context context,
                        final WSParameterContainer c,
                        final String TAG,
                              final String uriEnd) {
          super(context, uriEnd, c);
        mTAG = TAG;
    }

    /*
     private List<NameValuePair> generateJSON(final String phoneNumber,
                                             final String country) {
          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

          addDefaultsToParameters(nameValuePairs, true);
        nameValuePairs.add(new BasicNameValuePair(mNumberNameToDelete, phoneNumber));
          nameValuePairs.add(new BasicNameValuePair("country", country));

          Log.d(mTAG, "nameValuePairs = " + nameValuePairs.toString());

          return nameValuePairs;
     }
     */
     
     public void parseJSON(String response) {

          try {
               JSONObject jsMain = new JSONObject(response);
            mError = jsMain.getBoolean("error");
               if (mError) {
                Log.w(mTAG, "call failed");
            } else {
                Log.d(mTAG, "call to " + mEndpoint + " succeeded");
            }
          } catch (Exception e) {
               Log.e(mTAG, "parseJSON", e);
          }
     }

     public boolean getError(){
          return mError;
     }

     /*
     public void execute(final String phoneNumber,
                        final String country) {

        final String url = getWSUri(mUriEnd);
          final String response = new WebServiceUtil().postMethod(url, generateJSON(phoneNumber, country));
          parseJSON(response);
     }
     */
}
