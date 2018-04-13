package com.reverdapp.utils;

import com.reverdapp.fragment.loader.AbstractTaskLoader;

/**
 * Created by wojci on 4/14/15.
 */
public class LogConfig {
    public static String genClassLogTag(Class<?> type) {
        return Constants.PHONE_LOG + "." + type.getSimpleName().toString();
    }

    public static String genLogTag(final String localTag)
    {
        return Constants.PHONE_LOG + "." + localTag;
    }
}
