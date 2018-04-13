package com.reverdapp.fragment.task;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.database.Database;
import com.reverdapp.webservice.WSAddToBlacklist;
import com.reverdapp.webservice.WSBlacklistDelete;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSWhitelistDelete;

/**
 * Created by wojci on 7/24/15.
 */

public class UpdateBlacklistEntry extends UpdateListEntryBase {

    private static final String TAG = LogConfig.genLogTag("UpdateBlacklistEntry");

    public UpdateBlacklistEntry(final Context c,
                                final NumberListModel model) {
        super(c, model);
    }

    @Override
    void delete(final NumberListModel m) {
        WSParameterContainer c = WSParameterContainer.createDeleteFromBlacklist(mContext, mModel.getPhoneNumber(), mModel.getCountry());
        WSBlacklistDelete cmd = new WSBlacklistDelete(mContext, c);
        Log.d(TAG, "Delete BL: " + mModel.getPhoneNumber() + ", " + mModel.getCountry());
        try {
          cmd.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Delete from white list failed", e );
          String parts[] = e.getMessage().split("#");
          ((ReverdApp) mContext.getApplicationContext()).getDatabase().insertLostRequest( parts[0], parts[1] );
        }

    }

    @Override
    void add(final NumberListModel m) {
        WSParameterContainer c = WSParameterContainer.createAddToBlacklist(mContext, mModel.getPhoneNumber(), mModel.getCountry(), mModel.getCaller(), mModel.getNote());
        WSAddToBlacklist cmd = new WSAddToBlacklist(mContext, c);
        Log.d(TAG, "Adding WL");
        Log.d(TAG, "param: " + mModel.getPhoneNumber());
        Log.d(TAG, "param: " + mModel.getCountry());
        Log.d(TAG, "param: " + mModel.getCaller());
        Log.d(TAG, "param: " + mModel.getNote());
        try {
          cmd.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Delete from white list failed", e );
          String parts[] = e.getMessage().split("#");
          ((ReverdApp) mContext.getApplicationContext()).getDatabase().insertLostRequest( parts[0], parts[1] );
        }
    }
}
