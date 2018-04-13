package com.reverdapp.stats;

import java.util.Calendar;

/**
 * Created by wojci on 8/13/15.
 */
public class LogDate {
    private int mYear;
    private int mMonth;
    private int mDay;

    public LogDate(int y, int m, int d) {
        mYear = y;
        mMonth = m;
        mDay = d;
    }

    public int year() {
        return mYear;
    }

    public int month() {
        return mMonth;
    }

    public int day() {
        return mDay;
    }

    public static LogDate moveBackDays(final int days) {
        final Calendar cal = Calendar.getInstance();

        cal.add(Calendar.DATE, -days);

        int y = cal.get(Calendar.YEAR);
        int m = (cal.get(Calendar.MONTH) + 1); // Month starts at 0.
        int d = cal.get(Calendar.DAY_OF_MONTH);

        LogDate day = new LogDate(y,m,d);

        return day;
    }

    public static LogDate getCurrent() {

        final Calendar cal = Calendar.getInstance();

        int y = cal.get(Calendar.YEAR);
        int m = (cal.get(Calendar.MONTH) + 1); // Month starts at 0.
        int d = cal.get(Calendar.DAY_OF_MONTH);

        LogDate day = new LogDate(y,m,d);

        return day;
    }
}
