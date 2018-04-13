package com.reverdapp.model;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.utils.Iso2Phone;

// List model used for showing white/black lists.
public class NumberListModel implements Parcelable {

    protected boolean mSelected;
    protected String mCaller;
    protected String mPhoneNumber;
    protected int mCountryCode;
    protected String mCountry;
    protected int mIsLocal = 0;
    protected String mNote;
    protected int mId;

    public static final int INVALID_ID = -1;
    //public static final int LOCAL_ENTRY = 1;

    public NumberListModel(final Parcel in){
        mSelected = in.readByte() != 0;
        mCaller = in.readString();
        mPhoneNumber = in.readString();
        mCountryCode = in.readInt();
        mCountry = in.readString();
        mIsLocal = in.readInt();
        mNote = in.readString();
        mId = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (mSelected ? 1 : 0));
        dest.writeString(mCaller);
        dest.writeString(mPhoneNumber);
        dest.writeInt(mCountryCode);
        dest.writeString(mCountry);
        dest.writeInt(mIsLocal);
        dest.writeString(mNote);
        dest.writeInt(mId);
    }

    public NumberListModel (final int id) {
        mId = id;
        mSelected = false;
    }

    private NumberListModel () {
        mId = INVALID_ID;
        mSelected = false;
    }

    public boolean isSelected() {
        return mSelected;
    }

    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    public String getCaller() {
        return mCaller;
    }

    public void setCaller(String caller) {
        mCaller = caller;
    }

    public String getPhoneNumber() {
        return mPhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        mPhoneNumber = phoneNumber;
    }

    public int getCountryCode() {
        return mCountryCode;
    }

    public void setCountryCode(int countryCode) {
        mCountryCode = countryCode;
    }

    public String getCountry() {
        return mCountry;
    }

    public void setCountry(String country) {
        mCountry = country;
    }

    public int isLocal() {
        return mIsLocal;
    }

    public boolean canBeEdited() {
        return mIsLocal == Database.LOCAL_ENTRY;
    }

    public void setIsLocal(final int isLocal) {
        mIsLocal = isLocal;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        mNote = note;
    }

    public int getId() {
        return mId;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public NumberListModel createFromParcel(Parcel in) {
            return new NumberListModel(in);
        }

        public NumberListModel[] newArray(int size) {
            return new NumberListModel[size];
        }
    };

    public static NumberListModel createWhiteListModel(final Cursor cursor)
    {
        NumberListModel m = new NumberListModel(cursor.getInt(cursor.getColumnIndex(DatabaseField.WHITELIST_ID)));
        m.setCaller(cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_CALLER)));
        m.setPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_PHONE)));
        m.setCountryCode(cursor.getInt(cursor.getColumnIndex(DatabaseField.WHITELIST_COUNTRY_CODE)));
        m.setCountry(cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_COUNTRY)));
        m.setIsLocal(cursor.getInt(cursor.getColumnIndex(DatabaseField.WHITELIST_IS_LOCAL)));
        m.setNote(cursor.getString(cursor.getColumnIndex(DatabaseField.WHITELIST_NOTE)));
        return m;
    }

    public static NumberListModel createBlackListModel(final Cursor cursor)
    {
        NumberListModel m = new NumberListModel(cursor.getInt(cursor.getColumnIndex(DatabaseField.BLACKLIST_ID)));
        m.setCaller(cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_CALLER)));
        m.setPhoneNumber(cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_PHONE)));
        m.setCountryCode(cursor.getInt(cursor.getColumnIndex(DatabaseField.BLACKLIST_COUNTRY_CODE)));
        m.setCountry(cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_COUNTRY)));
        m.setIsLocal(cursor.getInt(cursor.getColumnIndex(DatabaseField.BLACKLIST_IS_LOCAL)));
        m.setNote(cursor.getString(cursor.getColumnIndex(DatabaseField.BLACKLIST_NOTE)));
        return m;
    }

    // Format contained number as "+countrycode-number"
    public String getFullNumber() {
        return Iso2Phone.formatFullPhonenumber(getCountryCode(), getPhoneNumber());
    }
}
