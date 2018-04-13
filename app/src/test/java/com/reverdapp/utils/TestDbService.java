package com.reverdapp.utils;

import android.app.Application;
import android.content.Intent;
import android.test.ServiceTestCase;
import android.util.Log;

import com.reverdapp.ReverdApp;
import com.reverdapp.Service.DBAccessService;
import com.reverdapp.database.Database;
import com.reverdapp.utils.Constants;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by wojci on 5/30/15.
 */
public class TestDbService extends ServiceTestCase<FakeDBAccessService> {
    /**
     * Constructor
     *
     * @param serviceClass The type of the service under test.
     */

    static final String PHONE_1 = "+4542443523";
    static final String PHONE_1_NO_PREFIX = "42443523";
    static final String PHONE_1_PREFIX = "45";

    static final String PHONE_2 = "+16044400132";
    static final String PHONE_2_NO_PREFIX = "6044400132";
    static final String PHONE_2_PREFIX = "1";

    static final String PHONE_3 = "+16048162244";
    static final String PHONE_3_NO_PREFIX = "6048162244";
    static final String PHONE_3_PREFIX = "1";

    private Database mDatabase;

    public TestDbService() {
        super(FakeDBAccessService.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();

        ReverdApp app = new ReverdApp();

        Database db = new Database(getContext());
        db.createDataBase();

        app.setDatabase(db);
        setApplication(app);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();

        ReverdApp app = (ReverdApp) getApplication();
        assertNotNull(app.getDatabase());
        Database db = app.getDatabase();

        db.deleteBlackListNumbers();
        db.deleteWhiteListNumbers();
    }
/*
    @Test
    public void testFailure() {
        assertEquals(true, false);
    }
*/
    @Test
    public void testBlackListInsert() {
        checkBlackListFlow(PHONE_1);
        checkBlackListFlow(PHONE_2);
        checkBlackListFlow(PHONE_3);
    }

    @Test
    public void testWhiteListInsert() {
        checkWhiteListFlow(PHONE_1);
        checkWhiteListFlow(PHONE_2);
        checkWhiteListFlow(PHONE_3);
    }

    private void checkBlackListFlow(final String number) {
        ReverdApp app = (ReverdApp) getApplication();
        assertNotNull(app.getDatabase());
        Database db = app.getDatabase();
        int res = db.checkIfNumIsBlackOrWhite(number);
        /*
        if (res != Constants.NUM_NOT_PRESENT) {
            db.deleteBlackListNumbers();
            db.deleteWhiteListNumbers();
        }
        res = db.checkIfNumIsBlackOrWhite(number);
        */
        assertEquals(Constants.NUM_NOT_PRESENT, res);

        Intent intent = new Intent();
        intent.setAction(FakeDBAccessService.INSERT_TO_BLACKLIST);
        intent.putExtra(Constants.PHONE_NUM, number);
        startService(intent);

        // Check that the db has been update correctly.
        res = db.checkIfNumIsBlackOrWhite(number);
        assertEquals(Constants.NUM_BLACKLISTED, res);

    }

    private void checkWhiteListFlow(final String number) {
        ReverdApp app = (ReverdApp) getApplication();
        assertNotNull(app.getDatabase());
        Database db = app.getDatabase();
        int res = db.checkIfNumIsBlackOrWhite(number);
        /*
        if (res != Constants.NUM_NOT_PRESENT) {
            db.deleteBlackListNumbers();
            db.deleteWhiteListNumbers();
        }
        res = db.checkIfNumIsBlackOrWhite(number);
        */
        assertEquals(Constants.NUM_NOT_PRESENT, res);

        Intent intent = new Intent();
        intent.setAction(FakeDBAccessService.INSERT_TO_WHITE_LIST);
        intent.putExtra(Constants.PHONE_NUM, number);
        startService(intent);

        // Check that the db has been update correctly.
        res = db.checkIfNumIsBlackOrWhite(number);
        assertEquals(Constants.NUM_WHITELISTED, res);

    }

}
