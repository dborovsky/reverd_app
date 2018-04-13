package com.reverdapp.fragment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.ToastUtil;
import com.reverdapp.webservice.WSAddToBlacklist;
import com.reverdapp.webservice.WSWhitelistDelete;

/**
 * Created by wojci on 7/24/15.
 */

public abstract class UpdateListEntryBase extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("UpdateListEntryBase");

    protected Context mContext;
    protected NumberListModel mModel;
    protected ReverdApp mReverdApp;

    public UpdateListEntryBase(final Context c, NumberListModel model) {
        mContext = c;
        mModel = model;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // TODO: handle progress dialog.
        // progressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait));

    }

    abstract void delete(NumberListModel m);
    abstract void add(NumberListModel m);

    @Override
    protected Void doInBackground(Void... params) {

        Log.d(TAG, "Deleting " + mModel.getId());
        delete(mModel);
        Log.d(TAG, "Adding " + mModel.getFullNumber());
        add(mModel);

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
    }

}
