package com.reverdapp.database;

public final class DatabaseField {

    //Table name
    public static String TABLE_BLACKLIST = "BlackList";
    public static String TABLE_WHITELIST = "WhiteList";
    public static String TABLE_COUNTRY = "Country";
    public static String TABLE_AREA = "Area";
    public static String TABLE_CALLSTATS = "CallStats";
    public static String TABLE_CALLDETAIL = "CallDetail";
    public static String TABLE_LOSTREQUESTS = "LostRequests";

    //column name
    public static String BLACKLIST_CALLER = "caller";
    public static String BLACKLIST_PHONE = "phone";
    public static String BLACKLIST_COUNTRY_CODE = "country_code";
    public static String BLACKLIST_COUNTRY = "country";
    public static String BLACKLIST_FULL_NUMBER = "full_number";
    public static String BLACKLIST_ID = "_id";
    public static String BLACKLIST_IS_LOCAL = "is_local";
    public static String BLACKLIST_NOTE = "note";
    
    public static String WHITELIST_CALLER = "caller";
    public static String WHITELIST_PHONE = "phone";
    public static String WHITELIST_COUNTRY_CODE = "country_code";
    public static String WHITELIST_COUNTRY = "country";
    public static String WHITELIST_FULL_NUMBER = "full_number";
    public static String WHITELIST_ID = "_id";
    public static String WHITELIST_IS_LOCAL = "is_local";
    public static String WHITELIST_NOTE = "note";
    
    public static String COUNTRY_CODE = "country_code";
    public static String CALLING_CODE = "calling_code";
    public static String COUNTRY_NAME = "country_name";
    public static String COUNTRY_IS_SELECTED = "is_selected";
    public static String COUNTRY_ID = "_id";
    
    public static String AREA_COUNTRY_CODE = "country_code";
    public static String AREA_COUNTRY_NAME = "country_name";
    public static String AREA_CODE = "area_code";
    public static String AREA_NAME = "area_name";
    public static String AREA_IS_SELECTED = "is_selected";

    public static String CALLSTATE_TYPE = "type";
    public static String CALLSTATE_VALUE = "value";
    public static String CALLSTATE_YEAR = "year";
    public static String CALLSTATE_MONTH = "month";
    public static String CALLSTATE_DAY = "day";
    public static String CALLSTATE_CREATED= "created"; // date

    public static String CALLDETAIL_ID= "_id";
    public static String CALLDETAIL_NUMBER= "number";
    public static String CALLDETAIL_DATE= "date";
    public static String CALLDETAIL_TYPE= "type";

    public static String LOSTREQUEST_ID= "_id";
    public static String LOSTREQUEST_URL= "url";
    public static String LOSTREQUEST_POSTDATA= "post_data";
    public static String LOSTREQUEST_RETRYCOUNT= "retry_count";
    public static int LOSTREQUEST_RETRYMAX= 5;

}
