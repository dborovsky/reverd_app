package com.reverdapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import com.reverdapp.BuildConfig;
import com.reverdapp.stats.LogDate;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Calendar;

public final class Database extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String TAG = LogConfig.genLogTag("Database");
    private static String DB_NAME = "ReverdApp-ng.sqlite";
    private SQLiteDatabase myDataBase;
    private Context mContext;
    private String DATABASE_PATH = "";
    private long localWhitelistSize;
    //private DatabaseField databaseField;

    // Indicates that a number entry is local.
    public static final int LOCAL_ENTRY = 1;
    public static final int REMOTE_ENTRY = 0;

    // URIs used to detech changes in the different tables.
    public static final Uri URI_TABLE_BLACKLIST = Uri.parse("sqlite://" + BuildConfig.APPLICATION_ID + "/" + DatabaseField.TABLE_BLACKLIST);
    public static final Uri URI_TABLE_WHITELIST = Uri.parse("sqlite://" + BuildConfig.APPLICATION_ID + "/" + DatabaseField.TABLE_WHITELIST);
    public static final Uri URI_TABLE_CALLDETAIL = Uri.parse("sqlite://" + BuildConfig.APPLICATION_ID + "/" + DatabaseField.TABLE_CALLDETAIL);
    public static final Uri URI_TABLE_CALLSTATS = Uri.parse("sqlite://" + BuildConfig.APPLICATION_ID + "/" + DatabaseField.TABLE_CALLSTATS);
    // NOTE: assumption: the other tables are not updated.


    public Database(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
        this.mContext = context;
        DATABASE_PATH = "/data/data/" + context.getPackageName() + "/databases/";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // ---Create the database---
    public void createDataBase() throws IOException {

        // ---Check whether database is already created or not---
        boolean dbExist = checkDataBase();

        if (!dbExist) {
            this.getReadableDatabase();
            try {
                // ---If not created then copy the database---
                copyDataBase();
                this.close();
            } catch (IOException e) {
                Log.e(TAG, "Error copying database", e);
                throw new Error("Error copying database");
            }
        }
    }

    // --- Check whether database already created or not---
    private boolean checkDataBase() {
        try {
            final String myPath = DATABASE_PATH + DB_NAME;
            final File f = new File(myPath);
            if (f.exists())
                return true;
            else
                return false;
        } catch (SQLiteException e) {
            Log.e(TAG, "checkDataBase", e);
            return false;
        }

    }

    // --- Copy the database to the output stream---
    private void copyDataBase() throws IOException {

        final InputStream myInput = mContext.getAssets().open(DB_NAME);
        final String outFileName = DATABASE_PATH + DB_NAME;

        final OutputStream myOutput = new FileOutputStream(outFileName);

        final byte[] buffer = new byte[1024];
        int length;
        while ((length = myInput.read(buffer)) > 0) {
            myOutput.write(buffer, 0, length);
        }

        myOutput.flush();
        myOutput.close();
        myInput.close();
    }

    /**
     * Method is used for copying database on to sd card.
     */
    public void copyDatabaseToSdCard() {
        try {
            File f1 = new File(DATABASE_PATH + DB_NAME);
            if (f1.exists()) {
                File f2 = new File(Environment.getExternalStorageDirectory().getAbsoluteFile() + "/" + DB_NAME);
                f2.createNewFile();
                InputStream in = new FileInputStream(f1);
                OutputStream out = new FileOutputStream(f2);
                byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                out.close();
            }
        } catch (FileNotFoundException e1) {
            Log.e(TAG, "copyDatabaseToSdCard", e1);
            System.exit(0);
        } catch (IOException e2) {
            Log.e(TAG, "copyDatabaseToSdCard", e2);
        }
    }

    public SQLiteDatabase openDataBase() throws SQLException {
        // --- Open the database---
        final String myPath = DATABASE_PATH + DB_NAME;

        myDataBase = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        return myDataBase;
    }

    public void closeDatabase() {
        myDataBase.close();
        SQLiteDatabase.releaseMemory();
    }

    /**
     * Insert data in table
     */

    // Called with data from a web service.
    public void insertBlackListedNumber(final String caller,
                                        final String phoneNumber,
                                        final int countryCode,
                                        final String country,
                                        final int isLocal,
                                        final String note) {

        ContentValues values = null;
        Cursor cursor = null;
        // Store the phone number in international format.
        final String fullNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(Iso2Phone.formatFullPhonenumber(countryCode, phoneNumber),mContext);
        try {
            cursor = getBlackListedNumber(fullNumber);
            if (cursor != null && cursor.getCount() > 0) {
                values = new ContentValues();
                values.put(DatabaseField.BLACKLIST_CALLER, caller);
                values.put(DatabaseField.BLACKLIST_PHONE, phoneNumber);
                values.put(DatabaseField.BLACKLIST_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.BLACKLIST_COUNTRY, country);
                values.put(DatabaseField.BLACKLIST_FULL_NUMBER, fullNumber);
                values.put(DatabaseField.BLACKLIST_IS_LOCAL, isLocal);
                values.put(DatabaseField.BLACKLIST_NOTE, note);
                myDataBase.update(DatabaseField.TABLE_BLACKLIST, values, DatabaseField.BLACKLIST_PHONE + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null);
                // Log.d(TAG, "Updated BL into db: " + values.toString());
            } else {
                values = new ContentValues();
                values.put(DatabaseField.BLACKLIST_CALLER, caller);
                values.put(DatabaseField.BLACKLIST_PHONE, phoneNumber);
                values.put(DatabaseField.BLACKLIST_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.BLACKLIST_COUNTRY, country);
                values.put(DatabaseField.BLACKLIST_FULL_NUMBER, fullNumber);
                values.put(DatabaseField.BLACKLIST_IS_LOCAL, isLocal);
                values.put(DatabaseField.BLACKLIST_NOTE, note);
                myDataBase.insert(DatabaseField.TABLE_BLACKLIST, null, values);
                // Log.d(TAG, "Inserted BL into db: " + values.toString());
            }
        } catch (Exception e) {
            Log.e(TAG, "insertBlackListedNumber", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tableUpdated(URI_TABLE_BLACKLIST);
    }

    // Called with data from a web service.
    public void insertWhiteListedNumber(String caller,
                                        String phoneNumber,
                                        int countryCode,
                                        String country,
                                        int isLocal,
                                        String note) {

        ContentValues values = null;
        Cursor cursor = null;
        // Store the phone number in international format.
        final String fullNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(Iso2Phone.formatFullPhonenumber(countryCode, phoneNumber),mContext);

        try {
            cursor = getWhiteListedNumber(fullNumber);
            if (cursor != null && cursor.getCount() > 0) {

                values = new ContentValues();
                values.put(DatabaseField.WHITELIST_CALLER, caller);
                values.put(DatabaseField.WHITELIST_PHONE, phoneNumber);
                values.put(DatabaseField.WHITELIST_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.WHITELIST_COUNTRY, country);
                values.put(DatabaseField.WHITELIST_FULL_NUMBER, fullNumber);
                values.put(DatabaseField.WHITELIST_IS_LOCAL, isLocal);
                values.put(DatabaseField.WHITELIST_NOTE, note);
                // Log.d(TAG, "insertWhiteListedNumber options(update): " + values.toString());
                myDataBase.update(DatabaseField.TABLE_WHITELIST, values, DatabaseField.WHITELIST_PHONE + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null);
            } else {
                values = new ContentValues();
                values.put(DatabaseField.WHITELIST_CALLER, caller);
                values.put(DatabaseField.WHITELIST_PHONE, phoneNumber);
                values.put(DatabaseField.WHITELIST_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.WHITELIST_COUNTRY, country);
                values.put(DatabaseField.WHITELIST_FULL_NUMBER, fullNumber);
                values.put(DatabaseField.WHITELIST_IS_LOCAL, isLocal);
                values.put(DatabaseField.WHITELIST_NOTE, note);
                Log.d(TAG, "insertWhiteListedNumber options (insert): " + values.toString());
                myDataBase.insert(DatabaseField.TABLE_WHITELIST, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "insertWhiteListedNumber", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        tableUpdated(URI_TABLE_WHITELIST);
    }

    public void insertCountry(final int callingCode,
                              final String countryCode,
                              final String countryName,
                              final boolean isSelected,
                              final int id) {

        ContentValues values = null;
        Cursor cursor = null;
        try {

            cursor = getCountry(countryName);
            if (cursor != null && cursor.getCount() > 0) {

                values = new ContentValues();
                values.put(DatabaseField.CALLING_CODE, callingCode);
                values.put(DatabaseField.COUNTRY_CODE, countryCode);
                values.put(DatabaseField.COUNTRY_NAME, countryName);
                values.put(DatabaseField.COUNTRY_IS_SELECTED, isSelected);
                values.put(DatabaseField.COUNTRY_ID, id);

                myDataBase.update(DatabaseField.TABLE_COUNTRY, values, DatabaseField.COUNTRY_NAME + " = " + DatabaseUtils.sqlEscapeString(countryName), null);


            } else {

                values = new ContentValues();
                values.put(DatabaseField.CALLING_CODE, callingCode);
                values.put(DatabaseField.COUNTRY_CODE, countryCode);
                values.put(DatabaseField.COUNTRY_NAME, countryName);
                values.put(DatabaseField.COUNTRY_IS_SELECTED, isSelected);
                values.put(DatabaseField.COUNTRY_ID, id);

                myDataBase.insert(DatabaseField.TABLE_COUNTRY, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "insertCountry", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void insertArea(String countryCode, String countryName, String stateCode, String stateName, boolean isSelected) {

        ContentValues values = null;
        Cursor cursor = null;
        int int_selected = 0;
        if (isSelected) {
            int_selected = 1;
        }

        try {
            cursor = getAreaOfCountry(countryCode, stateCode);
            if (cursor != null && cursor.getCount() > 0) {

                values = new ContentValues();
                values.put(DatabaseField.AREA_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.AREA_COUNTRY_NAME, countryName);
                values.put(DatabaseField.AREA_CODE, stateCode);
                values.put(DatabaseField.AREA_NAME, stateName);
                values.put(DatabaseField.AREA_IS_SELECTED, int_selected);

                myDataBase.update(DatabaseField.TABLE_AREA, values, DatabaseField.AREA_COUNTRY_CODE + " = " + DatabaseUtils.sqlEscapeString(countryCode) + " AND " + DatabaseField.AREA_CODE + " = " + DatabaseUtils.sqlEscapeString(stateCode), null);

            } else {

                values = new ContentValues();
                values.put(DatabaseField.AREA_COUNTRY_CODE, countryCode);
                values.put(DatabaseField.AREA_COUNTRY_NAME, countryName);
                values.put(DatabaseField.AREA_CODE, stateCode);
                values.put(DatabaseField.AREA_NAME, stateName);
                values.put(DatabaseField.AREA_IS_SELECTED, int_selected);

                myDataBase.insert(DatabaseField.TABLE_AREA, null, values);
            }
        } catch (Exception e) {
            Log.e(TAG, "insertArea", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Called from UI.
    public void insertIntoBlackList(String fullNumber,
                                    String numberWithoutPrefix,
                                    String caller, /* not used */
                                    String countryCode,
                                    String country,
                                    boolean isLocal,
                                    String note) {
        String table;
        ContentValues newNumValue = null;
        try {
            SetupDbPtr();
            if (checkIfInBlackList(fullNumber) == Constants.NUM_NOT_PRESENT) {
                table = DatabaseField.TABLE_BLACKLIST;

                newNumValue = new ContentValues();
                newNumValue.put(DatabaseField.BLACKLIST_CALLER, caller);
                newNumValue.put(DatabaseField.BLACKLIST_PHONE, numberWithoutPrefix);
                newNumValue.put(DatabaseField.BLACKLIST_COUNTRY_CODE, countryCode);
                newNumValue.put(DatabaseField.BLACKLIST_COUNTRY, country);
                newNumValue.put(DatabaseField.BLACKLIST_FULL_NUMBER, fullNumber);
                newNumValue.put(DatabaseField.BLACKLIST_IS_LOCAL, isLocal);
                newNumValue.put(DatabaseField.BLACKLIST_NOTE, note);
                // Log.d(TAG, "insertIntoBlackList options: " + newNumValue.toString());
                myDataBase.insert(table, null, newNumValue);
            }
            else {
                Log.w(TAG, "number " + fullNumber + " already present in BL.");
            }

        } catch (Exception ex) {
            Log.d(TAG, "Exception while inserting black list number: " + ex.getMessage());
        }

        tableUpdated(URI_TABLE_BLACKLIST);
    }

    // Called from UI.
    public void insertToWhiteList(String fullNumber,
                                  String numberWithoutPrefix,
                                  String caller, /* not used */
                                  String countryCode,
                                  String country,
                                  boolean isLocal,
                                  String note) {
        try {
            SetupDbPtr();
            if (checkIfInBlackList(fullNumber) == Constants.NUM_NOT_PRESENT) {
                String table = DatabaseField.TABLE_WHITELIST;
                ContentValues newNumValue = new ContentValues();
                newNumValue.put(DatabaseField.WHITELIST_FULL_NUMBER, fullNumber);
                newNumValue.put(DatabaseField.WHITELIST_PHONE, numberWithoutPrefix);
                newNumValue.put(DatabaseField.WHITELIST_CALLER, caller);
                newNumValue.put(DatabaseField.WHITELIST_COUNTRY_CODE, countryCode);
                newNumValue.put(DatabaseField.WHITELIST_COUNTRY, country);
                newNumValue.put(DatabaseField.WHITELIST_IS_LOCAL, isLocal);
                newNumValue.put(DatabaseField.WHITELIST_NOTE, note);

                // Log.d(TAG, "insertToWhiteList options: " + newNumValue.toString());

                myDataBase.insert(table, null, newNumValue);
            }
        } catch (Exception ex) {
            Log.e(TAG, "Exception while inserting black list number: " + ex.getMessage());
        }

        tableUpdated(URI_TABLE_WHITELIST);
    }

    // Called with lost data from a web service.
    public void insertLostRequest(final String url,
                                  final String postData
                                 ) {

        ContentValues values = null;
        Cursor cursor = null;
        // Store the phone number in international format.
        try {
            values = new ContentValues();
            values.put(DatabaseField.LOSTREQUEST_URL, url);
            values.put(DatabaseField.LOSTREQUEST_POSTDATA, postData);
            values.put(DatabaseField.LOSTREQUEST_RETRYCOUNT, 0);
            myDataBase.insert(DatabaseField.TABLE_LOSTREQUESTS, null, values);
            // Log.d(TAG, "Inserted lost request into db: " + values.toString());
        } catch (Exception e) {
            Log.e(TAG, "insertLostRequest", e);
        }
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // Called to set retry count of a lost request
    public void updateLostRequest(final int id,
                                  final String url,
                                  final String postData,
                                  final int count
                                 ) {
         try {
           ContentValues values = new ContentValues();
           values.put(DatabaseField.LOSTREQUEST_URL, url);
           values.put(DatabaseField.LOSTREQUEST_POSTDATA, postData);
           values.put(DatabaseField.LOSTREQUEST_RETRYCOUNT, count);
           myDataBase.update(DatabaseField.TABLE_LOSTREQUESTS, values, DatabaseField.LOSTREQUEST_ID + " = " + id, null);
         } catch ( Exception e ) {
           Log.e(TAG, "Updating retry count failed", e);
         }
    }

    // Delete lost request
    public void deleteLostRequest(final int id
                                 ) throws Exception {
          myDataBase.delete(DatabaseField.TABLE_LOSTREQUESTS, DatabaseField.LOSTREQUEST_ID + " = " + id, null);
    }

    public int checkIfNumIsBlackOrWhite(String number) {
        Log.d(TAG, "Number checked in db: " + number);

        if (checkIfInBlackList(number) != Constants.NUM_NOT_PRESENT) {
            Log.d(TAG, "Blocked number");
            return Constants.NUM_BLACKLISTED;
        }
        if (checkIfInWhiteList(number) != Constants.NUM_NOT_PRESENT) {
            Log.d(TAG, "Allowed number");
            return Constants.NUM_WHITELISTED;
        }
        Log.d(TAG, "New number");
        return Constants.NUM_NOT_PRESENT;


    }

    private int checkIfInWhiteList(String number) {
        Cursor cursor = null;
        String[] dbCols;
        String[] selectArgs = new String[]{number,number};
        String where;
        String table;

        try {
            SetupDbPtr();

            dbCols = new String[]{DatabaseField.WHITELIST_ID};
            where = DatabaseField.WHITELIST_FULL_NUMBER + "= ? or " + DatabaseField.WHITELIST_PHONE + "= ?";
            table = DatabaseField.TABLE_WHITELIST;
            cursor = myDataBase.query(table, dbCols, where, selectArgs, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                return Constants.NUM_WHITELISTED;
            }


        } catch (Exception e) {
            Log.d(TAG, e.getMessage() + "  in checkIfInBlackList");

        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return Constants.NUM_NOT_PRESENT;
    }

    private void SetupDbPtr() {
        if (myDataBase == null || !myDataBase.isOpen()) {
            myDataBase = openDataBase();
        }
    }

    private int checkIfInBlackList(final String number) {
        Cursor cursor = null;

        final String[] selectArgs = new String[]{number,number};
        Log.d(TAG, "Blacklist check: " + number);

        try {
            SetupDbPtr();

            String[] dbCols = new String[]{DatabaseField.BLACKLIST_ID};
            String where = DatabaseField.BLACKLIST_FULL_NUMBER + "= ? or " + DatabaseField.BLACKLIST_PHONE + "= ?";
            String table = DatabaseField.TABLE_BLACKLIST;
            cursor = myDataBase.query(table, dbCols, where, selectArgs, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                return Constants.NUM_BLACKLISTED;
            }
        } catch (Exception e) {
            Log.d(TAG, "checkIfInBlackList", e);
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
        return Constants.NUM_NOT_PRESENT;
    }

    public long getWhitelistSize() {
        final String sql = "SELECT COUNT(*) FROM " + DatabaseField.TABLE_WHITELIST;
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

    // Both local and remote numbers.
    public long getBlacklistSize() {
        final String sql = "SELECT COUNT(*) FROM " + DatabaseField.TABLE_BLACKLIST;
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

    public long getLocalBlacklistSize() {
        final String sql = "SELECT COUNT(*) FROM " + DatabaseField.TABLE_BLACKLIST + " WHERE " + DatabaseField.BLACKLIST_IS_LOCAL + "=1";
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

    public long getLocalWhitelistSize() {
        final String sql = "SELECT COUNT(*) FROM " + DatabaseField.TABLE_WHITELIST + " WHERE " + DatabaseField.WHITELIST_IS_LOCAL + "=1";
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long count = statement.simpleQueryForLong();
        return count;
    }

    /**
     * get all black-list numbers
     */
    public Cursor getAllBlackListedNumber() {
        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_BLACKLIST, new String[]{"*"}, null, null, null, null, DatabaseField.BLACKLIST_ID + " DESC");
        } catch (Exception e) {
            Log.e(TAG, "getAllBlackListedNumber", e);
        }
        return mCursor;
    }

    /**
     * get all lost requests
     */
    public Cursor getAllLostRequests() {
        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_LOSTREQUESTS, new String[]{"*"}, DatabaseField.LOSTREQUEST_RETRYCOUNT + " <= " + DatabaseField.LOSTREQUEST_RETRYMAX, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAllBlackListedNumber", e);
        }
        return mCursor;
    }

    public Cursor getBlackListedNumberById(int id) {
        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_BLACKLIST, new String[]{"*"}, DatabaseField.BLACKLIST_ID + " = " + id, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getWhiteListedNumberById", e);
        }
        return mCursor;
    }

    public Cursor getBlackListedNumber(String phoneNumber) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_BLACKLIST, new String[]{"*"}, DatabaseField.BLACKLIST_FULL_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getBlackListedNumber", e);
        }
        return mCursor;
    }

    public Cursor getWhiteListedNumberById(int id) {
        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_WHITELIST, new String[]{"*"}, DatabaseField.WHITELIST_ID + " = " + id, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getWhiteListedNumberById", e);
        }
        return mCursor;
    }

    public Cursor getWhiteListedNumber(String phoneNumber) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_WHITELIST, new String[]{"*"}, DatabaseField.BLACKLIST_FULL_NUMBER + " = " + DatabaseUtils.sqlEscapeString(phoneNumber), null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getWhiteListedNumber", e);
        }
        return mCursor;

    }

    public Cursor getAllWhiteListedNumber() {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_WHITELIST, new String[]{"*"}, null, null, null, null, DatabaseField.WHITELIST_ID + " DESC");
        } catch (Exception e) {
            Log.e(TAG, "getAllWhiteListedNumber", e);
        }
        return mCursor;

    }

    public Cursor getAllCountry() {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_COUNTRY, new String[]{"*"}, null, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAllCountry", e);
        }
        return mCursor;
    }

    public Cursor getCountry(String country) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_COUNTRY, new String[]{"*"}, DatabaseField.COUNTRY_NAME + " = " + DatabaseUtils.sqlEscapeString(country), null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getCountry", e);
        }
        return mCursor;

    }

    public int getCountryPosition(String countryCode) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_COUNTRY, new String[]{"*"}, DatabaseField.CALLING_CODE+ " = " + DatabaseUtils.sqlEscapeString(countryCode), null, null, null, null);
            if (mCursor != null && mCursor.getCount() > 0) {
                mCursor.moveToFirst();
                return mCursor.getInt(mCursor.getColumnIndex(DatabaseField.COUNTRY_ID));
            }
        } catch (Exception e) {
            Log.e(TAG, "getCountryPosition", e);
        }
        return 0;
    }

    public Cursor getSelectedCountries(int isSelected) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_COUNTRY, new String[]{"*"}, DatabaseField.COUNTRY_IS_SELECTED + " = " + isSelected, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getCountry", e);
        }
        return mCursor;

    }

    public Cursor getAllAreaOfCountry(String countryCode, String countryName) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_AREA, new String[]{"*"}, DatabaseField.AREA_COUNTRY_CODE + " = " + DatabaseUtils.sqlEscapeString(countryCode) + " AND " + DatabaseField.AREA_COUNTRY_NAME + " = " + DatabaseUtils.sqlEscapeString(countryName), null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAllAreaOfCountry", e);
        }
        return mCursor;

    }

    public Cursor getAreaOfCountry(String countryCode, String stateCode) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_AREA, new String[]{"*"}, DatabaseField.AREA_COUNTRY_CODE + " = " + DatabaseUtils.sqlEscapeString(countryCode) + " AND " + DatabaseField.AREA_CODE + " = " + DatabaseUtils.sqlEscapeString(stateCode), null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAreaOfCountry", e);
        }
        return mCursor;

    }

    /*
    public Cursor getAllAreaOfCountry(int isSelected) {

        Cursor mCursor = null;
        try {
            mCursor = myDataBase.query(DatabaseField.TABLE_AREA, new String[]{"*"}, DatabaseField.AREA_IS_SELECTED + " = " + isSelected, null, null, null, null);
        } catch (Exception e) {
            Log.e(TAG, "getAllAreaOfCountry", e);
        }
        return mCursor;

    }
    */


    /**
     * delete data from table
     */

    public void deleteBlackListNumbers() {

        try {
            myDataBase.delete(DatabaseField.TABLE_BLACKLIST, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteBlackListNumbers", e);
        }

        tableUpdated(URI_TABLE_BLACKLIST);
    }

    public void deleteWhiteListNumbers() {

        try {
            myDataBase.delete(DatabaseField.TABLE_WHITELIST, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteWhiteListNumbers", e);
        }

        tableUpdated(URI_TABLE_WHITELIST);
    }

    public void deleteCountry() {

        try {
            myDataBase.delete(DatabaseField.TABLE_COUNTRY, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteCountry", e);
        }
    }

    public void deleteArea() {

        try {
            myDataBase.delete(DatabaseField.TABLE_AREA, null, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteArea", e);
        }
    }

    /*
    public void deleteSelectedBlacklistNumber(String number) {

        try {
            myDataBase.delete(DatabaseField.TABLE_BLACKLIST, DatabaseField.BLACKLIST_PHONE + " = " + DatabaseUtils.sqlEscapeString(number), null);
        } catch (Exception e) {
            Log.e(TAG, "deleteSelectedBlacklistNumber", e);
        }
    }
    */

    public void deleteWhitelistNumber(int id) {

        try {
            myDataBase.delete(DatabaseField.TABLE_WHITELIST, DatabaseField.WHITELIST_ID + " = " + id, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteWhitelistNumber", e);
        }

        tableUpdated(URI_TABLE_WHITELIST);
    }

    public void deleteBlacklistNumber(int id) {

        try {
            myDataBase.delete(DatabaseField.TABLE_BLACKLIST, DatabaseField.BLACKLIST_ID + " = " + id, null);
        } catch (Exception e) {
            Log.e(TAG, "deleteBlacklistNumber", e);
        }

        tableUpdated(URI_TABLE_BLACKLIST);
    }

    /*
    public void deleteSelectedWhitelistNumber(String number) {

        try {
            myDataBase.delete(DatabaseField.TABLE_WHITELIST, DatabaseField.WHITELIST_PHONE + " = " + DatabaseUtils.sqlEscapeString(number), null);
        } catch (Exception e) {
            Log.e(TAG, "deleteSelectedWhitelistNumber", e);
        }
    }
    */

    // Move a blacklisted number to whitelist.
    // Use only an ID for the number of the blacklist.
    public void moveFromBlackListToWhiteList(final int blackListId) {
        Log.d(TAG, "Moving number with ID=" + blackListId + " from BL to WL.");

        boolean failed = false;
        SetupDbPtr();
        myDataBase.beginTransaction();

        Cursor cursor = null;
        try {
            final String[] selectArgs = new String[]{Integer.toString(blackListId)};
            final String[] dbCols = new String[]{"*"};
            final String where = DatabaseField.BLACKLIST_ID + "= ?";
            final String table = DatabaseField.TABLE_BLACKLIST;

            cursor = myDataBase.query(table, dbCols, where, selectArgs, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                failed = true;
                Log.e(TAG, "moveFromBlackListToWhiteList" + ", invalid ID " + blackListId);
            }
        } catch (Exception e) {
            Log.d(TAG, "checkIfInBlackList", e);
            cursor = null;
        }

        if ((cursor != null) && (!failed)) {
            try {
                cursor.moveToFirst();
                final String destTable = DatabaseField.TABLE_WHITELIST;
                ContentValues newNumValue = new ContentValues();
                newNumValue.put(DatabaseField.WHITELIST_FULL_NUMBER,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_FULL_NUMBER)));
                newNumValue.put(DatabaseField.WHITELIST_PHONE,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_PHONE)));
                newNumValue.put(DatabaseField.WHITELIST_CALLER,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_CALLER)));
                newNumValue.put(DatabaseField.WHITELIST_COUNTRY_CODE,
                        cursor.getInt(cursor.getColumnIndex(DatabaseField.BLACKLIST_COUNTRY_CODE)));
                newNumValue.put(DatabaseField.WHITELIST_COUNTRY,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_COUNTRY)));
                newNumValue.put(DatabaseField.WHITELIST_IS_LOCAL,
                        cursor.getInt(cursor.getColumnIndex(DatabaseField.BLACKLIST_IS_LOCAL)));
                newNumValue.put(DatabaseField.WHITELIST_NOTE,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_NOTE)));
                myDataBase.insert(destTable, null, newNumValue);

                // Delete old entry.
                myDataBase.delete(DatabaseField.TABLE_BLACKLIST, DatabaseField.BLACKLIST_ID + " = " + Integer.toString(blackListId), null);
            }
            catch (Exception e) {
                failed = true;
                Log.e(TAG, "moveFromBlackListToWhiteList, unable to insert or delete", e);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        if (!failed) {
            myDataBase.setTransactionSuccessful();
        }
        myDataBase.endTransaction();

        tableUpdated(URI_TABLE_BLACKLIST);
        tableUpdated(URI_TABLE_WHITELIST);
    }

    // Move a whitelisted number to blacklist.
    // Use only an ID for the number of the whitelist.
    public void moveFromWhiteListToBlackList(int whiteListId) {
        Log.d(TAG, "Moving number with ID=" + whiteListId + " from WL to BL.");

        boolean failed = false;
        SetupDbPtr();
        myDataBase.beginTransaction();

        Cursor cursor = null;
        try {
            final String[] selectArgs = new String[]{Integer.toString(whiteListId)};
            final String[] dbCols = new String[]{"*"};
            final String where = DatabaseField.WHITELIST_ID + "= ?";
            final String table = DatabaseField.TABLE_WHITELIST;

            cursor = myDataBase.query(table, dbCols, where, selectArgs, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) {
                failed = true;
                Log.e(TAG, "moveFromWhiteListToBlackList" + ", invalid ID " + whiteListId);
            }
        } catch (Exception e) {
            Log.d(TAG, "checkIfInBlackList", e);
            cursor = null;
        }

        if ((cursor != null) && (!failed)) {
            try {
                cursor.moveToFirst();
                final String destTable = DatabaseField.TABLE_BLACKLIST;
                ContentValues newNumValue = new ContentValues();
                newNumValue.put(DatabaseField.BLACKLIST_FULL_NUMBER,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_FULL_NUMBER)));
                newNumValue.put(DatabaseField.BLACKLIST_PHONE,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_PHONE)));
                newNumValue.put(DatabaseField.BLACKLIST_CALLER,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_CALLER)));
                newNumValue.put(DatabaseField.BLACKLIST_COUNTRY_CODE,
                        cursor.getInt(cursor.getColumnIndex(DatabaseField.WHITELIST_COUNTRY_CODE)));
                newNumValue.put(DatabaseField.BLACKLIST_COUNTRY,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_COUNTRY)));
                newNumValue.put(DatabaseField.BLACKLIST_IS_LOCAL,
                        cursor.getInt(cursor.getColumnIndex(DatabaseField.WHITELIST_IS_LOCAL)));
                newNumValue.put(DatabaseField.BLACKLIST_NOTE,
                        cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_NOTE)));
                myDataBase.insert(destTable, null, newNumValue);

                // Delete old entry.
                myDataBase.delete(DatabaseField.TABLE_WHITELIST, DatabaseField.WHITELIST_ID + " = " + Integer.toString(whiteListId), null);
            }
            catch (Exception e) {
                failed = true;
                Log.e(TAG, "moveFromWhiteListToBlackList, unable to insert or delete", e);
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        if (!failed) {
            myDataBase.setTransactionSuccessful();
        }
        myDataBase.endTransaction();

        tableUpdated(URI_TABLE_WHITELIST);
        tableUpdated(URI_TABLE_BLACKLIST);
    }

    // 45 (DK) -> Denmark
    public String getCountryName(final String prefix) {

        Cursor cursor = null;
        try {
            cursor = myDataBase.query(DatabaseField.TABLE_COUNTRY, new String[]{"*"}, DatabaseField.CALLING_CODE + " = " + DatabaseUtils.sqlEscapeString(prefix), null, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                return cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME));
            }
        } catch (Exception e) {
            Log.e(TAG, "getCountryName", e);
        }
        return "";
    }

    // Full number in the format saved in DB.
    public static String generateFullNumberInDbFormat(final String countryCode, final String phone) {
        final StringBuffer sb = new StringBuffer();
        return sb.append("+").
                append(countryCode).
                append(phone).
                toString();
    }

    // Full number in the format used by the web service.
    public static String generateFullNumberInServerFormat(final String countryCode, final String phone) {
        final StringBuffer sb = new StringBuffer();
        return sb.append("+").
                append(countryCode).
                append("-").
                append(phone).
                toString();
    }

    // Update the entry described by dbId.
    public void updateBlackListEntry(int dbId,
                                     int countryCode,
                                     String countryNameToUpdate,
                                     String numberToUpdate, // without prefix
                                     String nameToUpdate,
                                     String noteToUpdate) {

        final ContentValues cv = new ContentValues();
        cv.put(DatabaseField.BLACKLIST_PHONE, numberToUpdate);
        cv.put(DatabaseField.BLACKLIST_CALLER, nameToUpdate);
        cv.put(DatabaseField.BLACKLIST_NOTE , noteToUpdate);

        cv.put(DatabaseField.BLACKLIST_COUNTRY_CODE, countryCode);
        cv.put(DatabaseField.BLACKLIST_COUNTRY, countryNameToUpdate);

        // Generate the full number.
        final String fullNumberToUpdate = generateFullNumberInDbFormat(Integer.toString(countryCode), numberToUpdate);
        cv.put(DatabaseField.BLACKLIST_FULL_NUMBER, fullNumberToUpdate);

        // No matter what this entry is now local moveBackDays it was updated.
        cv.put(DatabaseField.BLACKLIST_IS_LOCAL, 1);

        // Log.d(TAG, "Updating BL entry: " + cv.toString());

        myDataBase.update(DatabaseField.TABLE_BLACKLIST, cv, DatabaseField.BLACKLIST_ID + "=" + dbId, null);

        tableUpdated(URI_TABLE_BLACKLIST);
    }

    // Update the entry described by dbId.
    public void updateWhiteListEntry(int dbId,
                                     int countryCode,
                                     String countryNameToUpdate,
                                     String numberToUpdate, // without prefix
                                     String nameToUpdate,
                                     String noteToUpdate) {

        final ContentValues cv = new ContentValues();
        cv.put(DatabaseField.WHITELIST_PHONE, numberToUpdate);
        cv.put(DatabaseField.WHITELIST_CALLER, nameToUpdate);
        cv.put(DatabaseField.WHITELIST_NOTE , noteToUpdate);

        cv.put(DatabaseField.WHITELIST_COUNTRY_CODE, countryCode);
        cv.put(DatabaseField.WHITELIST_COUNTRY, countryNameToUpdate);

        // Generate the full number.
        final String fullNumberToUpdate = generateFullNumberInDbFormat(Integer.toString(countryCode), numberToUpdate);
        cv.put(DatabaseField.WHITELIST_FULL_NUMBER, fullNumberToUpdate);

        // No matter what this entry is now local moveBackDays it was updated.
        cv.put(DatabaseField.WHITELIST_IS_LOCAL, 1);

        // Log.d(TAG, "Updating WL entry: " + cv.toString());

        myDataBase.update(DatabaseField.TABLE_WHITELIST, cv, DatabaseField.WHITELIST_ID + "=" + dbId, null);

        tableUpdated(URI_TABLE_WHITELIST);
    }

    private boolean gotCounterForDay(final int type, final LogDate day) {

        final StringBuffer sb = new StringBuffer();

        sb.append("SELECT COUNT(*) FROM ").append(DatabaseField.TABLE_CALLSTATS).append(" WHERE ").
                append(DatabaseField.CALLSTATE_TYPE).append("=").append(type).append(" AND ").
                append(DatabaseField.CALLSTATE_YEAR).append("=").append(day.year()).append(" AND ").
                append(DatabaseField.CALLSTATE_MONTH).append("=").append(day.month()).append(" AND ").
                append(DatabaseField.CALLSTATE_DAY).append("=").append(day.day());

        final String sql = sb.toString();
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long value = statement.simpleQueryForLong();

        if (value > 0) {
            return true;
        }
        return false;
    }

    public void updateCallCounter(final int type) {
        final Calendar cal = Calendar.getInstance();

        LogDate d = LogDate.getCurrent();

        final StringBuffer sb = new StringBuffer();

        if (gotCounterForDay(type, d)) {
                sb.append("UPDATE ").append(DatabaseField.TABLE_CALLSTATS).
                    append(" SET ").
                    append(DatabaseField.CALLSTATE_VALUE).
                    append("=").
                    append(DatabaseField.CALLSTATE_VALUE).append(" + 1").
                    append(" WHERE ").
                    append(DatabaseField.CALLSTATE_TYPE).append("=").append(type).
                    append(" AND year = ").append(d.year()).
                    append(" AND month = ").append(d.month()).
                    append(" AND day = ").append(d.day());

                final String query = sb.toString();
                myDataBase.execSQL(query);
        }
        else {
            ContentValues values = new ContentValues();
            values.put(DatabaseField.CALLSTATE_TYPE, type);
            values.put(DatabaseField.CALLSTATE_VALUE, 1);
            values.put(DatabaseField.CALLSTATE_YEAR, d.year());
            values.put(DatabaseField.CALLSTATE_MONTH, d.month());
            values.put(DatabaseField.CALLSTATE_DAY, d.day());

            final long now = System.currentTimeMillis() / 1000L;
            values.put(DatabaseField.CALLSTATE_CREATED, now);

            myDataBase.insert(DatabaseField.TABLE_CALLSTATS, null, values);
        }

        tableUpdated(URI_TABLE_CALLSTATS);
    }

    private long getCounterForDay(final int type, final LogDate day) {
        if (!gotCounterForDay(type, day)) {
            // Log.d(TAG, "getCounterForDay, no counter " + type + ", " + year + ", " + month + ", " + day);
            return 0;
        }

        final StringBuffer sb = new StringBuffer();
        sb.append("SELECT value FROM ").append(DatabaseField.TABLE_CALLSTATS).append(" WHERE ").
                append(DatabaseField.CALLSTATE_TYPE).append("=").append(type).append(" AND ").
                append(DatabaseField.CALLSTATE_YEAR).append("=").append(day.year()).append(" AND ").
                append(DatabaseField.CALLSTATE_MONTH).append("=").append(day.month()).append(" AND ").
                append(DatabaseField.CALLSTATE_DAY).append("=").append(day.day());

        final String sql = sb.toString();
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        long value = statement.simpleQueryForLong();

        return value;
    }

    // Get call counters for last numberOfDays days.
    public long getRoughCallCounter(final int type, final int numberOfDays) {

        // Get a date numberOfDays ago.
        long sum = 0;
        for (int counter = 0; counter < numberOfDays; counter++) {

            final LogDate day = LogDate.moveBackDays(counter);
            long counterForDay = getCounterForDay(type, day);
            Log.d(TAG, "date " + day.year() + "-" + day.month() + "-" + day.day() + ":" + counterForDay);
            sum += counterForDay;
        }

        Log.d(TAG, "Counter " + type + ", value = " + sum);
        return sum;
    }

    public void updateAddCallInfo(final int type, final String phone) {

        ContentValues values = new ContentValues();

        // Timestamp in seconds.
        final long timeStamp = System.currentTimeMillis() / 1000L;

        values.put(DatabaseField.CALLDETAIL_NUMBER, phone);
        values.put(DatabaseField.CALLDETAIL_DATE, timeStamp);
        values.put(DatabaseField.CALLDETAIL_TYPE, type);

        myDataBase.insert(DatabaseField.TABLE_CALLDETAIL, null, values);

        // Log.d(TAG, "Inserted " + values.toString() + " into table " + DatabaseField.TABLE_CALLDETAIL);

        tableUpdated(URI_TABLE_CALLDETAIL);
    }

    // Notify whomever listens for changes to the tables.
    private void tableUpdated(final Uri u) {
        mContext.getContentResolver().notifyChange(u, null);
        // Log.d(TAG, "Notified: " + u);
    }

    // Get call counter for the specified type.
    // sinceSecondsAgo is used to find the number of blocked calls.
    public long getDetailedCallCounter(final int type, long sinceSecondsAgo) {
        // Timestamp in seconds.
        final long now = System.currentTimeMillis() / 1000L;
        final long startTimeStamp = now - sinceSecondsAgo;

        Log.d(TAG, "Getting calls totals from " + startTimeStamp + " to " + now);

        final StringBuffer sb = new StringBuffer();

        sb.append("SELECT COUNT(*) FROM ").append(DatabaseField.TABLE_CALLDETAIL).append(" WHERE ").
                append(DatabaseField.CALLDETAIL_TYPE).append("=").append(type).append(" AND ").
                append(DatabaseField.CALLDETAIL_DATE).append(">=").append(startTimeStamp);

        final String sql = sb.toString();
        final SQLiteStatement statement = myDataBase.compileStatement(sql);
        final long value = statement.simpleQueryForLong();

        Log.d(TAG, "Calls since " + startTimeStamp + ": " + value);

        return value;
    }

    public Cursor getCalls(final int type, final long fromTimestamp, final long toTimestamp) {

        Log.d(TAG, "Getting calls from " + fromTimestamp + " to " + toString() + " with ID=" + type);

        final String[] dbCols = new String[]{DatabaseField.CALLDETAIL_ID, DatabaseField.CALLDETAIL_NUMBER, DatabaseField.CALLDETAIL_DATE};

        final StringBuffer sb = new StringBuffer();
        sb.append(DatabaseField.CALLDETAIL_TYPE)
                .append("=? AND ")
                .append(DatabaseField.CALLDETAIL_DATE)
                .append(">=? AND ")
                .append(DatabaseField.CALLDETAIL_DATE)
                .append("<=?");

        final String where = sb.toString(); //DatabaseField.CALLDETAIL_TYPE + "= ?" ;
        final String table = DatabaseField.TABLE_CALLDETAIL;
        final String[] selectArgs = new String[]{
                    Integer.toString(type),
                    Long.toString(fromTimestamp),
                    Long.toString(toTimestamp)
        };

        final String orderBy = DatabaseField.CALLDETAIL_DATE + " DESC";

        Log.d(TAG, "where = " + where);
        Log.d(TAG, "selectArgs = " + Arrays.toString(selectArgs));

        Cursor cursor = null;
        try {
            cursor = myDataBase.query(table, dbCols, where, selectArgs, null, null, orderBy);
        } catch (Exception e) {
            Log.e(TAG, "getCalls", e);
        }
        return cursor;
    }

    public void cleanUpDetailedCallLog(final long since) {
        Log.d(TAG, "Cleaning up call details older than stamp: " + since);

        // Delete any entries older than one month to save space.
        final String whereClause = DatabaseField.CALLDETAIL_DATE + " <= ?";
        final String[] whereArgs = {Long.toString(since)};

        int affected = myDataBase.delete(DatabaseField.TABLE_CALLDETAIL, whereClause, whereArgs);

        Log.d(TAG, "cleanUpDetailedCallLog, deleted " + affected + " rows");
    }

    public void cleanUpCallStats(final long since) {
        Log.d(TAG, "Cleaning up statistics older than stamp: " + since);

        // Delete any entries older than one month to save space.
        final String whereClause = DatabaseField.CALLSTATE_CREATED + " <= ?";
        final String[] whereArgs = {Long.toString(since)};

        int affected = myDataBase.delete(DatabaseField.TABLE_CALLSTATS, whereClause, whereArgs);

        Log.d(TAG, "cleanUpDetailedCallLog, deleted " + affected + " rows");

    }
}
