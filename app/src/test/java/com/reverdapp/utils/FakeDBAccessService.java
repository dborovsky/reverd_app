package com.reverdapp.utils;

import android.content.Intent;

import com.reverdapp.Service.DBAccessService;

/**
 * Created by wojci on 5/30/15.
 */
public class FakeDBAccessService extends DBAccessService {
    @Override
    public void onStart(Intent intent, int startId) {
        onHandleIntent(intent);
        stopSelf(startId);
    }
}
