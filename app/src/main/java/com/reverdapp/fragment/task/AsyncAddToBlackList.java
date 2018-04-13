package com.reverdapp.fragment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.ToastUtil;
import com.reverdapp.webservice.WSAddToBlacklist;
import com.reverdapp.webservice.WSParameterContainer;

/**
 * Created by wojci on 7/24/15.
 */

public class AsyncAddToBlackList extends AsyncTask<Void, Void, WSAddToBlacklist> {

    private static final String TAG = LogConfig.genLogTag("AsyncAddToBlackList");
    private ProgressDialog progressDialog;
    private int mNavigateTo;
    private IFragmentConnection mIFragmentConnection;
    private Context mContext;
    private ReverdApp mReverdApp;
    private boolean mRemoteAdd;

    public AsyncAddToBlackList(IFragmentConnection fc, final Context c, final int navigateTo) {
        mContext = c;
        mIFragmentConnection = fc;
        mNavigateTo = navigateTo;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
        mRemoteAdd = true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait));

    }

    @Override
    protected WSAddToBlacklist doInBackground(Void... params) {

        final Bundle b = mIFragmentConnection.getAsyncTaskOptions();

        Log.d(TAG, "Option: IFC_ADD_TO_COMM_BL_ID = " + b.getBoolean(IFragmentConnection.IFC_ADD_TO_COMM_BL_ID));
        Log.d(TAG, "Option: IFC_FULL_NUMBER_ID = " + b.getString(IFragmentConnection.IFC_FULL_NUMBER_ID));
        Log.d(TAG, "Option: IFC_NUMBER_WITHOUT_PREFIX_ID = " + b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID));
        Log.d(TAG, "Option: IFC_COUNTRY_NAME_ID = " + b.getString(IFragmentConnection.IFC_COUNTRY_NAME_ID));
        Log.d(TAG, "Option: IFC_COUNTRY_ISO3166_1_ID = " + b.getString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID));
        Log.d(TAG, "Option: IFC_NAME_ID = " + b.getString(IFragmentConnection.IFC_NAME_ID));
        Log.d(TAG, "Option: IFC_NOTE_ID = " + b.getString(IFragmentConnection.IFC_NOTE_ID));

        mRemoteAdd = b.getBoolean(IFragmentConnection.IFC_ADD_TO_COMM_BL_ID, false);
        if (mRemoteAdd) {
            Log.d(TAG, "Remote add!");
            String country = b.getString(IFragmentConnection.IFC_COUNTRY_NAME_ID);
            final WSParameterContainer c = WSParameterContainer.createAddToBlacklist(mContext,
                    b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID),
                    b.getString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID),
                    b.getString(IFragmentConnection.IFC_NAME_ID),
                    b.getString(IFragmentConnection.IFC_NOTE_ID));
            final WSAddToBlacklist call = new WSAddToBlacklist(mContext, c);
            try {
              call.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Adding to remote black list failed : " + e.getMessage(), e );
              String[] parts = e.getMessage().split("#");
              mReverdApp.getDatabase().insertLostRequest( parts[0], parts[1] );
            }

            Log.d(TAG, "executed WS");
            addToDb(b, 1);
            Log.d(TAG, "executed addToDb");
            return call;
        }
        else {
            Log.d(TAG, "Local add!");
            addToDb(b, 1);
            Log.d(TAG, "executed addToDb");
            return null;
        }

    }

    void addToDb(final Bundle b, int isLocal) {
        final String phoneNumber = b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID);

        // Use the two letter code instead of the full country here, as the web service expects this.
        //String country = b.getString(IFragmentConnection.IFC_COUNTRY_NAME_ID);
        final String country = b.getString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID);
        int countryCode = b.getInt(IFragmentConnection.IFC_COUNTRY_NUM_CODE_ID);
        final String name = b.getString(IFragmentConnection.IFC_NAME_ID);
        final String notes = b.getString(IFragmentConnection.IFC_NOTE_ID);
        mReverdApp.getDatabase().insertBlackListedNumber(name, phoneNumber, countryCode, country, isLocal, notes);
    }

    private void remoteFinished(final WSAddToBlacklist result) {
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ToastUtil.showToast(mContext, mContext.getString(R.string.phone_added_to_backlist));
        if (!result.getStatus()){
            Log.d(TAG, "Handling navigation: " + mNavigateTo);
            mIFragmentConnection.handlePostExecute(mNavigateTo);
        }
        Log.d(TAG, "Remote add, nothing more to do.");
    }

    private void localFinished() {
        Log.d(TAG, "Local add, nothing more to do.");
        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        ToastUtil.showToast(mContext, mContext.getString(R.string.phone_added_to_backlist));
        Log.d(TAG, "Handling navigation: " + mNavigateTo);
        mIFragmentConnection.handlePostExecute(mNavigateTo);
    }

    @Override
    protected void onPostExecute(WSAddToBlacklist result) {
        super.onPostExecute(result);
        if (mRemoteAdd)
        {
            remoteFinished(result);
        } else {
            localFinished();
        }
    }

}
