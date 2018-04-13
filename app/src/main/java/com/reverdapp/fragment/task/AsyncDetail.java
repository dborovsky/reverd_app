package com.reverdapp.fragment.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.WSDetails;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.cache.DetailsCache;
import com.reverdapp.webservice.cache.ICache;

/**
 * Created by wojci on 7/31/15.
 */
public class AsyncDetail extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("AsyncDetail");
    private ProgressBar mProgressBar;
    private Context mContext;
    private ICache mCache;
    private String mPhoneNumberToFetchDetailsfor;
    private TextView mNameDestination;
    private TextView mNoteDestination;
    private WSDetails mWsCall;
    private boolean mGotCachedEntry;

    public AsyncDetail(Context c,
                       String phoneNumberToFetchDetailsfor,
                       ProgressBar progressBar,
                       TextView nameDestination, TextView noteDestination) {
        mContext = c;
        mCache = ((ReverdApp)mContext.getApplicationContext()).getDetailsCache();

        mProgressBar = progressBar;
        mPhoneNumberToFetchDetailsfor = phoneNumberToFetchDetailsfor;
        mNameDestination = nameDestination;
        mNoteDestination = noteDestination;
        mGotCachedEntry = false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
    }
    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Option: phone = " + mPhoneNumberToFetchDetailsfor);
        if (mCache.contains(mPhoneNumberToFetchDetailsfor)) {
            mGotCachedEntry = true;
        }
        else {
            WSParameterContainer c = WSParameterContainer.createDetails(mContext, mPhoneNumberToFetchDetailsfor);
            mWsCall = new WSDetails(mContext, c);
            try {
              mWsCall.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Fetch details failed", e );
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (isCancelled()) {
            return;
        }

        if (mProgressBar!=null && mProgressBar.getVisibility() == View.VISIBLE)
            mProgressBar.setVisibility(View.GONE);

        if (mGotCachedEntry) {
            // Use cached values.

            final Bundle b = mCache.get(mPhoneNumberToFetchDetailsfor);

            final String cachedName = b.getString(DetailsCache.ID_NAME);
            final String cachedNote = b.getString(DetailsCache.ID_NOTE);

            if (cachedName != null) {
                mNameDestination.setText(cachedName);
            }
            if (cachedNote!=null) {
                mNoteDestination.setText(cachedNote);
            }
            return;
        }

        if (!mWsCall.getStatus()) {
            if (mWsCall.getName()!=null) {
                mNameDestination.setText(mWsCall.getName());
            }
            if (mWsCall.getNote()!=null) {
                mNoteDestination.setText(mWsCall.getNote());
            }
        }

        if (!mGotCachedEntry) {
            updateCache(mPhoneNumberToFetchDetailsfor, mWsCall.getName(), mWsCall.getNote());
        }
    }

    private void updateCache(final String phone, final String name, final String note) {
        Bundle b = new Bundle();
        b.putString(DetailsCache.ID_NAME, name);
        b.putString(DetailsCache.ID_NOTE, note);
        mCache.add(phone, b);
    }
}
