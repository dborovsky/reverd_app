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

public class WSWhiteListMerge extends ListUpdaterBase {

    private static final String TAG = LogConfig.genLogTag("WSWhiteListMerge");
    private final Database mDatabase;
    private Context mContext;

    public WSWhiteListMerge(final Context context, final WSParameterContainer c) {
        super(context, WSName.WHITELIST, c, new String[] { USER_WL_PHONES });
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
        if (name.equals(USER_WL_PHONES)) {
            onUserWlPhoneResult(result);
        }
    }

    // The user's entries.
    private void onUserWlPhoneResult(final JSONObject result) {
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

        // Log.d(TAG, "Merging WL user entry: " + fullNumber);

        final Cursor wlCursor = mDatabase.getWhiteListedNumber(fullNumber);

        if (wlCursor.getCount() > 0) {
            // Assumption: nothing to do.
        }
        else {
            mDatabase.insertWhiteListedNumber(result.optString("caller"),
                    result.optString("phone"),
                    result.optInt("country_code"),
                    result.optString("country"),
                    Database.LOCAL_ENTRY,
                    result.optString("notes"));
        }

        wlCursor.close();
    }

    @Override
    public void onFinish(final boolean error) {
        Log.d(TAG, "finished BL merge, error=" + error);
    }
}
