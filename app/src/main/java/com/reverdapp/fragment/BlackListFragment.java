package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.database.ContentObserver;
import android.net.Uri;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncDeleteFromBlacklist;
import com.reverdapp.fragment.task.AsyncFetchBlacklist;
import com.reverdapp.fragment.task.AsyncMoveBlacklistToWhitelist;
import com.reverdapp.fragment.task.FragmentConnectionHelper;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HomeActivity;
import com.reverdapp.view.HandleBack;

import java.util.ArrayList;
import java.util.HashMap;

public class BlackListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder,
        OnItemClickListener, HandleBack
{

    private static final String TAG = LogConfig.genLogTag("BlackListFragment");

    private static final int MENU_CONTEXT_EDIT_ID = 1;
    private static final int MENU_CONTEXT_DELETE_ID = 2;

    private SimpleCursorAdapter mAdapter =null;

    private ListView mListView;

    private Menu menu;
    private ReverdApp mReverdApp;

    // The selected elements from the list of numbers.
    private HashMap<Integer, NumberListModel> mSelectedItems = new HashMap<Integer, NumberListModel>();

    // Indicates if the user is selecting numbers,
    private boolean mSelectingMode;
    private boolean mForceRefresh;

    // List of URIs changes are going to update this fragment.
    private static final Uri TABLEURIS[] = {
            Database.URI_TABLE_BLACKLIST
    };

    @Override
    public void onCreate (Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        mForceRefresh = false;
        mSelectingMode = false;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_blacklist, null);
        mListView = (ListView) view.findViewById(R.id.fragment_blacklist_lv_black_list_number);
        mReverdApp = (ReverdApp) getActivity().getApplicationContext();
        updateContents();
        return view;
    }

    public void updateContents() {

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.row_black_list,
                null /* cursor not available */,
                new String[] {
                        DatabaseField.BLACKLIST_FULL_NUMBER,
                        DatabaseField.BLACKLIST_CALLER
                },
                new int[] {
                        R.id.row_black_list_tv_number,
                        R.id.row_black_list_tv_name},
                0);

        mAdapter.setViewBinder(this);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);
        registerForContextMenu(mListView);
        mListView.setOnItemClickListener(this);

        // set actionbar view
        ((BaseActivity)getActivity()).setActionbarTextColorBlack();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.black_list_title));
        Log.d(TAG, "Refreshing contents");
    }

    private Handler mHandler = new Handler();

    private final ContentObserver mContentObserver = new ContentObserver(mHandler) {
        @Override
        public void onChange(final boolean selfChange) {
            Log.d(TAG, "Black-list onChange:" + selfChange);
            onChange(selfChange, null);
        }

        @Override
        public void onChange(final boolean selfChange, final Uri uri) {
            Log.d(TAG, "onChange:" + uri.toString());
            updateContents();
        }
    };

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

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        /*
        long size = mReverdApp.getDatabase().getBlacklistSize();

        if (size <=0) {
            // Fetch from network.
            callBlackListService();
        }
        */
    }

    // Load data from service.
    private void callBlackListService() {
        
        if (Util.isNetworkAvailable(getActivity())) {
            IFragmentConnection fc = new IFragmentConnection() {
                @Override
                public Bundle getAsyncTaskOptions() {
                    // Not used.
                    return null;
                }

                @Override
                public void handlePostExecute(int destination) {
                    forceListRefresh();
                    refreshList();
                }
            };

            AsyncFetchBlacklist task = new AsyncFetchBlacklist(getActivity(),
                    (ProgressBar) getView().findViewById(R.id.fragment_blacklist_pb),
                    fc);
            task.execute();
        }else {
            Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long arg3) {
        final Cursor c = (Cursor)mAdapter.getItem(position);

        if (mSelectingMode) {
            NumberListModel model = mSelectedItems.get(position);

            if (model == null) {
                model = NumberListModel.createBlackListModel(c);
                model.setSelected(true);
                mSelectedItems.put(new Integer(position), model);
            }
            else {
                if (model.isSelected()) {
                    model.setSelected(false);
                } else {
                    model.setSelected(true);
                }
            }

            Log.d(TAG, "Selected item: " + model.getFullNumber());

            CheckBox cb = (CheckBox)view.findViewById(R.id.row_black_list_cb_edit);
            cb.setVisibility(View.VISIBLE);
            cb.setChecked(model.isSelected());

            refreshList();
        }
        else
        {
            final NumberListModel model = NumberListModel.createBlackListModel(c);
            model.setSelected(true);
            Log.d(TAG, "Viewing item: " + model.getFullNumber());
            DetailFragment f = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.OPTION_MODEL, model);
            bundle.putInt(DetailFragment.OPTION_SOURCE, DetailFragment.SOURCE_BLACKLIST);
            f.setArguments(bundle);
            f.setTargetFragment(this, R.id.FRAGMENT_ID_DETAILS);

            mForceRefresh = true;
            FragmentHelper.ReplaceFragment(getFragmentManager(), f);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        final Cursor c = (Cursor)mListView.getItemAtPosition(info.position);
        final NumberListModel model = NumberListModel.createBlackListModel(c);
        menu.setHeaderTitle(model.getFullNumber());
        final String MENU_CONTEXT_EDIT_TEXT = getString(R.string.white_list_edit);
        final String MENU_CONTEXT_DELETE_TEXT = getString(R.string.white_list_delete);

        // Disable menus for items which are not created by the user.
        final MenuItem editMenuItem = menu.add(Menu.NONE, MENU_CONTEXT_EDIT_ID, Menu.NONE, MENU_CONTEXT_EDIT_TEXT);
        editMenuItem.setEnabled(model.canBeEdited());
        final MenuItem deleteMenuItem = menu.add(Menu.NONE, MENU_CONTEXT_DELETE_ID, Menu.NONE, MENU_CONTEXT_DELETE_TEXT);
        deleteMenuItem.setEnabled(model.canBeEdited());
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        boolean unhandled = false;
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        final Cursor c = (Cursor)mListView.getItemAtPosition(info.position);
        final NumberListModel model = NumberListModel.createBlackListModel(c);

        switch (item.getItemId()) {
            case MENU_CONTEXT_EDIT_ID:
                Log.d(TAG, "MENU_CONTEXT_EDIT_ID");
                editEntry(model);
                break;
            case MENU_CONTEXT_DELETE_ID:
                Log.d(TAG, "MENU_CONTEXT_DELETE_ID");
                deleteEntry(model);
                break;
            default:
                unhandled = true;
        }
        if (unhandled) {
            Log.w(TAG, "Unhandled menu item with id " + item.getItemId());
            return false;
        }
        else {
            return true;
        }
    }

    private void deleteEntry(final NumberListModel model) {
        model.setSelected(true);
        Log.d(TAG, "Deleting " + model.getId());

        IFragmentConnection fc = new IFragmentConnection() {
            @Override
            public Bundle getAsyncTaskOptions() {
                Bundle b = new Bundle();
                // Model to delete:
                final ArrayList<NumberListModel> list = new ArrayList<NumberListModel>();
                list.add(model);
                b.putParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST, list);

                return b;
            }

            @Override
            public void handlePostExecute(int destination) {
                // Refresh list.
                Log.d(TAG, "Refreshing BL after delete");
                forceListRefresh();
            }
        };

        AsyncDeleteFromBlacklist task = new AsyncDeleteFromBlacklist(getActivity(), fc);
        task.execute();
    }

    private void forceListRefresh() {
        Log.d(TAG, "forceListRefresh");
        getLoaderManager().getLoader(0).forceLoad();
    }

    private void editEntry(NumberListModel model) {
        Log.d(TAG, "Editing " + model.getId());

        mForceRefresh = true;

        EditListEntryFragment f = new EditListEntryFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(EditListEntryFragment.TYPE_ID, EditListEntryFragment.TYPE_BL_ID);
        bundle.putInt(EditListEntryFragment.ENTRY_ID, model.getId());
        f.setArguments(bundle);
        FragmentHelper.ReplaceFragment(getFragmentManager(), f);
    }

    public void setActionbarWithEditOption() {
        menu.findItem(R.id.action_add_to_blacklist).setVisible(true);
        menu.findItem(R.id.action_edit).setVisible(true);
        menu.findItem(R.id.action_white_list).setVisible(false);
    }

    private void setActionbarWithoutEditOption() {
        menu.findItem(R.id.action_add_to_blacklist).setVisible(false);
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_white_list).setVisible(true);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if (item.getItemId() == R.id.action_add_to_blacklist) {
            mForceRefresh = true;
            AddToBlackListFragment f = new AddToBlackListFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(getString(R.string.put_extra_is_from_blacklist), true);
            f.setArguments(bundle);
            FragmentHelper.ReplaceFragment(getFragmentManager(), f);
        } else if(item.getItemId() == R.id.action_edit) {
            // Select phone numbers.
            selectingModeStarted();
        } else if (item.getItemId() == R.id.action_white_list) {

            final IFragmentConnection fc = new IFragmentConnection() {
                @Override
                public Bundle getAsyncTaskOptions() {
                    Bundle b = new Bundle();
                    ArrayList<NumberListModel> list = new ArrayList<NumberListModel>();
                    for (NumberListModel m: mSelectedItems.values())
                    {
                        if (m.isSelected()) {
                            list.add(m);
                        }
                    }
                    Log.d(TAG, "getAsyncTaskOptions: " + IFragmentConnection.IFC_NUMBER_LIST + ", size " + list.size());
                    b.putParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST, list);
                    return b;
                }

                @Override
                public void handlePostExecute(int destination) {
                    forceListRefresh();
                    refreshList();
                    selectingModeFinished();
                }
            };

            // Move numbers selected to another list.
            final int numberOfSelectedItems = FragmentConnectionHelper.getNumberOfSelectedItems(fc.getAsyncTaskOptions(), IFragmentConnection.IFC_NUMBER_LIST);
            Log.d(TAG, "Selected items: " + numberOfSelectedItems);

            if (numberOfSelectedItems > 0) {
                // if (Util.isNetworkAvailable(getActivity())) {
                    AsyncMoveBlacklistToWhitelist task = new AsyncMoveBlacklistToWhitelist(fc, getActivity());
                    task.execute();
                // } else {
                //     Util.displayDialog(getString(R.string.app_name),
                //             getString(R.string.alert_no_internet),
                //             getActivity());
                // }
            } else {
                Toast.makeText(getActivity(), getString(R.string.alert_please_select_atleast_one_number), Toast.LENGTH_LONG).show();
            }
        }
        
        return super.onOptionsItemSelected(item);
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

    /*
    public void setEditActionBack() {
        selectingModeFinished();
        setActionbarWithEditOption();
    }
    */

    /* Loader */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity()){
            @Override
            public Cursor loadInBackground() {
                Cursor cursor = mReverdApp.getDatabase().getAllBlackListedNumber();
                Log.d(TAG, "Got cursor, size " + cursor.getCount());
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
        mAdapter.swapCursor(null);
    }

    /* Loader */
    @Override
    public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
        final LinearLayout directParent = (LinearLayout)view.getParent();
        final ViewGroup vg = (ViewGroup)directParent.getParent();
        final CheckBox cb = (CheckBox) vg.findViewById(R.id.row_black_list_cb_edit);
        cb.setClickable(false);

        if (columnIndex == 0 ) {
            if (mSelectingMode) {
                cb.setVisibility(View.VISIBLE);

                final int position = cursor.getPosition();

                NumberListModel model = mSelectedItems.get(position);
                if (model == null) {
                    cb.setChecked(false);
                } else {
                    cb.setChecked(model.isSelected());
                }
            } else {
                cb.setVisibility(View.GONE);
            }
        }

        return false;
    }

    @Override
    public void onBackPress() {
        
        if (mSelectingMode) {
            mSelectingMode = false;
            setActionbarWithEditOption();
        } else {
            getActivity().finish();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume");
        Log.d(TAG, "Forcing a refresh..");
        mForceRefresh = false;
        forceListRefresh();
        registerObservers();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterObservers();
    }

    private void selectingModeStarted() {
        Log.d(TAG, "selectingModeStarted");
        mSelectingMode = true;
        mSelectedItems.clear();
        setActionbarWithoutEditOption();
        refreshList();
    }

    private void selectingModeFinished() {
        Log.d(TAG, "selectingModeFinished");
        mSelectingMode = false;
        mSelectedItems.clear();
        setActionbarWithEditOption();
        refreshList();
    }

    public boolean isSelectionActive() {
        return mSelectingMode;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        setActionbarWithEditOption();
    }

}
