package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.google.i18n.phonenumbers.NumberParseException;

import org.json.JSONObject;

public class WSInitialBlackList extends ListUpdaterBase {

    private static final String TAG = LogConfig.genLogTag("WSInitialBlackList");
    private final Database mDatabase;
    private Context mContext;

    public WSInitialBlackList(final Context context, final WSParameterContainer c) {
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
        mDatabase.deleteBlackListNumbers();
        Log.d(TAG, "OK");
    }

    @Override
    public void onError() {
        Log.e(TAG, "unable to fetch result");
    }

    @Override
    public void onResult(final String name, final JSONObject result) {

        final String inputPhone = Iso2Phone.formatFullPhonenumber(result.optInt("country_code"), result.optString("phone"));

        // Log.d(TAG, "Checking (" + name + ") phone:" + inputPhone);

        try {
            Iso2Phone.convertPhoneNumberToInternationalFormat(inputPhone, mContext);
        } catch (final NumberParseException e) {
            Log.w(TAG, "Invalid phone", e);
            return;
        }

        if (name.equals(BL_PHONES)) {

            // Remote entry, no name or note - this is going to be fetched over network at runtime.
            mDatabase.insertBlackListedNumber(result.optString("caller"),
                    result.optString("phone"),
                    result.optInt("country_code"),
                    result.optString("country"),
                    Database.REMOTE_ENTRY,
                    "");

        } else if (name.equals(USER_BL_PHONES)) {

                mDatabase.insertBlackListedNumber(result.optString("caller"),
                        result.optString("phone"),
                        result.optInt("country_code"),
                        result.optString("country"),
                        Database.LOCAL_ENTRY,
                        result.optString("notes"));

            //Log.d(TAG, "Unhandled: " + USER_BL_PHONES);
        }
    }

    @Override
    public void onFinish(final boolean error) {
        Log.d(TAG, "finished initial BL update, error=" + error);
    }
}
