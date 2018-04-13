package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class ListUpdaterBase extends WSBase implements IListUpdater {

	private static final String TAG = LogConfig.genLogTag("ListUpdaterBase");
    //private String mUri;
    private String[] mArrayNames;

	public ListUpdaterBase(final Context context,
                           final String uri,
                           final WSParameterContainer c,
                           final String[] arrayNames) {
		super(context, uri, c);
        mArrayNames = arrayNames;
	}

    /*
    protected HashMap<String, String> getParameters()
    {
        HashMap<String, String> p = new HashMap<String, String>();
        addDefaultsToParameters(p, false);

        return p;
    }
    */

    /*
	private List<NameValuePair> generateJSON() {
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

        addDefaultsToParameters(nameValuePairs, false);

		return nameValuePairs;
	}
	*/

	public void parseJSON(final String response) {
        super.parseJSON(response);
        onInit();

        boolean error = true;
        try {
			final JSONObject jsMain = new JSONObject(response);
            error = jsMain.getBoolean("error");

			if (!error) {
                onSuccess();

                for (String arrayName: mArrayNames) {
                    Log.d(TAG, "Expecting name '" + arrayName + "'");
                    final JSONArray numbers = jsMain.optJSONArray(arrayName);

                    if (numbers != null) {
                        Log.d(TAG, "Found expected array '" + arrayName + "'");

                        for (int i = 0; i < numbers.length(); i++) {
                            final JSONObject number = numbers.getJSONObject(i);
                            onResult(arrayName, number);
                        }
                    } else {
                        Log.w(TAG, "No array with name '" + arrayName + "' provided.");
                    }
                }
			}

		} catch (final Exception e) {
			Log.w(TAG, "parseJSON", e);
		}

        if (error) {
            onError();
        }

        onFinish(error);
	}

    /*
	public void execute() {
        final String url = getWSUri(mUri);
		Log.d(TAG, "Using url:" + url);
		final String response = new WebServiceUtil().postMethod(url, generateJSON());
		parseJSON(response);
	}
	*/
}
