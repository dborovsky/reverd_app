package com.reverdapp.Service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class DBAccessService extends IntentService {
   public static final String INSERT_TO_BLACKLIST = "Insert to blacklist";
    public static final String INSERT_TO_WHITE_LIST = "Insert to whitelist";
    private static final String TAG = LogConfig.genLogTag("DBAccessService");
    private String mNumber;

    public DBAccessService() {
        super("DBAccessService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            mNumber = intent.getStringExtra(Constants.PHONE_NUM);
            Log.d(TAG, " DB Service Action: " + intent.getAction());
            if (intent.getAction().equals(INSERT_TO_BLACKLIST)) {
                insertToBlackList();
            }
            else if (intent.getAction().equals(INSERT_TO_WHITE_LIST)) {
                insertToWhiteList();
            }
            else {
                Log.e(TAG, "Unhanled intent");
            }

        }
    }

    private void insertToBlackList() {
        ReverdApp reverdApp = (ReverdApp) this.getApplicationContext();
        Database db = reverdApp.getDatabase();
        String countryPrefix= Iso2Phone.getCountryPrefix(mNumber);
        String countryCode = Iso2Phone.getCountryCode(mNumber);
        String phoneWithoutPrefix = Iso2Phone.stripCountryPrefix(mNumber);
        Log.d(TAG, "insertToBlackList, input: '" + mNumber + "', number: '" + phoneWithoutPrefix + "', country prefix: '" + countryPrefix + "', countryCode: '" + countryCode + "'");
        // String number, String caller, String countryCode, String country, boolean isLocal, String note
        final String addedBy = reverdApp.getResources().getString(R.string.number_added_by_reverd_app);
        db.insertIntoBlackList(mNumber, phoneWithoutPrefix, "", countryPrefix, countryCode, true, addedBy);
    }

    private void insertToWhiteList() {
        ReverdApp reverdApp = (ReverdApp) this.getApplicationContext();
        Database db = reverdApp.getDatabase();
        String countryPrefix= Iso2Phone.getCountryPrefix(mNumber);
        String countryCode= Iso2Phone.getCountryCode(mNumber);
        String phoneWithoutPrefix = Iso2Phone.stripCountryPrefix(mNumber);
        Log.d(TAG, "insertToWhiteList, input: '" + mNumber + "', number: '" + phoneWithoutPrefix + "', country prefix: '" + countryPrefix + "', countryCode: '" + countryCode + "'");

        // String number, String caller, String countryCode, String country, boolean isLocal, String note
        final String addedBy = reverdApp.getResources().getString(R.string.number_added_by_reverd_app);
        db.insertToWhiteList(mNumber, phoneWithoutPrefix,"", countryPrefix, countryCode, true, addedBy);
    }


}
