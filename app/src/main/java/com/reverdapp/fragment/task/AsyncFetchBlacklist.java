package com.reverdapp.fragment.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.WSInitialBlackList;
import com.reverdapp.webservice.WSParameterContainer;

/**
 * Created by wojci on 8/7/15.
 */

// Load the default black list from network.
public class AsyncFetchBlacklist extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("AsyncFetchBlacklist");

    private Context mContext;
    private WSInitialBlackList mWsCall;
    private ProgressBar mProgressBar;
    private IFragmentConnection mFragmentConnection;
    private ReverdApp mReverdApp;

    public AsyncFetchBlacklist(final Context c, final ProgressBar pb, final IFragmentConnection fc) {
        super();
        mContext = c;
        mProgressBar = pb;
        mFragmentConnection = fc;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //progressBar = (ProgressBar) getView().findViewById(R.id.fragment_blacklist_pb);
        mProgressBar.setVisibility(View.VISIBLE);
    }
    @Override
    protected Void doInBackground(Void... params) {
        WSParameterContainer c = WSParameterContainer.createBlacklist(mContext);
        mWsCall = new WSInitialBlackList(mContext, c);
        try {
          mWsCall.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Fetching black-list failed", e );
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (isCancelled()) {
            return;
        }

        if(mProgressBar!=null && mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.GONE);
        }

        Log.d(TAG, "Finished loading BL from network.");

        mFragmentConnection.handlePostExecute(IFragmentConnection.UNUSED_NAVIGATE_ID);
    }
}
