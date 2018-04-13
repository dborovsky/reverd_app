package com.reverdapp.fragment;

import android.app.Fragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;

public class StatFragment extends Fragment {

    private static final String TAG = LogConfig.genLogTag("StatFragment");

    private ReverdApp mReverdApp;

    private Button mSubscribeButton;
    private Button mViewBlockedNumbersButton;

    private Button mViewBLButton;
    private Button mViewWLButton;

    private TextView mCommunityDb1;
    private TextView mCommunityDb2;

    private TextView mBlockedCalls1;
    private TextView mBlockedCalls2;

    private TextView mBLNumbers;
    private TextView mWLNumbers;

    private Handler mHandler = new Handler();

    // List of URIs changes are going to update this fragment.
    private static final Uri TABLEURIS[] = {
            Database.URI_TABLE_CALLDETAIL,
            Database.URI_TABLE_CALLSTATS,
            Database.URI_TABLE_BLACKLIST,
            Database.URI_TABLE_WHITELIST
    };

    private final ContentObserver mContentObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(final boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            Log.d(TAG, "onChange:" + uri.toString());
            updateContents();
        }
    };

    @Override
    public void onResume()
    {
        super.onResume();
        registerObservers();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        unregisterObservers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_stats, null);

        mSubscribeButton = (Button)view.findViewById(R.id.subscribeButton);

        mSubscribeButton.setOnClickListener(new OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    Log.d(TAG, "Subscribe button");
                                                    Fragment f = new SubscriptionFragment();
                                                    FragmentHelper.ReplaceFragment(getFragmentManager(), f);
                                                }
                                            }
        );

        mViewBlockedNumbersButton = (Button)view.findViewById(R.id.viewBlockedNumbersButton);
        mViewBlockedNumbersButton.setOnClickListener(new OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             Fragment f = new BlockedListFragment();
                                                             FragmentHelper.ReplaceFragment(getFragmentManager(), f);
                                                         }
                                                     }
        );

        mViewBLButton = (Button)view.findViewById(R.id.viewBlackListButton);
        mViewBLButton.setOnClickListener(new OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             Fragment f = new BlackListFragment();
                                                             FragmentHelper.ReplaceFragment(getFragmentManager(), f);
                                                         }
                                                     }
        );

        mViewWLButton = (Button)view.findViewById(R.id.viewWhiteListButton);
        mViewWLButton.setOnClickListener(new OnClickListener() {
                                                         @Override
                                                         public void onClick(View v) {
                                                             Fragment f = new WhiteListFragment();
                                                             FragmentHelper.ReplaceFragment(getFragmentManager(), f);
                                                         }
                                                     }
        );

        mCommunityDb1 = (TextView) view.findViewById(R.id.db_status1);
        mCommunityDb2 = (TextView)view.findViewById(R.id.db_status2);

        mBlockedCalls1 = (TextView)view.findViewById(R.id.blockedCalls1);
        mBlockedCalls2 = (TextView)view.findViewById(R.id.blockedCalls2);

        mBLNumbers = (TextView)view.findViewById(R.id.bl_phonenumbers);
        mWLNumbers = (TextView)view.findViewById(R.id.wl_phonenumbers);

        mReverdApp = (ReverdApp) getActivity().getApplicationContext();

        final String title = getString(R.string.stats_title);
        ((BaseActivity)getActivity()).setTitle(title);
        ((BaseActivity)getActivity()).setActionbarTextColorWhite();

        return view;
    }

    private void updateContents() {
        final Database db = mReverdApp.getDatabase();

        // Community database date.
        final AppPreferences ap = new AppPreferences(getActivity());
        final long currentStamp = Util.getTimestamp();
        final String temp = ap.get(AppPreferences.PREF_LAST_SYNC_DATE, Long.toString(currentStamp));
        final long lastUpdateStamp = Long.valueOf(temp);

        Log.d(TAG, "currentStamp = " + currentStamp + ", lastUpdateStamp = " + lastUpdateStamp);

        // Difference in milliseconds.
        long diff = currentStamp - lastUpdateStamp;
        final long twentyFourHoursInMiliseconds = 86400000L;

        Log.d(TAG, "Difference: " + diff);

        if (diff <= twentyFourHoursInMiliseconds) {
            mCommunityDb1.setText(R.string.up_to_date);
            mCommunityDb2.setVisibility(View.GONE);
        }
        else {
            // Older than a day.
            mCommunityDb1.setText(R.string.is_outdated);
            final Long daysOld = Util.daysBetween(lastUpdateStamp, currentStamp);

            Log.d(TAG, "Database age: " + daysOld + " days");

            String relative = "";
            if (daysOld <= 1) {
                relative = Long.toString(daysOld) + " " + getString(R.string.day_old);
            }
            else {
                relative = Long.toString(daysOld) + " " + getString(R.string.days_old);
            }
            mCommunityDb2.setText(relative);
        }

        // Number of blocked calls.
        // Number of days to go back.
        final int days = 30;

        final long counter = db.getRoughCallCounter(Constants.NUM_BLACKLISTED, days);

        final String BlockedCalls = getString(R.string.blocked_calls);
        mBlockedCalls1.setText(counter + " " + BlockedCalls);

        // Black list:
        final String PhoneNumbers = getString(R.string.num_phone_numbers);
        long ownBLNumbers = db.getLocalBlacklistSize();
        mBLNumbers.setText(ownBLNumbers + " " + PhoneNumbers);

        // White list:
        long ownWLNumbers = db.getLocalWhitelistSize();
        mWLNumbers.setText(ownWLNumbers + " " + PhoneNumbers);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        updateContents();
    }

    private void unregisterObservers() {
        getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
        Log.d(TAG, "Unregistered observer for " + mContentObserver.toString());
    }

    private void registerObservers() {
        for (Uri u: TABLEURIS) {
            Log.d(TAG, "Registered observer for " + u.toString());
            getActivity().getContentResolver().registerContentObserver(u, true, mContentObserver);
        }
    }

    /*
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorBlack();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.edit_list_entry));
        }
    }
    */
}
