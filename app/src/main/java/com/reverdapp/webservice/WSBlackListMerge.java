package com.reverdapp.webservice;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;

import org.json.JSONObject;

public class WSBlackListMerge extends ListUpdaterBase {

    private static final String TAG = LogConfig.genLogTag("WSBlackListMerge");
    private final Database mDatabase;
    private Context mContext;

    public WSBlackListMerge(final Context context, final WSParameterContainer c) {
        super(context, WSName.BLACKLIST, c, new String[] { BL_PHONES, USER_BL_PHONES });
        mDatabase = ((ReverdApp) getContext().getApplicationContext()).getDatabase();
        mContext = context;
    }

    @Override
    public void onInit() {
        // Nothing to be done here.
    }

    @Override
    public void onSuccess() {
        Log.d(TAG, "OK");
    }

    @Override
    public void onError() {
        Log.e(TAG, "unable to fetch result");
    }

    @Override
    public void onResult(final String name, final JSONObject result) {

        // System entries will be merged first followed by the user's entries.
        if (name.equals(BL_PHONES)) {
            onBlPhoneResult(result);
        } else if (name.equals(USER_BL_PHONES)) {
            onUserBlPhoneResult(result);
        }
    }

    // The user's entries.
    private void onUserBlPhoneResult(final JSONObject result) {
        final String inputPhone = result.optString("phone");

        final int inputCountryCode = result.optInt("country_code");

        String fullNumber = "";
        if (result.has("country_code") && inputCountryCode > 0) {

            //Log.d(TAG, "Got country code! " + inputCountryCode);

            fullNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(Iso2Phone.formatFullPhonenumber(inputCountryCode, inputPhone),mContext);
        }
        else {
            fullNumber = inputPhone;
        }

        //Log.d(TAG, "Merging BL user entry: " + fullNumber);

        final Cursor wlCursor = mDatabase.getWhiteListedNumber(fullNumber);

        if (wlCursor.getCount() > 0) {
            // Assumption: nothing to do.

            // Update the number entry on the user's WL.
            //Log.d(TAG, "Merge: BL entry already exists");

            wlCursor.close();
            return;
        }

        wlCursor.close();

        final Cursor blCursor = mDatabase.getBlackListedNumber(fullNumber);

        if (blCursor.getCount() > 0) {

            blCursor.moveToFirst();
            final NumberListModel nlm = NumberListModel.createBlackListModel(blCursor);

            // Assumption: nothing to do.
            //Log.d(TAG, "Merge: user BL entry already exists");
        }
        else {
            // New entry, remote.
            Log.d(TAG, "Add new user BL entry: local");
            mDatabase.insertBlackListedNumber(result.optString("caller"),
                    result.optString("phone"),
                    result.optInt("country_code"),
                    result.optString("country"),
                    Database.LOCAL_ENTRY,
                    result.optString("notes"));
        }

        blCursor.close();
    }

    // The system entries.
    private void onBlPhoneResult(final JSONObject result) {
        final String inputPhone = result.optString("phone");
        final int inputCountryCode = result.optInt("country_code");

        final String fullNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(Iso2Phone.formatFullPhonenumber(inputCountryCode, inputPhone),mContext);

        //Log.d(TAG, "Merging BL entry: " + fullNumber);

        final Cursor wlCursor = mDatabase.getWhiteListedNumber(fullNumber);

        if (wlCursor.getCount() > 0) {
            // Update the number entry on the user's WL.

            //Log.d(TAG, "Merge: replace WL entry");

            wlCursor.moveToFirst();
            final NumberListModel nlm = NumberListModel.createWhiteListModel(wlCursor);
            if (nlm.isLocal() == Database.LOCAL_ENTRY) {
                // A local WL entry exists.
                Log.d(TAG, "Merge: replace WL local with remote");

                int id = nlm.getId();
                mDatabase.deleteWhitelistNumber(id);

                mDatabase.insertWhiteListedNumber(result.optString("caller"),
                        result.optString("phone"),
                        result.optInt("country_code"),
                        result.optString("country"),
                        Database.REMOTE_ENTRY,
                        "");
            }
            else {
                // Assumption: nothing to do.
                // Merge the contents:
                //Log.d(TAG, "Merge: WL remote");
            }

            wlCursor.close();

            return;
        }

        wlCursor.close();

        final Cursor blCursor = mDatabase.getBlackListedNumber(fullNumber);

        if (blCursor.getCount() > 0) {
            blCursor.moveToFirst();
            final NumberListModel nlm = NumberListModel.createBlackListModel(blCursor);

            if (nlm.isLocal() == Database.LOCAL_ENTRY) {
                // A local entry exists.
                Log.d(TAG, "Merge: replace local with remote");

                int id = nlm.getId();
                mDatabase.deleteBlacklistNumber(id);

                mDatabase.insertBlackListedNumber(result.optString("caller"),
                        result.optString("phone"),
                        result.optInt("country_code"),
                        result.optString("country"),
                        Database.REMOTE_ENTRY,
                        "");
            }
            else {
                // Assumption: nothing to do.
                // Merge the contents:
                //Log.d(TAG, "Merge: remote");
            }
        }
        else {
            // New entry, remote.
            Log.d(TAG, "Add new BL number: remote");
            mDatabase.insertBlackListedNumber(result.optString("caller"),
                    result.optString("phone"),
                    result.optInt("country_code"),
                    result.optString("country"),
                    Database.REMOTE_ENTRY,
                    "");
        }

        blCursor.close();
    }

    @Override
    public void onFinish(final boolean error) {
        Log.d(TAG, "finished BL merge, error=" + error);
    }
}
