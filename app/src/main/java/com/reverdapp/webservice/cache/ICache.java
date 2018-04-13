package com.reverdapp.webservice.cache;

import android.os.Bundle;

/**
 * Created by wojci on 8/16/15.
 */
public interface ICache {
    void add(String key, Bundle value);
    Bundle get(String key);
    boolean contains(String key);
}
