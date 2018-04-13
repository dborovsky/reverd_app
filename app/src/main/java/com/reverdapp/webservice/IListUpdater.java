package com.reverdapp.webservice;

import android.content.Context;

import org.json.JSONObject;

/**
 * Created by wojci on 8/26/15.
 */
public interface IListUpdater {
    static final String BL_PHONES = "bl_phones";
    static final String USER_BL_PHONES = "user_bl_phones";
    static final String USER_WL_PHONES = "user_wl_phones";

    // Called before getting any results.
    abstract void onInit();

    // Called when the WS call succeeded.
    abstract void onSuccess();

    // Called for each result received.
    abstract void onResult(final String name, final JSONObject result);

    // Called when the WS call finished with an error.
    abstract void onError();

    // Called to do any cleanup after the WS call.
    abstract void onFinish(final boolean error);
}
