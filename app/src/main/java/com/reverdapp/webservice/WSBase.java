package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;
import android.util.Pair;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wojci on 5/5/15.
 */
public abstract class WSBase {
    private static final String TAG = LogConfig.genLogTag("WSBase");

    protected final Context mContext;
    protected String mEndpoint;
    protected WSParameterContainer mWSParameterContainer;
    //protected final String mPhoneId;

    public WSBase(final Context context, final String endpoint, final WSParameterContainer c)
    {
        mContext = context;
        mEndpoint = endpoint;
        mWSParameterContainer = c;
        final ReverdApp app = (ReverdApp) mContext.getApplicationContext();
        //mPhoneId = app.getPhoneId();
    }

    protected Context getContext()
    {
        return mContext;
    }

    /*protected String getPhoneId() {
        return mPhoneId;
    }
    */

    /*
    protected void addDefaultsToParameters(HashMap<String, String> parameters, boolean useIMEI) {
        if (useIMEI) {
            parameters.put("IMEI", mPhoneId);
        } else {
            parameters.put("phone", mPhoneId);
        }
        parameters.put("platform", "Android");
    }

    protected void addDefaultsToParameters(List<NameValuePair> parameters, boolean useIMEI) {
        if (useIMEI) {
            parameters.add(new BasicNameValuePair("IMEI", mPhoneId));
        } else {
            parameters.add(new BasicNameValuePair("phone", mPhoneId));
        }
        parameters.add(new BasicNameValuePair("platform", "Android"));
    }
*/

    public String getWSUri(String postfix)
    {
        StringBuffer sb = new StringBuffer();
        sb.append(getWSPrefix());
        sb.append("/");
        sb.append(postfix);

        return sb.toString();
    }

    public String getWSPrefix()
    {
        return mContext.getString(R.string.WebServicePrefix);
    }

    //protected abstract HashMap<String, String> getParameters();

    protected void parseJSON(final String response) {
        Log.d(TAG, "Received: " + response);
    }

    public void execute() throws Exception {
        final HashMap<String, String> parameters = mWSParameterContainer.getParameters();
        final String url = getWSUri(mEndpoint);

        Log.d(TAG, "Calling: " + url);

        for(Map.Entry<String, String> entry : parameters.entrySet()) {
            Log.d(TAG, "parameter " + entry.getKey() + " = " + entry.getValue());
        }

        final String response = WebServiceUtil.postMethod(mContext, url, parameters);
        parseJSON(response);
    }
}
