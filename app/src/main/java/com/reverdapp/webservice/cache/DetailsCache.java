package com.reverdapp.webservice.cache;

import android.os.Bundle;

/**
 * Created by wojci on 8/16/15.
 */
public class DetailsCache extends CacheBase {
    public static final String ID_NAME = "ID_NAME";
    public static final String ID_NOTE = "ID_NOTE";

    static final int MAXITEMS = 100;
    static final int MAXAGE = 5*60*1000;

    public DetailsCache() {
        super(MAXITEMS, MAXAGE);
    }
}
