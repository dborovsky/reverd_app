package com.reverdapp.subscription;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.fragment.task.UpdateNumberLists;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.webservice.WSInitialBlackList;
import com.reverdapp.webservice.WSInitialWhitelist;
import com.reverdapp.webservice.WSParameterContainer;

/**
 * Created by wojci on 8/14/15.
 */
public final class SubscriptionManager {
    private static final String TAG = LogConfig.genLogTag("SubscriptionManager");

    private Context mContext;
    private ReverdApp mReverdApp;
    private boolean mSubscribed;

    public SubscriptionManager(Context c)
    {
        mContext = c;
        mReverdApp = (ReverdApp) mContext.getApplicationContext();
        mSubscribed = false;
    }

    public boolean initialUpdate() {
        boolean status = true;

        Log.d(TAG, "Initial update");

        try {
            WSParameterContainer c = WSParameterContainer.createBlacklist(mContext);

            WSInitialBlackList mWsInitialBLCall = new WSInitialBlackList(mContext, c);
            mWsInitialBLCall.execute();

            Log.d(TAG, "Updated BL");

            c = WSParameterContainer.createWhitelist(mContext);
            WSInitialWhitelist mWsInitialWLCall = new WSInitialWhitelist(mContext, c);
            mWsInitialWLCall.execute();

            Log.d(TAG, "Updated WL");
        }
        catch (Exception e) {
            status = false;
            Log.w(TAG, "initialUpdate failed!", e);
        }

        return status;
    }

    IFragmentConnection mFragmentConnection = new IFragmentConnection() {

        @Override
        public Bundle getAsyncTaskOptions() {
            Bundle b = new Bundle();
            return b;
        }

        @Override
        public void handlePostExecute(final int destination) {
            final AppPreferences ap = new AppPreferences(mContext);
            final long stamp = Util.getTimestamp();
            ap.set(AppPreferences.PREF_LAST_SYNC_DATE, Long.toString(stamp));
        }
    };

    // Update the BL and the WL (if any numbers were moved).
    // Handle if any numbers were moved or added as own numbers.
    public void updateNumberLists() {
        Log.d(TAG, "updateNumberLists");
        new UpdateNumberLists(mContext, mFragmentConnection).execute();
    }

    public void writeInitialSettings() {
        final AppPreferences ap = new AppPreferences(mContext);
        long stamp = Util.getTimestamp();

        if (mReverdApp.isDebugBuild()) {
            Log.d(TAG, "Debug, modify stamp .. ");
            final long temp = 3 * 86400000L;
            stamp -= temp;
        }

        Log.d(TAG, "Initial modify stamp: " + Long.toString(stamp));
        ap.set(AppPreferences.PREF_LAST_SYNC_DATE, Long.toString(stamp));
    }

    // Clean up old entries in DB.
    public void cleanUp() {
        Database db = mReverdApp.getDatabase();

        final long now = System.currentTimeMillis() / 1000L;
        final long thirtyOneDaysAgo = now - (31L * 24L * 60L * 60L);

        db.cleanUpDetailedCallLog(thirtyOneDaysAgo);
        db.cleanUpCallStats(thirtyOneDaysAgo);

    }

    public void setState(boolean subscribed) {
        mSubscribed = subscribed;
    }
}
