package com.reverdapp.fragment.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSSubscribe;

/**
 * Created by wojci on 7/24/15.
 */

public class AsyncBuySubscription extends AsyncTask<Void, Void, WSSubscribe> {

    private static final String TAG = LogConfig.genLogTag("AsyncBuySubscription");
    private Context mContext;
    private IFragmentConnection mFragmentConnection;

    public final static int INVALID_TERM = -1;

    public final static int TERM_1 = 1;
    public final static int TERM_6 = 6;
    public final static int TERM_12 = 12;

    public final static int RECURRING_ON = 1;
    public final static int RECURRING_OFF = 0;

    // Navigation:
    public final static int NAVIGATION_SUCCESS = 0;
    public final static int NAVIGATION_ERROR = -1;

    public AsyncBuySubscription(final Context c, IFragmentConnection fc) {
        mContext = c;
        mFragmentConnection = fc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected WSSubscribe doInBackground(Void... params) {

        final Bundle b = mFragmentConnection.getAsyncTaskOptions();
        final int subscriptionTerm = b.getInt(IFragmentConnection.IFC_IAP_TERM);
        final int recurring = b.getInt(IFragmentConnection.IFC_IAP_RECOURRING);

        Log.d(TAG, "Buying " + subscriptionTerm + ", " + recurring);
        WSParameterContainer c = WSParameterContainer.createSubscription(mContext, subscriptionTerm, recurring);
        WSSubscribe s = new WSSubscribe(mContext, c);
        try {
          s.execute();
        } catch ( Exception e ) {
          Log.e(TAG, "Updating back-end about subscription failed ( Weird you came here, should not happen)", e );
        }

        return s;
    }

    @Override
    protected void onPostExecute(final WSSubscribe result) {
        super.onPostExecute(result);

        if (result.getError()) {
            Log.d(TAG, "Subscribe error!");
            mFragmentConnection.handlePostExecute(NAVIGATION_ERROR);
        } else {
            Log.d(TAG, "Subscribed");
            mFragmentConnection.handlePostExecute(NAVIGATION_SUCCESS);
        }
    }
}
