package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncDeleteFromBlacklist;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HandleBack;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;

public class BlockedListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder
{

    private static final String TAG = LogConfig.genLogTag("BlockedListFragment");

    private SimpleCursorAdapter mAdapter =null;

    private ListView mListView;

    private ReverdApp mReverdApp;

    private boolean mForceRefresh;

    private Handler mHander = new Handler();

    // Content observer used to update the list when the database changes.
    private ContentObserver mContentObserver;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mForceRefresh = false;

        mContentObserver = new ContentObserver(mHander) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                if (!selfChange) {
                    Log.d(TAG, "Data changed " + selfChange);
                    forceListRefresh();
                }
            }

            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Log.d(TAG, "onCreateView");

        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_blockedlist, null);
        mListView = (ListView) view.findViewById(R.id.blockedlist);
        mReverdApp = (ReverdApp) getActivity().getApplicationContext();

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.row_blocked_list,
                null /* cursor not available */,
                new String[] {
                        DatabaseField.CALLDETAIL_NUMBER,
                        DatabaseField.CALLDETAIL_DATE
                },
                new int[] {
                        R.id.number,
                        R.id.date},
                0);

        mAdapter.setViewBinder(this);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        registerForContextMenu(mListView);

        // set actionbar view
        ((BaseActivity)getActivity()).setActionbarTextColorBlack();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.blocked_list_title));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    private void forceListRefresh() {
        Log.d(TAG, "forceListRefresh");
        getLoaderManager().getLoader(0).forceLoad();
    }

    // Refresh the list of phone numbers.
    private void refreshList() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mListView.invalidateViews();
                mListView.refreshDrawableState();
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    /* Loader */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity()) {
            @Override
            public Cursor loadInBackground() {
                final long to = System.currentTimeMillis() / 1000L;
                long from = to - (30*24L*60L*60L);
                final Cursor cursor = mReverdApp.getDatabase().getCalls(Constants.NUM_BLACKLISTED, from, to);
                Log.d(TAG, "Got cursor, size " + cursor.getCount());

                // Register an observer for changes in the DB.
                cursor.getCount();
                cursor.registerContentObserver(mContentObserver);
                cursor.setNotificationUri(getContext().getContentResolver(), Database.URI_TABLE_CALLDETAIL);

                return cursor;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        Log.d(TAG, "onLoadFinished");
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        final Cursor oldCursor = mAdapter.getCursor();
        if (oldCursor != null) {
            oldCursor.unregisterContentObserver(mContentObserver);
            Log.d(TAG, "Cursor, unregisterContentObserver");
        }
        mAdapter.swapCursor(null);
    }

    /* Loader */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex == 2) {
            // Timestamp in seconds.
            final Long timestamp = cursor.getLong(columnIndex);

            Locale l = Locale.getDefault();
            Calendar cal = Calendar.getInstance(l);
            cal.setTimeInMillis(timestamp*1000);
            String date = DateFormat.format("dd-MM-yyyy hh:mm", cal).toString();

            final TextView dateTV = (TextView) view;
            dateTV.setText(date);

            // Log.d(TAG, "Timestamp = " + timestamp + ", date = " + date);
            return true;
        }
        return false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");

        if (mForceRefresh)
        {
            Log.d(TAG, "Forcing a refresh..");
            mForceRefresh = false;
            forceListRefresh();
        }
    }
}
