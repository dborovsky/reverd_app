package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
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
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncDeleteFromWhitelist;
import com.reverdapp.fragment.task.AsyncFetchWhitelist;
import com.reverdapp.fragment.task.AsyncMoveWhitelistToBlacklist;
import com.reverdapp.fragment.task.FragmentConnectionHelper;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HandleBack;

import java.util.ArrayList;
import java.util.HashMap;

public class WhiteListFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>,
        SimpleCursorAdapter.ViewBinder,
        OnItemClickListener, HandleBack
{

    private static final String TAG = LogConfig.genLogTag("WhiteListFragment");

    private static final int MENU_CONTEXT_EDIT_ID = 1;
    private static final int MENU_CONTEXT_DELETE_ID = 2;

    private SimpleCursorAdapter mAdapter=null;

    private ListView mListView;

    private Menu menu;
    private ReverdApp mReverdApp;

    // The selected elements from the list of numbers.
    private HashMap<Integer, NumberListModel> mSelectedItems = new HashMap<Integer, NumberListModel>();

    // Indicates if the user is selecting numbers,
    private boolean mSelectingMode;
    private boolean mForceRefresh;

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
        View view = inflater.inflate(R.layout.fragment_whitelist, null);
        mListView = (ListView) view.findViewById(R.id.fragment_whitelist_lv_white_list_number);
        mReverdApp = (ReverdApp) getActivity().getApplicationContext();

        mAdapter = new SimpleCursorAdapter(getActivity(),
                R.layout.row_white_list,
                null /* cursor not available */,
                new String[] {
                        DatabaseField.WHITELIST_FULL_NUMBER,
                        DatabaseField.WHITELIST_CALLER
                },
                new int[] {
                        R.id.row_white_list_tv_number,
                        R.id.row_white_list_tv_name},
                0);

        mAdapter.setViewBinder(this);
        mListView.setAdapter(mAdapter);
        getLoaderManager().initLoader(0, null, this);

        registerForContextMenu(mListView);

        mListView.setOnItemClickListener(this);

        // set actionbar view
        ((BaseActivity)getActivity()).setActionbarTextColorWhite();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.white_list_title));

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
/*
        long size = mReverdApp.getDatabase().getWhitelistSize();

        if (size <=0) {
            // Fetch from network.
            callWhiteListSrvice();
        }
*/
    }

    private void callWhiteListSrvice() {
        
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

            AsyncFetchWhitelist task = new AsyncFetchWhitelist(getActivity(),
                    (ProgressBar) getView().findViewById(R.id.fragment_whitelist_pb),
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
                model = NumberListModel.createWhiteListModel(c);
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

            CheckBox cb = (CheckBox)view.findViewById(R.id.row_white_list_cb_edit);
            cb.setVisibility(View.VISIBLE);
            cb.setChecked(model.isSelected());

            refreshList();
        }
        else
        {
            final NumberListModel model = NumberListModel.createWhiteListModel(c);
            model.setSelected(true);

            Log.d(TAG, "Viewing item: " + model.getFullNumber());

            DetailFragment f = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putParcelable(DetailFragment.OPTION_MODEL, model);
            bundle.putInt(DetailFragment.OPTION_SOURCE, DetailFragment.SOURCE_WHITELIST);
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
        final NumberListModel model = NumberListModel.createWhiteListModel(c);
        menu.setHeaderTitle(model.getFullNumber());
        final String MENU_CONTEXT_EDIT_TEXT = getString(R.string.white_list_edit);
        final String MENU_CONTEXT_DELETE_TEXT = getString(R.string.white_list_delete);
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
        final NumberListModel model = NumberListModel.createWhiteListModel(c);

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

        final IFragmentConnection fc = new IFragmentConnection() {
            @Override
            public Bundle getAsyncTaskOptions() {
                Bundle b = new Bundle();
                // Model to delete:
                ArrayList<NumberListModel> list = new ArrayList<NumberListModel>();
                list.add(model);
                b.putParcelableArrayList(IFragmentConnection.IFC_NUMBER_LIST, list);

                return b;
            }

            @Override
            public void handlePostExecute(int destination) {
                // Refresh list.
                Log.d(TAG, "Refreshing WL after delete");
                forceListRefresh();
            }
        };

        final AsyncDeleteFromWhitelist task = new AsyncDeleteFromWhitelist(getActivity(), fc);
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
        bundle.putInt(EditListEntryFragment.TYPE_ID, EditListEntryFragment.TYPE_WL_ID);
        bundle.putInt(EditListEntryFragment.ENTRY_ID, model.getId());
        f.setArguments(bundle);

        FragmentHelper.ReplaceFragment(getFragmentManager(), f);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        setActionbarWithEditOption();
    }

    public void setActionbarWithEditOption(){

        menu.findItem(R.id.action_edit).setVisible(true);
        menu.findItem(R.id.action_blacklist).setVisible(false);
    }
    
    private void setActionbarWithoutEditOption(){
        menu.findItem(R.id.action_edit).setVisible(false);
        menu.findItem(R.id.action_blacklist).setVisible(true);
        }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_edit) {
            // Select phone numbers.
            selectingModeStarted();
        } else if (item.getItemId() == R.id.action_blacklist) {

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
                    AsyncMoveWhitelistToBlacklist task = new AsyncMoveWhitelistToBlacklist(fc, getActivity());
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

    public void setEditActionBack() {
        selectingModeFinished();
        setActionbarWithEditOption();
    }

    /* Loader */

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity()){
            @Override
            public Cursor loadInBackground() {
                Cursor cursor = mReverdApp.getDatabase().getAllWhiteListedNumber();
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
        final CheckBox cb = (CheckBox) vg.findViewById(R.id.row_white_list_cb_edit);
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

        if (mForceRefresh)
        {
            Log.d(TAG, "Forcing a refresh..");

            mForceRefresh = false;
            forceListRefresh();
        }
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
}
