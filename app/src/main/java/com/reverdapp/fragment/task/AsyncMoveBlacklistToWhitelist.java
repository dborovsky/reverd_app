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
import com.reverdapp.webservice.WSBlacklistToWhiteList;
import com.reverdapp.webservice.WSParameterContainer;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by wojci on 8/5/15.
 */

public class AsyncMoveBlacklistToWhitelist extends AsyncTask<Void, Void, Void> {
    private static final String TAG = LogConfig.genLogTag("AsyncMoveBlacklistToWhitelist");
    private IFragmentConnection mFragmentConnection;
    private Context mContext;
    private ProgressDialog mProgressDialog;
    private WSBlacklistToWhiteList mWsCall;
    private ReverdApp mReverdApp;
    private ArrayList<NumberListModel> mList;

    public AsyncMoveBlacklistToWhitelist(IFragmentConnection fc, Context context) {
        super();
        mFragmentConnection = fc;
        mContext = context;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait));
        final Bundle b = mFragmentConnection.getAsyncTaskOptions();
        mList = b.getParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST);
        Log.d(TAG, "Option: IFC_NUMBER_LIST = " + Arrays.toString(mList.toArray()));
        WSParameterContainer c = WSParameterContainer.createMoveBLtoWL(mContext, mList);
        mWsCall = new WSBlacklistToWhiteList(mContext, c);
    }
    @Override
    protected Void doInBackground(Void... params) {
        try {
          mWsCall.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Move to white-list failed", e );
          String[] parts = e.getMessage().split("#");
          mReverdApp.getDatabase().insertLostRequest( parts[0], parts[1] );
          for (final NumberListModel model: mList) {
              Log.d(TAG, "Using selected number: " + model.getPhoneNumber());
              mReverdApp.getDatabase().moveFromBlackListToWhiteList(model.getId());
          }
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if(mProgressDialog !=null && mProgressDialog.isShowing())
            mProgressDialog.dismiss();

        ToastUtil.showToast(mContext, mWsCall.getMessage());

        if (!mWsCall.getStatus()) {
            for (final NumberListModel model: mList) {
                Log.d(TAG, "Using selected number: " + model.getPhoneNumber());
                mReverdApp.getDatabase().moveFromBlackListToWhiteList(model.getId());
            }
        }
        mFragmentConnection.handlePostExecute(IFragmentConnection.UNUSED_NAVIGATE_ID);
    }
}
