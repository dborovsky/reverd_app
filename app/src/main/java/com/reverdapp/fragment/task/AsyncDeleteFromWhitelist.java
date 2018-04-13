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
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSWhitelistDelete;

import java.util.ArrayList;

/**
 * Created by wojci on 8/6/15.
 */
public class AsyncDeleteFromWhitelist extends AsyncTask<Void, Void, Void> {

    private static final String TAG = LogConfig.genLogTag("AsyncDeleteFromWhitelist");
    private ProgressDialog mProgressDialog;
    private Context mContext;
    private IFragmentConnection mFragmentConnection;
    private ReverdApp mReverdApp;

    public AsyncDeleteFromWhitelist(final Context c, final IFragmentConnection fc) {
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
        final ArrayList<NumberListModel> models = mFragmentConnection.getAsyncTaskOptions().getParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST);
        final Database db = mReverdApp.getDatabase();
        for (NumberListModel model : models) {
            final int numberId = model.getId();
            Log.d(TAG, "Deleting " + numberId + " from WL");

            final WSParameterContainer c = WSParameterContainer.createDeleteFromWhitelist(mContext, model.getPhoneNumber(), model.getCountry());
            final WSWhitelistDelete call = new WSWhitelistDelete(mContext, c);
            try {
              call.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Delete from white-list failed", e ); 
              String parts[] = e.getMessage().split("#");
              db.insertLostRequest( parts[0], parts[1] );
            }

            db.deleteWhitelistNumber(numberId);
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
