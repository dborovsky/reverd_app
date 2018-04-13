package com.reverdapp.utils;

import android.test.ActivityUnitTestCase;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Log;

import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.view.HomeActivity;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TestIso2Phone extends ActivityUnitTestCase<HomeActivity> {

    static final String PHONE_1 = "+4542443523";
    static final String PHONE_1_NO_PREFIX = "42443523";
    static final String PHONE_1_PREFIX = "45";

    static final String PHONE_2 = "+16044400132";
    static final String PHONE_2_NO_PREFIX = "6044400132";
    static final String PHONE_2_PREFIX = "1";

    static final String PHONE_3 = "+16048162244";
    static final String PHONE_3_NO_PREFIX = "6048162244";
    static final String PHONE_3_PREFIX = "1";

    static final String PHONE_4 = "16049245566";

    public TestIso2Phone() {
        super(HomeActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testGetCountryCode() {

        String countryCode = Iso2Phone.getCountryCode(PHONE_1);
        Log.d("TEST", "countryCode = " + countryCode);
        assertTrue(countryCode.equals("DK"));

        countryCode = Iso2Phone.getCountryCode(PHONE_2);
        Log.d("TEST", "countryCode = " + countryCode);
        assertTrue(countryCode.equals("US"));

        countryCode = Iso2Phone.getCountryCode(PHONE_3);
        Log.d("TEST", "countryCode = " + countryCode);
        assertTrue(countryCode.equals("US"));
    }

    @Test
    public void testStripCountryPrefix() {

        String phoneWithoutPrefix = Iso2Phone.stripCountryPrefix(PHONE_1);
        Log.d("TEST", "phoneWithoutPrefix = " + phoneWithoutPrefix);
        assertTrue(phoneWithoutPrefix.equals(PHONE_1_NO_PREFIX));

        phoneWithoutPrefix = Iso2Phone.stripCountryPrefix(PHONE_2);
        Log.d("TEST", "phoneWithoutPrefix = " + phoneWithoutPrefix);
        assertTrue(phoneWithoutPrefix.equals(PHONE_2_NO_PREFIX));

        phoneWithoutPrefix = Iso2Phone.stripCountryPrefix(PHONE_3);
        Log.d("TEST", "phoneWithoutPrefix = " + phoneWithoutPrefix);
        assertTrue(phoneWithoutPrefix.equals(PHONE_3_NO_PREFIX));
    }

    @Test
    public void testGetCountryPrefix() {

        String prefix = Iso2Phone.getCountryPrefix(PHONE_1);
        Log.d("TEST", "prefix = " + prefix);
        assertTrue(prefix.equals(PHONE_1_PREFIX));

        prefix = Iso2Phone.getCountryPrefix(PHONE_2);
        Log.d("TEST", "prefix = " + prefix);
        assertTrue(prefix.equals(PHONE_2_PREFIX));

        prefix = Iso2Phone.getCountryPrefix(PHONE_3);
        Log.d("TEST", "prefix = " + prefix);
        assertTrue(prefix.equals(PHONE_3_PREFIX));
    }

    @Test
    public void testRealPhoneNumbers() {

        String prefix = Iso2Phone.getCountryPrefix(PHONE_4);
        Log.d("TEST", "prefix = " + prefix);
        //assertTrue(prefix.equals(PHONE_1_PREFIX));

    }
}