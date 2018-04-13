package com.reverdapp.webservice;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.google.i18n.phonenumbers.NumberParseException;

import org.json.JSONObject;

public class WSInitialWhitelist extends ListUpdaterBase {

    private static final String TAG = LogConfig.genLogTag("WSInitialWhitelist");
    private final Database mDatabase;
    private Context mContext;

	public WSInitialWhitelist(final Context context, final WSParameterContainer c) {
           super(context, WSName.WHITELIST, c, new String[] {USER_WL_PHONES});
           mDatabase = ((ReverdApp) getContext().getApplicationContext()).getDatabase();
           mContext = context;
	}

	@Override
	public void onInit() {

	}

	@Override
	public void onSuccess() {
           mDatabase.deleteWhiteListNumbers();
           Log.d(TAG, "OK");
	}

	@Override
	public void onResult(final String name, final JSONObject result) {

        final String inputPhone = Iso2Phone.formatFullPhonenumber(result.optInt("country_code"), result.optString("phone"));

        // Log.d(TAG, "Checking (" + name + ") phone:" + inputPhone);

        String formattedPhone = "";
        try {
            formattedPhone = Iso2Phone.convertPhoneNumberToInternationalFormat(inputPhone, mContext);
        } catch (final NumberParseException e) {
            Log.w(TAG, "Invalid phone", e);
            return;
        }

        final Cursor c = mDatabase.getBlackListedNumber(formattedPhone);
        if (c.getCount() > 0) {
            // If the BL contains this number it needs to be moved.
            c.moveToFirst();
            int id = c.getInt(c.getColumnIndex(DatabaseField.BLACKLIST_ID));
            mDatabase.moveFromBlackListToWhiteList(id);
        }
        else {
            // An entry added by the user.
            mDatabase.insertWhiteListedNumber(
                    result.optString("caller"),
                    result.optString("phone"),
                    result.optInt("country_code"),
                    result.optString("country"),
                    Database.LOCAL_ENTRY,
                    result.optString("notes"));
        }
    }

    @Override
	public void onError() {
        Log.e(TAG, "unable to fetch result");
	}

	@Override
	public void onFinish(final boolean error) {
        Log.d(TAG, "finished initial WL update, error=" + error);
	}
}
