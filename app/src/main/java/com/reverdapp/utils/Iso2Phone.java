package com.reverdapp.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder;

import java.util.Locale;

/**
 * This class is used to get Country Code from their initials : like IN -> +91
 * for India
 */
public class Iso2Phone {
    private static final String TAG = LogConfig.genLogTag("Iso2Phone");

    private static PhoneNumberUtil mPhoneNumberUtil;
    private static PhoneNumberOfflineGeocoder mPhoneNumberOfflineGeocoder;

    static {
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();
        mPhoneNumberOfflineGeocoder = PhoneNumberOfflineGeocoder.getInstance();
    }

    public static String getIMEI(final Context c) {
        TelephonyManager tm = (TelephonyManager)c.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    @SuppressLint("DefaultLocale")
    public static String getPhone(String code) {
        int codeForRegion = mPhoneNumberUtil.getCountryCodeForRegion(code);
        return String.format("%d", codeForRegion);
    }

    public static String getCountryPrefix(final String phoneNumber) {
        try {
            Phonenumber.PhoneNumber numberProto = mPhoneNumberUtil.parse(phoneNumber, "");
            String region = mPhoneNumberUtil.getRegionCodeForNumber(numberProto);
            int codeForRegion = mPhoneNumberUtil.getCountryCodeForRegion(region);
            return String.format("%d", codeForRegion);
        } catch (NumberParseException e) {
            Log.e(TAG, "Parsing number exception", e);
            return "";
        }
    }

    public static String getCountryCode(final String phoneNumber) {
        try {
            Phonenumber.PhoneNumber numberProto = mPhoneNumberUtil.parse(phoneNumber, "");
            return mPhoneNumberUtil.getRegionCodeForNumber(numberProto);
        } catch (NumberParseException e) {
            Log.e(TAG, "Parsing number exception", e);
            return "";
        }
    }

    /* Remove country prefix. For example +4522403503 becomes 22403503 */
    public static String stripCountryPrefix(final String number) {

        final String cc = getCountryPrefix(number);
        //Log.d(TAG, "stripCountryPrefix, number = " + number + ", cc = " + cc);
        final StringBuffer sb = new StringBuffer(number);
        if (sb.charAt(0) == '+')
        {
            sb.deleteCharAt(0);
        }

        if (cc.length() > 0) {
            sb.delete(0, cc.length());
        }

        //Log.d(TAG, "stripCountryPrefix, result = " + sb.toString());

        return sb.toString();
   }

    public static String convertPhoneNumberToInternationalFormat(final String number, final Context c)
            throws NumberParseException {

        TelephonyManager telephonyManager = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        final String simCountryIso = telephonyManager.getSimCountryIso();

        Phonenumber.PhoneNumber numberProto = null;
        if (number.startsWith("+")) {
            numberProto = mPhoneNumberUtil.parse(number, "");
        }
        else {
            numberProto = mPhoneNumberUtil.parse("+" + number, "");
        }

        return mPhoneNumberUtil.format(numberProto, PhoneNumberUtil.PhoneNumberFormat.E164);
    }

   public static String convertPhoneNumberToInternationalFormatNoThrow(final String number, final Context c) {
       try {
           return convertPhoneNumberToInternationalFormat(number,c);
       } catch (NumberParseException e) {
           Log.e(TAG, "NumberParseException was thrown: ", e);
           Log.d(TAG, "Invalid number " + number + ", forwarding as it is.");
           return number;
       }
    }

    /* Get the country and possibly region of a phone number */
    public static String getCountryAndRegion(Context c, final String number) {

        try {
            Phonenumber.PhoneNumber numberProto = null;
            if (number.startsWith("+")) {
                numberProto = mPhoneNumberUtil.parse(number, "");
            }
            else {
                numberProto = mPhoneNumberUtil.parse("+" + number, "");
            }

            // int countrycode = numberProto.getCountryCode();

            final Locale current = c.getResources().getConfiguration().locale;

            return mPhoneNumberOfflineGeocoder.getDescriptionForNumber(numberProto, current);

        } catch (NumberParseException e) {
            Log.e(TAG, "NumberParseException was thrown: ", e);
        }
        return "";
    }

    /*
     * Functions used to format phone numbers.
     *
     */

    public static String formatFullPhonenumber(int countryCode, String number) {
        final StringBuffer sb = new StringBuffer();
        return sb.append("+").
                append(countryCode).
                append("-").
                append(number).
                toString();
    }

    public static String formatFullPhonenumber(String countryCode, String number) {
        final StringBuffer sb = new StringBuffer();
        return sb.append("+").
                append(countryCode).
                append("-").
                append(number).
                toString();
    }
}
