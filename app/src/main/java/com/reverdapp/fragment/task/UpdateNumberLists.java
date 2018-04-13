package com.reverdapp.fragment.task;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.WSBlackListMerge;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSWhiteListMerge;

/**
 * Created by wojci on 8/18/15.
 */
public class UpdateNumberLists extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("UpdateNumberLists");

    private Context mContext;
    private IFragmentConnection mFragmentConnection;

    public UpdateNumberLists(Context c, IFragmentConnection fc) {
        super();
        mContext = c;
        mFragmentConnection = fc;
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "UpdateNumberLists running..");

        // Merge BL:
        WSParameterContainer c = WSParameterContainer.createBlacklist(mContext);
        final WSBlackListMerge wsBLMergeCall = new WSBlackListMerge(mContext, c);
        try {
          wsBLMergeCall.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Black-list merge failed", e );
        }

        // Merge WL:
        c = WSParameterContainer.createWhitelist(mContext);
        final WSWhiteListMerge wsWLMergeCall = new WSWhiteListMerge(mContext, c);
        try {
          wsWLMergeCall.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "White-list merge failed", e );
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        Log.d(TAG, "UpdateNumberLists finished..");
        mFragmentConnection.handlePostExecute(IFragmentConnection.UNUSED_NAVIGATE_ID);
    }
}
