package com.reverdapp.stats;

import android.util.Log;

import com.reverdapp.database.Database;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.LogConfig;

/**
 * Created by wojci on 8/12/15.
 */
public class DbCallStatLogger implements ICallStatLogger {
    private static final String TAG = LogConfig.genLogTag("DbCallStatLogger");

    private Database mDatabase;

    public DbCallStatLogger(Database db) {
        mDatabase = db;
    }

    @Override
    public void onReceivedCall(final int type, final String phone) {
        Log.d(TAG, "onReceivedCall " + type + ", " + phone);

        switch (type) {
            case Constants.NUM_BLACKLISTED:
            case Constants.NUM_WHITELISTED:
            case Constants.NUM_NOT_PRESENT:
                mDatabase.updateCallCounter(type);
                mDatabase.updateAddCallInfo(type, phone);
                break;
            default:
                Log.e(TAG, "Unknown call type " + type);
                break;
        }
    }
}
