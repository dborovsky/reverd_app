package com.reverdapp.fragment.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;

/**
 * Created by wojci on 7/24/15.
 */

// This is probably not going to be used.
public class AsyncAddToWhiteList extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("AsyncAddToWhiteList");
    private ProgressDialog progressDialog;
    private int mNavigateTo;
    private IFragmentConnection mIFragmentConnection;
    private Activity mActivity;
    private ReverdApp mReverdApp;

    public AsyncAddToWhiteList(IFragmentConnection fc, final Activity activity, final int navigateTo) {
        mActivity = activity;
        mIFragmentConnection = fc;
        mNavigateTo = navigateTo;
        mReverdApp = (ReverdApp) mActivity.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mActivity, null, mActivity.getString(R.string.please_wait));
    }

    @Override
    protected Void doInBackground(Void... params) {
        final Bundle b = mIFragmentConnection.getAsyncTaskOptions();

        final Database db = mReverdApp.getDatabase();

        Log.d(TAG, "Option: IFC_FULL_NUMBER_ID = " + b.getString(IFragmentConnection.IFC_FULL_NUMBER_ID));
        Log.d(TAG, "Option: IFC_NUMBER_WITHOUT_PREFIX_ID = " + b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID));
        Log.d(TAG, "Option: IFC_NOTE_ID = " + b.getString(IFragmentConnection.IFC_NOTE_ID));

        final String number = b.getString(IFragmentConnection.IFC_FULL_NUMBER_ID);
        final String countryPrefix = Iso2Phone.getCountryPrefix(number);
        final String countryCode= Iso2Phone.getCountryCode(number);

        db.insertToWhiteList(number, b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID), "", countryPrefix, countryCode, true, b.getString(IFragmentConnection.IFC_NOTE_ID));

        Log.d(TAG, "executed");
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        if(progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        mIFragmentConnection.handlePostExecute(mNavigateTo);

        /*
        ToastUtil.showToast(mActivity, wsAddToBlacklist.getMessage());
        if(!wsAddToBlacklist.getStatus()){

            Log.d(TAG, "Handling navigation: " + mNavigateTo);
            mIFragmentConnection.handlePostExecute(mNavigateTo);
        }
        */
    }

}
