package com.reverdapp.fragment.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.WSBlacklistDelete;
import com.reverdapp.webservice.WSParameterContainer;

import java.util.ArrayList;

/**
 * Created by wojci on 8/6/15.
 */
public class AsyncDeleteFromBlacklist extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("AsyncDeleteFromBlacklist");
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private IFragmentConnection mFragmentConnection;
    private ReverdApp mReverdApp;
    private ArrayList<NumberListModel> mModels;

    public AsyncDeleteFromBlacklist(final Context c, final IFragmentConnection fc) {
        mProgressDialog = null;
        mContext = c;
        mFragmentConnection = fc;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressDialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.please_wait));
    }

    @Override
    protected Void doInBackground(Void... params) {
        Log.d(TAG, "Option: IFC_NUMBER_LIST size = " + mFragmentConnection.getAsyncTaskOptions().getParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST).size());
        mModels = mFragmentConnection.getAsyncTaskOptions().getParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST);
        final Database db = mReverdApp.getDatabase();
        for (NumberListModel model : mModels) {
            final int numberId = model.getId();
            Log.d(TAG, "Deleting " + numberId + " from BL");

            final WSParameterContainer c = WSParameterContainer.createDeleteFromBlacklist(mContext, model.getPhoneNumber(), model.getCountry());
            final WSBlacklistDelete call = new WSBlacklistDelete(mContext, c);
            try {
              call.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Delete from black-list failed", e );
              String[] parts = e.getMessage().split("#");
              db.insertLostRequest( parts[0], parts[1] );
            }

            db.deleteBlacklistNumber(numberId);
        }

        return null;
    }
    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        if (mProgressDialog!=null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }

        mFragmentConnection.handlePostExecute(IFragmentConnection.UNUSED_NAVIGATE_ID);

        // TODO: show something to the user!
        //ToastUtil.showToast(getActivity(), wsWhitelistToBlacklist.getMessage());
    }
}
