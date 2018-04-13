package com.reverdapp.fragment;

import android.app.Fragment;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.LinearLayout;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.adapter.CountryAdapter;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncDetail;
import com.reverdapp.fragment.task.UpdateBlacklistEntry;
import com.reverdapp.fragment.task.UpdateWhitelistEntry;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;

import java.util.ArrayList;

public class EditListEntryFragment extends Fragment {

    public static final String TYPE_ID = "TYPE_ID";
    public static final int TYPE_WL_ID = 1;
    public static final int TYPE_BL_ID = 2;

    public static final String TYPE_IS_LOCAL_ID = "TYPE_IS_LOCAL_ID";

    public static final String ENTRY_ID = "ENTRY_ID";
    private static final String TAG = LogConfig.genLogTag("EditListEntryFragment");

    private int mType;
    private int mDbId;

    private Button mSubmitButton;
    private Spinner mCountrySpinner;
    private CountryAdapter mCountryAdapter;
    private ReverdApp mReverdApp;
    private EditText mNumber;
    private EditText mName;
    private EditText mNote;
    private LinearLayout mLayout;
    private ProgressBar mProgressBar;
    private int mPosition;
    private ArrayList<String> mCountries = new ArrayList<String>();
    private ArrayList<String> mCountryCodes = new ArrayList<String>();
    private ArrayList<Integer> mCallingCodes = new ArrayList<Integer>();
    //private boolean mNumberChanged;

    // TODO: handle local numbers differently.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mType = getArguments().getInt(EditListEntryFragment.TYPE_ID);
        mDbId = getArguments().getInt(EditListEntryFragment.ENTRY_ID);

        final View view = inflater.inflate(R.layout.fragment_edit_listentry, null);

