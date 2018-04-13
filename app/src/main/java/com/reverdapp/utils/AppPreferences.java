package com.reverdapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.reverdapp.R;

/**
 * Created by wojci on 8/13/15.
 */

final public class AppPreferences {
    private static final String TAG = LogConfig.genLogTag("AppPreferences");

    //
    // Keys used to read preferences:
    //
    // Indicates that the app executes has executed before.
    public static final String INITIALIZED = "INITIALIZED";
    // The help slide has been shown at least once
    public static final String HELPSHOWN = "HELPSHOWN";
    // Used to identify the user of this app / phone.
    //public static final String PREF_SIM_ID = "PREF_SIM_ID";
    public static final String PREF_SIM_COUNTRY_CODE = "PREF_SIM_COUNTRY_CODE";
    public static final String PREF_SYNC_EMAIL = "PREF_SYNC_EMAIL";
    public static final String PREF_BLOCK_COUNTRY_AREA = "PREF_BLOCK_COUNTRY_AREA";
    public static final String PREF_BLOCK_WITH_NO_CALLER_ID = "PREF_BLOCK_WITH_NO_CALLER_ID";
    public static final String PREF_SELECTED_COUNTRY_CODES = "PREF_SELECTED_COUNTRY_CODES";
    public static final String PREF_CALL_BLOCKING_ENABLED = "PREF_CALL_BLOCKING_ENABLED";

    public static final String PREF_LAST_SYNC_DATE = "PREF_LAST_SYNC_DATE";

    public static final Integer ACTION_SHOW_SLIDES = 7867;

    private SharedPreferences mSharedPreferences;

    public AppPreferences(Context c) {
        mSharedPreferences = c.getSharedPreferences("Reverd", Context.MODE_PRIVATE);
    }

    public void set(final String name, final String value) {
        Log.d(TAG, "setPreference: " + name + " " + value);
        mSharedPreferences.edit().
                putString(name, value).
                commit();
    }

    public void set(final String name, final Boolean value) {
        Log.d(TAG, "setPreference: " + name + " " + value);
        mSharedPreferences.edit().
                putBoolean(name, value).
                commit();
    }

    public String get(final String name, final String defValue) {
        final String value = mSharedPreferences.getString(name, defValue);
        Log.d(TAG, "get: " + name + " " + value);
        return value;
    }

    public Boolean get(final String name, final Boolean defValue) {
        final Boolean value = mSharedPreferences.getBoolean(name, defValue);
        Log.d(TAG, "get: " + name + " " + value);
        return value;
    }

    public boolean contains(final String name) {
        return mSharedPreferences.contains(name);
    }

    /*
    public boolean isSet(final String name) {
        boolean value = mSharedPreferences.getBoolean(name, false);
        Log.d(TAG, "isSet: " + name + " " + value);
        return value;
    }
    */

    // In-app billing Base64-encoded RSA public key from google play:
    public static final String AppBillingKey =
            // key:
            //"MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAm6Czth3QGErsEKlPNcmUYcd4pS87H1D4YjFK8QGgCERBsglRtTjt8PoS/jkXPsgSX5p63WH24B9sOqY3o8ye6APlaRrCqJ4igTvAjhcA3/gbU2xaXDQQIkTEwRlggcGdrEmQbIX/kRBGZBUCnIOcZmQIMtek8MHB5aWcuiVqJmsVurEAMY06eeOIoIL5xMPaxprzbrSbVeEWAKaYeS2RkznQGsGNAqEjkKd/TXTCXBNW0MXVG4SsrkU6mV2L9qKMxEEktPoBfS/p4puQm3u1GyUfr4PgXf3lWHdWg8USqKzXWqAHAyeezeo+ll+tyUYWSewOaSvDAjYPfv7qobYVKwIDAQAB";
            // encrypted key (as per google recommendation):
            "rPXOmFpNleUp2b5WliVrdc2a7g5KQxmm1B1Yu5piPHxEG+O/KW0qvR0s35/iHrfn7a4RVMl/xAwKqI/V6sQmL3zhO6jW13XvJxOtQ6gFPkQb5pxJ/4/GsTTaZDmssba2tarQQX9DReG1C5OW2BBZPnMHEF2zxvrpetWTtX9LerlyB3Wz61Q++ib0/ml5tsNrNRRUBUF7qEYaQj4rJvH9Xd69IaBQosxrh3QD//SLqMzSfl27TKnw4jbTkGUG8IOr02ereQ7721j0ZR8Pm89rjyocRbSaOUM5AWknwGDiW0BeRWmcjcsDb6AuI8WjAEcTtFqh05zy+Ke/qTJBQZaEUtvtnoFwxLQGao2J5lZjTI1tf5mGbKkOYCPfD5JFv44FnA8tOwY9H4l0b+jbGoW5Y1kRTnJ72JMUUza2EROXLOl1+qwvtFcqAZpDL9esXfS5h9AilEFM+p2IGxAmTQp6cA37PEmY47PHaTTugfJRpNZZ2KDUT1A/EiOWPgGUtRAtDjS1aVFWtql/wjzxhPjGQw==";

}
