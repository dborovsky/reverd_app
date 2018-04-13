package com.reverdapp.webservice;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;

import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by wojci on 9/4/15.
 */
public final class WSParameterContainer {
    private static final String TAG = LogConfig.genLogTag("WSParameterContainer");

    private final static String ARG_LANGCODE = "langcode";

    private final static String ARG_IMEI = "IMEI";
    private final static String ARG_PHONE = "phone";
    private final static String ARG_PLATFORM = "platform";

    private final static String ARG_PHONE_BL = "phone_bl";
    private final static String ARG_PHONE_WL = "phone_wl";
    private final static String ARG_COUNTRY = "country";
    private final static String ARG_NAME = "name";
    private final static String ARG_NOTES = "notes";

    private final static String ARG_CALLER = "caller";
    private final static String ARG_COUNTRY_CODE = "country_code";

    private final static String ARG_PHONES_TO_BL= "phones_to_bl";
    private final static String ARG_PHONES_TO_WL= "phones_to_wl";

    private final static String ARG_EMAIL = "email";
    private final static String ARG_FEEDBACK = "feedback";
    private final static String ARG_VERSION = "version";

    private final static String ARG_CALLER_PHONE = "caller_phone";
    private final static String ARG_CALLER_COUNTRY = "caller_country";
    private final static String ARG_CALLER_NAME = "caller_name";
    private final static String ARG_COMPLAINT = "complaint";

    private final static String VALUE_PLATFORM_NAME = "Android";

    private final static String ARG_SUBSCRIPTION_TERM = "subscription_term";
    private final static String ARG_SUBSCRIPTION_RECURRING = "recurring";

    private final HashMap<String, String> mParameters;

    public WSParameterContainer() {
        mParameters = new HashMap<String, String>();
    }

    // For chaining.
    public WSParameterContainer add(final String name, final String value) {
        mParameters.put(name, value);
        return this;
    }

    public HashMap<String, String> getParameters() {
        return mParameters;
    }

    private static WSParameterContainer createDefaultContainer(final Context c, final boolean useIMEI) {
        final ReverdApp app = (ReverdApp) c.getApplicationContext();
        final String phoneId = app.getPhoneId();
        final WSParameterContainer container = new WSParameterContainer();
        if (useIMEI) {
            container.add(ARG_IMEI, phoneId);
        } else {
            container.add(ARG_PHONE, phoneId);
        }
        container.add(ARG_PLATFORM, VALUE_PLATFORM_NAME);
        return container;
    }