        mLayout = (LinearLayout) view.findViewById(R.id.entry_layout);
        mLayout.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              Util.hideSoftKeyboard(getActivity());
            }
        });

        mSubmitButton = (Button) view.findViewById(R.id.entry_submitbtn);
        mSubmitButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!checkEnteredNumber()) {
                    Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_number_already_exists), getActivity());
                    return;
                }

                Log.d(TAG, "Clicked submit, type " + mType);
                switch (mType)
                {
                    case EditListEntryFragment.TYPE_WL_ID:
                    case EditListEntryFragment.TYPE_BL_ID:
                        saveListChanges();
                        break;
                    default:
                        Log.e(TAG, "Unhandled type.");
                        break;
                }

                // Close this fragment.
                switch (mType) {
                    case EditListEntryFragment.TYPE_WL_ID:
                        break;
                    case EditListEntryFragment.TYPE_BL_ID:
                        break;
                }
                // TODO: go back to another fragment depending on what was edited.

                getFragmentManager().popBackStack();
            }});

        mCountrySpinner = (Spinner) view.findViewById(R.id.entry_country);
        mNumber = (EditText) view.findViewById(R.id.entry_number);
        mName = (EditText) view.findViewById(R.id.entry_name);
        mNote = (EditText) view.findViewById(R.id.entry_note);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressbar);

        mReverdApp = (ReverdApp) getActivity().getApplicationContext();

        switch (mType)
        {
            case EditListEntryFragment.TYPE_WL_ID:
                setupWhiteListEntry();
                break;
            case EditListEntryFragment.TYPE_BL_ID:
                setupBlackListEntry();
                break;
        }

        /*
        mNumberChanged = false;
        mNumber.addTextChangedListener(new TextWatcher() {
                                           // unused
                                           @Override
                                           public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                                           // unused
                                           @Override
                                           public void onTextChanged(CharSequence s, int start, int before, int count) {}
                                           @Override
                                           public void afterTextChanged(Editable s) {
                                               mNumberChanged = true;
                                           }
                                       }
        );
        */

        mCountryAdapter = new CountryAdapter(getActivity(), mCountries);
        mCountrySpinner.setAdapter(mCountryAdapter);
        
        ((BaseActivity)getActivity()).setActionbarTextColorBlack();
        // TODO: set title based on arguments.
        ((BaseActivity)getActivity()).setTitle(getString(R.string.edit_list_entry));

        //mCountrySpinner.setSelection(reverdApp.getDatabase().getCountryPosition(reverdApp.sharedPreferences.getString(getString(R.string.pref_sim_country_code), "+1"))+1);
        mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                mPosition = position;
                Log.d(TAG, "Set spinner position to " + mPosition);
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });
        
        return view;
    }

    private boolean checkEnteredNumber() {

        final String number = mNumber.getText().toString();
        final int callingCode = mCallingCodes.get(mPosition);
        final String fullNumber = Database.generateFullNumberInDbFormat(Integer.toString(callingCode), number);

        // check if the number is the same number as the previous one.
        NumberListModel nlm = null;
        switch (mType) {
            case EditListEntryFragment.TYPE_BL_ID: {
                final Database db = mReverdApp.getDatabase();
                final Cursor c = db.getBlackListedNumberById(mDbId);
                c.moveToFirst();
                nlm = NumberListModel.createBlackListModel(c);
                break;
            }
            case EditListEntryFragment.TYPE_WL_ID: {
                final Database db = mReverdApp.getDatabase();
                final Cursor c = db.getWhiteListedNumberById(mDbId);
                c.moveToFirst();
                nlm = NumberListModel.createWhiteListModel(c);
                break;
            }
        }

        final String originalFullNumber = Database.generateFullNumberInDbFormat(Integer.toString(nlm.getCountryCode()), nlm.getPhoneNumber());

        Log.d(TAG, "Compare " + originalFullNumber + " vs " + fullNumber);
        if (originalFullNumber.equals(fullNumber)) {
            return true;
        }

        final int ret = mReverdApp.getDatabase().checkIfNumIsBlackOrWhite(fullNumber);

        if (ret == Constants.NUM_NOT_PRESENT) {
            Log.d(TAG, "Number: " + fullNumber + " can be added");
            return true;
        }

        Log.d(TAG, "Number: " + fullNumber + " cannot be added (" + ret + ")");
        return false;
    }

    // Save changes from UI to DB.
    private void saveListChanges() {
        Log.d(TAG, "saveListChanges, mType=" + mType);

        //final String countryName = mCountryAdapter.getItem(mCountrySpinnerPosition).toString();
        final String countryName = mCountryCodes.get(mPosition);
        final int countryCode = mCallingCodes.get(mPosition);
        final String numberToUpdate = mNumber.getText().toString();
        final String nameToUpdate = mName.getText().toString();
        final String noteToUpdate = mNote.getText().toString();

        final Database db = mReverdApp.getDatabase();

        switch (mType) {

            case EditListEntryFragment.TYPE_BL_ID: {
                // Update DB.
                db.updateBlackListEntry(mDbId,
                        countryCode,
                        countryName,
                        numberToUpdate,
                        nameToUpdate,
                        noteToUpdate);
                // Update WS:
                Cursor c = db.getBlackListedNumberById(mDbId);
                c.moveToFirst();
                final NumberListModel nlm = NumberListModel.createBlackListModel(c);
                final UpdateBlacklistEntry task = new UpdateBlacklistEntry(getActivity(), nlm);
                task.execute();
                break;
            }

            case EditListEntryFragment.TYPE_WL_ID: {
                // Update DB.
                db.updateWhiteListEntry(mDbId,
                        countryCode,
                        countryName,
                        numberToUpdate,
                        nameToUpdate,
                        noteToUpdate);
                // Update WS:
                Cursor c = db.getWhiteListedNumberById(mDbId);
                c.moveToFirst();
                final NumberListModel nlm = NumberListModel.createWhiteListModel(c);
                final UpdateWhitelistEntry task = new UpdateWhitelistEntry(getActivity(), nlm);
                task.execute();
                break;
            }
        }

        Log.d(TAG, "saveListChanges, position = " + mPosition + ", countryName = " + countryName);
    }

    private void hideProgressBar() {
        mProgressBar.setVisibility(View.GONE);
    }

    private void showProgressBar() {
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void fetchDetailsFromRemote(final String fullNumber, final int isLocal) {
        if (isLocal == 0) {
            showProgressBar();
            if (Util.isNetworkAvailable(getActivity())) {
                AsyncDetail task = new AsyncDetail(getActivity(),
                        fullNumber,
                        mProgressBar,
                        mName,
                        mNote);
                task.execute();
            } else {
                hideProgressBar();
                Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
            }
        }
        else {
            hideProgressBar();
        }
    }

    private void setupBlackListEntry() {
        final Database db = mReverdApp.getDatabase();
        final Cursor c = db.getBlackListedNumberById(mDbId);
        c.moveToFirst();

        final String phone = c.getString(c.getColumnIndex(DatabaseField.BLACKLIST_PHONE));
        mNumber.setText(phone);

        final String countryCode = c.getString(c.getColumnIndex(DatabaseField.BLACKLIST_COUNTRY_CODE));
        Log.d(TAG, "Country code: " + countryCode);
        final int position = db.getCountryPosition(countryCode);
        Log.d(TAG, "Position for country code: " + position);

        // TODO: this is a hack that might not work.
        // http://stackoverflow.com/questions/16635834/android-scrollto-does-not-work-in-onresume-method/25291102#25291102
        mCountrySpinner.post(new Runnable() {
            @Override
            public void run() {
                mCountrySpinner.setSelection(position);
            }
        });

        final String name = c.getString(c.getColumnIndex(DatabaseField.BLACKLIST_CALLER));
        mName.setText(name);

        final String note = c.getString(c.getColumnIndex(DatabaseField.BLACKLIST_NOTE));

        final int isLocal = c.getInt(c.getColumnIndex(DatabaseField.BLACKLIST_IS_LOCAL));
        Log.d(TAG, "Editing phone = " + phone + ", isLocal = " + isLocal);

        mNote.setText(note);

        final String fullNumber = Database.generateFullNumberInServerFormat(countryCode, phone);
        fetchDetailsFromRemote(fullNumber, isLocal);
    }

    private void setupWhiteListEntry() {
        final Database db = mReverdApp.getDatabase();
        final Cursor c = db.getWhiteListedNumberById(mDbId);
        c.moveToFirst();

        final String phone = c.getString(c.getColumnIndex(DatabaseField.WHITELIST_PHONE));
        mNumber.setText(phone);

        final String countryCode = c.getString(c.getColumnIndex(DatabaseField.WHITELIST_COUNTRY_CODE));
        Log.d(TAG, "Country code: " + countryCode);
        final int position = db.getCountryPosition(countryCode);
        Log.d(TAG, "Position for country code: " + position);

        // TODO: this is a hack that might not work.
        // http://stackoverflow.com/questions/16635834/android-scrollto-does-not-work-in-onresume-method/25291102#25291102
        mCountrySpinner.post(new Runnable() {
            @Override
            public void run() {
                mCountrySpinner.setSelection(position);
            }
        });

        final String name = c.getString(c.getColumnIndex(DatabaseField.WHITELIST_CALLER));
        mName.setText(name);

        final int isLocal = c.getInt(c.getColumnIndex(DatabaseField.WHITELIST_IS_LOCAL));
        Log.d(TAG, "Editing phone = " + phone + ", isLocal = " + isLocal);

        final String note = c.getString(c.getColumnIndex(DatabaseField.WHITELIST_NOTE));
        mNote.setText(note);

        final String fullNumber = Database.generateFullNumberInServerFormat(countryCode, phone);
        fetchDetailsFromRemote(fullNumber, isLocal);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mCountries.add("Country");
        Cursor cursor = mReverdApp.getDatabase().getAllCountry();
        if (cursor!=null && cursor.getCount()>0) {
            for(int i=0;i<cursor.getCount();i++){
                cursor.moveToPosition(i);
                mCountries.add(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME)));
                mCountryCodes.add(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_CODE)));
                mCallingCodes.add(cursor.getInt(cursor.getColumnIndex(DatabaseField.CALLING_CODE)));

            }
        }
        mCountryAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorBlack();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.edit_list_entry));
        }
    }
}
