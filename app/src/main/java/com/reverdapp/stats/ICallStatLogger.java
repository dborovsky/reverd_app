package com.reverdapp.stats;

/**
 * Created by wojci on 8/12/15.
 */
public interface ICallStatLogger {

    void onReceivedCall(final int type, final String phone);
}
