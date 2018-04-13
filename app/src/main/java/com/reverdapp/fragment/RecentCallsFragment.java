package com.reverdapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.fragment.task.AsyncAddToBlackList;
import com.reverdapp.fragment.task.AsyncAddToWhiteList;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HandleBack;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class RecentCallsFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder,
        OnItemClickListener, HandleBack {

    private static final int MENU_CONTEXT_BLACKLIST_ID = 1;
    private static final int MENU_CONTEXT_WHITELIST_ID = 2;

    private static final String TAG = LogConfig.genLogTag("RecentCallsFragment");

    private ListView mListView;
    private ReverdApp mReverdApp;
    private static final String SELECTION = CallLog.Calls.TYPE + "=" + CallLog.Calls.INCOMING_TYPE;

    private static final String[] PROJECTION=new String[] {
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.DURATION,
            CallLog.Calls.DATE,
            CallLog.Calls.COUNTRY_ISO,
            CallLog.Calls.NUMBER_PRESENTATION };

    private SimpleCursorAdapter mAdapter =null;

    private Handler mHander = new Handler();

    // Content observer used to update the list when the database changes.
    private ContentObserver mContentObserver;

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mContentObserver = new ContentObserver(mHander) {
            @Override
            public void onChange(boolean selfChange, Uri uri) {

                if (uri == null) {
                    onChange(selfChange);
                    return;
                }

                super.onChange(selfChange, uri);

                final Activity act = getActivity();
                if (act == null) {
                    Log.d(TAG, "onChange, no activity - cannot continue without getting a resolver");
                    return;
                }

                final Cursor c = act.getContentResolver().query(uri, null, null, null, null);
                if (c.moveToLast()) {

                    final int type = c.getColumnIndex(CallLog.Calls.TYPE);
                    final int dircode = c.getInt(type);
                    final int number = c.getColumnIndex(CallLog.Calls.NUMBER);
                    final String phone = c.getString(number);

                    switch (dircode) {
                        case CallLog.Calls.MISSED_TYPE:
                        case CallLog.Calls.INCOMING_TYPE: {
                            Log.d(TAG, "Data changed " + selfChange + ", phone: " + phone + ", code: " + dircode);
                            mAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                }
            }

            @Override
            public boolean deliverSelfNotifications() {
                return super.deliverSelfNotifications();
            }
        };
    }

    private void forceListRefresh() {
        Log.d(TAG, "forceListRefresh");
        getLoaderManager().getLoader(0).forceLoad();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_recent_calls, null);
        mListView = (ListView) view.findViewById(R.id.fragment_recent_calls_call_list);
        mReverdApp = (ReverdApp) getActivity().getApplicationContext();

        mAdapter = new SimpleCursorAdapter(getActivity(), R.layout.row_recent_call_list, null, new String[] {
                        CallLog.Calls.NUMBER, CallLog.Calls.DURATION, CallLog.Calls.DATE, CallLog.Calls.COUNTRY_ISO }, new int[] {
                        R.id.number, R.id.status, R.id.date, R.id.origin }, 0);

        mAdapter.setViewBinder(this);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        registerForContextMenu(mListView);

        // set actionbar view
        ((BaseActivity)getActivity()).setActionbarTextColorWhite();

        String title = getString(R.string.recent_calls_title);
        if (mReverdApp.isDebugBuild()) {
            title += " (debug, all calls)";
        }

        ((BaseActivity)getActivity()).setTitle(title);

        // Content observer:
        getActivity().getContentResolver().registerContentObserver(
                CallLog.Calls.CONTENT_URI, true,
                mContentObserver);

        return view;
    }
    
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onBackPress() {
    }

    @Override
    public void onResume() {
        super.onResume();
        forceListRefresh();
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        final long now = System.currentTimeMillis();
        final long amountOfTimeAgo = now-(48*60*60*1000); // 2 days
        final String DATE_SELECTION;

        if (mReverdApp.isDebugBuild()) {
            DATE_SELECTION = SELECTION;
        }
        else {
            DATE_SELECTION = SELECTION + " AND " + CallLog.Calls.DATE + " >= " + amountOfTimeAgo;
        }

        return(new CursorLoader(getActivity(), CallLog.Calls.CONTENT_URI,
                   PROJECTION, DATE_SELECTION, null, CallLog.Calls.DATE + " DESC"));
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private String formatNumber(String number) {
        return Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(number,getActivity());
    }

    private String getCountryAndRegion(String number) {
        return Iso2Phone.getCountryAndRegion(getActivity(), number);
    }

    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        if (columnIndex==1) {
            // Phone number
            final String number=cursor.getString(columnIndex);
            final String formattedNumber = formatNumber(number);
            Log.d(TAG, "Call number: " + formattedNumber);
            ((TextView) view).setText(formattedNumber);

            return true;
        }
        else if (columnIndex==2) {
            // Do nothing on purpose.
            // This column is being used to show the origin of the call.
            final long duration = cursor.getLong(columnIndex);
            final String d = formatDuration(duration);
            ((TextView)view).setText(d);
            return true;
        }
        else if (columnIndex==3) {
            // Date
            long time=cursor.getLong(columnIndex);
            // Log.d(TAG, "Time: " + time);
            final String format = new String("yyyy-MM-dd HH:mm:ss");
            final SimpleDateFormat sdf = new SimpleDateFormat(format);

            Date d = new Date(time);
            String formattedTime = sdf.format(d);
            ((TextView)view).setText(formattedTime);
            return true;
        }
        else if (columnIndex==4) {
            String origin = cursor.getString(columnIndex);
            String prefix = "+"+Iso2Phone.getPhone( origin );
            Log.d(TAG, "Country iso : " + origin + " prefix : " + prefix );

            ViewGroup vg = (ViewGroup)view.getParent();
            TextView numberText = (TextView) vg.findViewById(R.id.number);
            String inumber = numberText.getText().toString();
            if ( !inumber.startsWith(prefix) )
            {
               inumber = prefix + inumber.replaceAll(Pattern.quote("+"),"");
               numberText.setText( inumber );
            }

            TextView statusText = (TextView) vg.findViewById(R.id.status);
            String status = getNumberStatus(inumber);
            Log.d( TAG, "Full number : " + inumber + " status : " + status );
            statusText.setText(status);

            // refine the origin
            origin = getCountryAndRegion(inumber);
            ((TextView)view).setText(origin);

            return true;
        }
        else if (columnIndex==5) {
            Log.d(TAG, "Number presentation : " + cursor.getString(columnIndex) );
            return true;
        }
        else {
            return false;
        }
    }

    private String getNumberStatus(String formattedNumber) {
        String ret;
        Database db = mReverdApp.getDatabase();
        switch (db.checkIfNumIsBlackOrWhite(formattedNumber))
        {
            case Constants.NUM_BLACKLISTED:
                ret = getString(R.string.recent_calls_status_blacklist);
                break;
            case Constants.NUM_WHITELISTED:
                ret = getString(R.string.recent_calls_status_whitelist);
                break;
            default:
                ret = "";
                break;
        }

        return ret;
    }

    private final String formatDuration(long duration) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        df.setTimeZone(tz);
        String time = df.format(new Date(duration * 1000L));
        return time;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        final String formattedNumber = getSelectedPhoneNumber(info);
        menu.setHeaderTitle(formattedNumber);

        final String add_to_bl = getString(R.string.recent_calls_add_to_blacklist);
        // final String add_to_wl = getString(R.string.recent_calls_add_to_whitelist);

        MenuItem blItem = menu.add(Menu.NONE, MENU_CONTEXT_BLACKLIST_ID, Menu.NONE, add_to_bl);
        //MenuItem wlItem = menu.add(Menu.NONE, MENU_CONTEXT_WHITELIST_ID, Menu.NONE, add_to_wl);

        final Database db = mReverdApp.getDatabase();
        if (db.checkIfNumIsBlackOrWhite(formattedNumber) == Constants.NUM_BLACKLISTED) {
            blItem.setEnabled(false);
            //wlItem.setEnabled(false);
        }
    }

    private String getSelectedPhoneNumber(final AdapterView.AdapterContextMenuInfo info)
    {
        Cursor c = (Cursor)mListView.getItemAtPosition(info.position);
        String number=c.getString(c.getColumnIndex(CallLog.Calls.NUMBER));
        String origin = c.getString(c.getColumnIndex(CallLog.Calls.COUNTRY_ISO));
        String prefix = "+"+Iso2Phone.getPhone( origin );
        if ( !number.startsWith(prefix) )
        {
           number = prefix + number.replaceAll(Pattern.quote("+"),"");
        }
        final String formattedNumber = formatNumber(number);
        return formattedNumber;
    }

    private class WhiteListFragmentConnection implements IFragmentConnection {
        private String mNumber;

        public WhiteListFragmentConnection(String number)
        {
            mNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(number,getActivity());
        }

        @Override
        public Bundle getAsyncTaskOptions() {
            Bundle b = new Bundle();

            b.putBoolean(IFragmentConnection.IFC_ADD_TO_COMM_WL_ID, false);
            b.putString(IFragmentConnection.IFC_FULL_NUMBER_ID, mNumber);
            b.putString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID, Iso2Phone.stripCountryPrefix(mNumber));
            b.putString(IFragmentConnection.IFC_NOTE_ID, mReverdApp.getResources().getString(R.string.number_added_by_reverd_app));

            return b;
        }

        @Override
        public void handlePostExecute(int destination) {
            // Empty on purpose.
            refreshList();
        }
    };

    private class BlackListFragmentConnection implements IFragmentConnection {
        private String mNumber;
        private boolean mRemoteAdd;
        public BlackListFragmentConnection(String number, boolean remoteAdd)
        {
            mNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(number,getActivity());
            mRemoteAdd = remoteAdd;
            Log.d(TAG, "Formatted number: " + number + ", remote add: " + mRemoteAdd);
        }

        @Override
        public Bundle getAsyncTaskOptions() {
            Bundle b = new Bundle();

            b.putString(IFragmentConnection.IFC_FULL_NUMBER_ID, mNumber);
            b.putBoolean(IFragmentConnection.IFC_ADD_TO_COMM_BL_ID, mRemoteAdd);

            b.putString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID, Iso2Phone.stripCountryPrefix(mNumber));

            final String prefix = Iso2Phone.getCountryPrefix(mNumber);
            b.putInt(IFragmentConnection.IFC_COUNTRY_NUM_CODE_ID, Integer.valueOf(prefix));

            Log.d(TAG, "Using prefix: " + prefix);
            final String countryName = mReverdApp.getDatabase().getCountryName(prefix);
            Log.d(TAG, "Resolved country name: " + countryName);
            b.putString(IFragmentConnection.IFC_COUNTRY_NAME_ID, countryName);

            b.putString(IFragmentConnection.IFC_NAME_ID, "");
            b.putString(IFragmentConnection.IFC_NOTE_ID, mReverdApp.getResources().getString(R.string.number_added_by_reverd_app));

            return b;
        }

        @Override
        public void handlePostExecute(int destination) {
            // Do nothing on purpose.
            refreshList();
        }
    }

    private void addToBlackList(final String number) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(getString(R.string.alert_report_this_number_to_the_community));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (Util.isNetworkAvailable(getActivity())) {
                    AsyncAddToBlackList task = new AsyncAddToBlackList(new BlackListFragmentConnection(number, true), getActivity(), IFragmentConnection.UNUSED_NAVIGATE_ID);
                    task.execute();

                    // TODO: Add the same number to the community blacklist.
                    // TODO: !!!
                    Log.d(TAG, "Adding to community list.");
                } else {
                    Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                AsyncAddToBlackList task = new AsyncAddToBlackList(new BlackListFragmentConnection(number, true), getActivity(), IFragmentConnection.UNUSED_NAVIGATE_ID);
                task.execute();
                Log.d(TAG, "Adding to local list only.");
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private void addToWhiteList(final String number) {
        AsyncAddToWhiteList asyncAddToWhiteList = new AsyncAddToWhiteList(new WhiteListFragmentConnection(number), getActivity(), IFragmentConnection.UNUSED_NAVIGATE_ID);
        asyncAddToWhiteList.execute();
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        boolean unhandled = false;

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        switch (item.getItemId()) {

            case MENU_CONTEXT_BLACKLIST_ID:
                String blackListNumber = getSelectedPhoneNumber(info);
                Log.d(TAG, "blackListNumber: " + blackListNumber);
                //addToBlackList(blackListNumber);
                showAddToBlackListFragment(blackListNumber);
                break;

            case MENU_CONTEXT_WHITELIST_ID:
                String whiteListNumber = getSelectedPhoneNumber(info);
                Log.d(TAG, "whiteListNumber: " + whiteListNumber);
                addToWhiteList(whiteListNumber);
                break;

            default:
                unhandled = true;
        }

        if (unhandled) {
            return false;
        }
        else {
            return true;
        }
    }

    private void showAddToBlackListFragment(String number) {
        Fragment f = new AddToBlackListFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean(AddToBlackListFragment.OPTION_SETUP_FROM_ARGUMENTS, true);
        bundle.putString(AddToBlackListFragment.OPTION_FULL_NUMBER, number);
        f.setArguments(bundle);
        FragmentHelper.ReplaceFragment(getFragmentManager(), f);
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
}