    public static WSParameterContainer createComplaint(final Context context,
                                                       final String callerPhone,
                                                       final String callerCountry,
                                                       final String callerName,
                                                       final String complaint) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        return c.add(ARG_CALLER_PHONE, callerPhone).
                add(ARG_CALLER_COUNTRY, callerCountry).
                add(ARG_CALLER_NAME, callerName).
                add(ARG_COMPLAINT, complaint).
                add(ARG_LANGCODE, Locale.getDefault().getLanguage());
    }

    public static WSParameterContainer createAddToBlacklist(final Context context,
                                                            final String addToBlackListNumber,
                                                            final String country,
                                                            final String name,
                                                            final String notes) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        return c.add(ARG_PHONE_BL, addToBlackListNumber).
            add(ARG_COUNTRY, country).
            add(ARG_NAME, name).
            add(ARG_NOTES, notes).
            add(ARG_LANGCODE, Locale.getDefault().getLanguage());
    }

    public static WSParameterContainer createAddToWhiteList(final Context context,
                                                            final String phoneNumber,
                                                            final String country,
                                                            final String name,
                                                            final String notes) {
        // IMEI
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, true);
        return c.add(ARG_PHONE_WL, phoneNumber).
            add(ARG_COUNTRY, country).
            add(ARG_NAME, name).
            add(ARG_NOTES, notes).
            add(ARG_LANGCODE, Locale.getDefault().getLanguage());
    }

    public static WSParameterContainer createDeleteFromWhitelist(final Context context,
                                                            final String phoneNumber,
                                                            final String country) {
        // IMEI
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, true);
        return c.add(ARG_PHONE_WL, phoneNumber).
                add(ARG_COUNTRY, country);
    }

    public static WSParameterContainer createDeleteFromBlacklist(final Context context,
                                                                 final String phoneNumber,
                                                                 final String country) {
        // IMEI
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, true);
        return c.add(ARG_PHONE_BL, phoneNumber).
                add(ARG_COUNTRY, country);
    }

    public static WSParameterContainer createDetails(final Context context, final String phone) {
        final WSParameterContainer c = new WSParameterContainer();
        c.add(ARG_PHONE, phone);
        return c;
    }

    public static WSParameterContainer createBlacklist(final Context context) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        return c;
    }

    public static WSParameterContainer createWhitelist(final Context context) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        return c;
    }

    // Whitelist to Blacklist
    public static WSParameterContainer createMoveWLtoBL(final Context context, final ArrayList<NumberListModel> numbers) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());
        final JSONArray jsMain = new JSONArray();
        for(int i=0;i<numbers.size();i++) {

            if(numbers.get(i).isSelected()) {
                final JSONObject jsSelectedNumber = new JSONObject();
                try {
                    jsSelectedNumber.put(ARG_CALLER, numbers.get(i).getCaller());
                    jsSelectedNumber.put(ARG_COUNTRY, numbers.get(i).getCountry());
                    jsSelectedNumber.put(ARG_COUNTRY_CODE, numbers.get(i).getCountryCode());
                    jsSelectedNumber.put(ARG_PHONE, numbers.get(i).getPhoneNumber());
                } catch (JSONException e) {
                    Log.e(TAG, "generateJSON", e);
                }
                jsMain.put(jsSelectedNumber);
            }
        }
        c.add(ARG_PHONES_TO_BL, jsMain.toString());
        return c;
    }

    // Blacklist to Whitelist
    public static WSParameterContainer createMoveBLtoWL(final Context context, final ArrayList<NumberListModel> numbers) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());
        final JSONArray jsMain = new JSONArray();
        for(int i=0;i<numbers.size();i++) {
            if(numbers.get(i).isSelected()) {
                final JSONObject jsSelectedNumber = new JSONObject();
                try {
                    jsSelectedNumber.put(ARG_CALLER, numbers.get(i).getCaller());
                    jsSelectedNumber.put(ARG_COUNTRY, numbers.get(i).getCountry());
                    jsSelectedNumber.put(ARG_COUNTRY_CODE, numbers.get(i).getCountryCode());
                    jsSelectedNumber.put(ARG_PHONE, numbers.get(i).getPhoneNumber());
                } catch (JSONException e) {
                    Log.e(TAG, "generateJSON", e);
                }
                jsMain.put(jsSelectedNumber);
            }

        }
        c.add(ARG_PHONES_TO_WL, jsMain.toString());
        return c;
    }

    public static WSParameterContainer createAreaList(final Context context) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());
        return c;
    }

    public static WSParameterContainer createRegistration(final Context context) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);
        c.add(ARG_VERSION, Build.VERSION.RELEASE + "");
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());
        return c;
    }

    public static WSParameterContainer createSync(final Context context, final String email) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);

        c.add(ARG_EMAIL, email);
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());

        return c;
    }

    public static WSParameterContainer createFeedback(final Context context, final String feedback) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);

        c.add(ARG_FEEDBACK, feedback);
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());

        return c;
    }

    public static WSParameterContainer createSubscriptionCheck(final Context context) {
        // Use IMEI:
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, true);
        return c;
    }

    public static WSParameterContainer createSubscription(final Context context, int subscriptionTerm, int recurring) {
        final WSParameterContainer c = WSParameterContainer.createDefaultContainer(context, false);

        c.add(ARG_SUBSCRIPTION_TERM, Integer.toString(subscriptionTerm));
        c.add(ARG_SUBSCRIPTION_RECURRING, Integer.toString(recurring));
        c.add(ARG_LANGCODE, Locale.getDefault().getLanguage());

        return c;
    }
}
