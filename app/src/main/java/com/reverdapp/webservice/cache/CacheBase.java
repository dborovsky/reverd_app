package com.reverdapp.webservice.cache;

import android.os.Bundle;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wojci on 8/16/15.
 */
public class CacheBase implements ICache {

    private static final String TAG = LogConfig.genLogTag("CacheBase");

    private long mMaxAge;
    private Map<String, Bundle> mMap;
    private Map<String, Long> mAge;

    public static <String, Bundle> Map<String, Bundle> createLRUMap(final int maxEntries) {
        return new LinkedHashMap<String, Bundle>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Bundle> eldest) {
                return size() > maxEntries;
            }
        };
    }

    public static <String, Bundle> Map<String, Long> createLRUMap2(final int maxEntries) {
        return new LinkedHashMap<String, Long>(maxEntries*10/7, 0.7f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Long> eldest) {
                return size() > maxEntries;
            }
        };
    }
    // maxAge in milliseconds.
    public CacheBase(int max, int maxAge)
    {
        mMap = CacheBase.createLRUMap(max);
        mAge = CacheBase.createLRUMap2(max);

        mMaxAge = maxAge;

        Log.d(TAG, "Cache created.");
    }

    @Override
    public void add(String key, Bundle value) {

        final long now = System.currentTimeMillis();

        mAge.put(key, now);
        mMap.put(key, value);

        Log.d(TAG, "add: " + key + " => " + value.toString());
    }

    @Override
    public Bundle get(String key) {
        return mMap.get(key);
    }

    @Override
    public boolean contains(final String key) {
        if (!mAge.containsKey(key))
        {
            return false;
        }

        // Contains the value we are looking for.
        final long now = System.currentTimeMillis();
        long ts = mAge.get(key);
        long age = now-ts;
        if (age > mMaxAge)
        {
            // Too old.
            return false;
        }

        Log.d(TAG, "contains: " + key + ", left = " + (mMaxAge-age));

        return true;
    }
}
