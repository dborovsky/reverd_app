package com.reverdapp.fragment.task;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.database.Database;
import com.reverdapp.webservice.WSAddComplaint;
import com.reverdapp.webservice.WSParameterContainer;

/**
 * Created by wojci on 8/17/15.
 */

public class AsyncSubmitComplain extends AsyncTask<Void, Void, WSAddComplaint> {
    private static final String TAG = LogConfig.genLogTag("AsyncSubmitComplain");

    private Activity mActivity;
    private int mNavigateTo;
    private ReverdApp mReverdApp;
    private IFragmentConnection mFragmentConnection;
    private ProgressDialog progressDialog;

    public AsyncSubmitComplain(IFragmentConnection fc, Activity a, final int navigateTo) {
        mActivity = a;
        mNavigateTo = navigateTo;
        mReverdApp = (ReverdApp)mActivity.getApplicationContext();
        mFragmentConnection = fc;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = ProgressDialog.show(mActivity, null, mActivity.getString(R.string.please_wait));
    }

    @Override
    protected WSAddComplaint doInBackground(Void... params) {

        final Bundle b = mFragmentConnection.getAsyncTaskOptions();

        // Extract options:
        final String countryCode = b.getString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID);
        final String phoneNumber = b.getString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID);
        final String name = b.getString(IFragmentConnection.IFC_NAME_ID);
        final String note = b.getString(IFragmentConnection.IFC_NOTE_ID);

        Log.d(TAG, "Option: IFC_COUNTRY_ISO3166_1_ID = " + countryCode);
        Log.d(TAG, "Option: IFC_NUMBER_WITHOUT_PREFIX_ID = " + phoneNumber);
        Log.d(TAG, "Option: IFC_NAME_ID = " + name);
        Log.d(TAG, "Option: IFC_NOTE_ID = " + note);

        final WSParameterContainer c = WSParameterContainer.createComplaint(mActivity, phoneNumber, countryCode, name, note);
        final WSAddComplaint call = new WSAddComplaint(mActivity, c);
        try {
          call.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Submit complaint failed", e );
          String[] parts = e.getMessage().split("#");
          mReverdApp.getDatabase().insertLostRequest( parts[0], parts[1] );
        }
        return call;
    }

    @Override
    protected void onPostExecute(WSAddComplaint result) {
        super.onPostExecute(result);

        if (progressDialog!=null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

        if (!result.getStatus()) {
            Util.hideSoftKeyboard(mActivity);
            Toast.makeText(mActivity, mActivity.getString(R.string.alert_your_complaint_has_been_sent_for_review), Toast.LENGTH_LONG).show();
            mFragmentConnection.handlePostExecute(mNavigateTo);
        }
    }
}
