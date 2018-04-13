package com.reverdapp.fragment.task;

import android.content.Context;
import android.util.Log;

import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.webservice.WSAddToBlacklist;
import com.reverdapp.webservice.WSAddToWhitelist;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSWhitelistDelete;

/**
 * Created by wojci on 7/24/15.
 */

public class UpdateWhitelistEntry extends UpdateListEntryBase {

    private static final String TAG = LogConfig.genLogTag("UpdateWhitelistEntry");
    Context mContext;

    public UpdateWhitelistEntry(final Context c, final NumberListModel model) {
        super(c, model);
        mContext = c;
    }

    @Override
    void delete(final NumberListModel m) {
        final WSParameterContainer c = WSParameterContainer.createDeleteFromWhitelist(mContext, mModel.getPhoneNumber(), mModel.getCountry());
        final WSWhitelistDelete cmd = new WSWhitelistDelete(mContext, c);
        Log.d(TAG, "Delete WL: " + mModel.getPhoneNumber() + ", " + mModel.getCountry());
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
        final WSParameterContainer c = WSParameterContainer.createAddToWhiteList(mContext, mModel.getPhoneNumber(), mModel.getCountry(), mModel.getCaller(), mModel.getNote());

        final WSAddToWhitelist cmd = new WSAddToWhitelist(mContext, c);
        Log.d(TAG, "Adding BL, params: " + c.toString());
        try {
          cmd.execute();
        } catch ( Exception e ) {
          Log.e( TAG, "Add from white list failed", e );
          String parts[] = e.getMessage().split("#");
          ((ReverdApp) mContext.getApplicationContext()).getDatabase().insertLostRequest( parts[0], parts[1] );
        }
    }
}
