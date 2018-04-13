package com.reverdapp.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.view.inputmethod.InputMethodManager;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.adapter.CountryAdapter;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncAddToBlackList;
import com.reverdapp.fragment.task.AsyncSubmitComplain;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;

public class AddToBlackListFragment extends Fragment {
    private static final String TAG = LogConfig.genLogTag("AddToBlackListFragment");

    public static final String OPTION_SETUP_FROM_ARGUMENTS = "OPTION_SETUP_FROM_ARGUMENTS";
    public static final String OPTION_FULL_NUMBER = "OPTION_FULL_NUMBER";

    private Button mAddButton;
    private Spinner mCountrySpinner;
    private CountryAdapter mCountryAdapter;
    private ReverdApp mReverdApp;
    private EditText mNumber;
    private EditText mName;
    private EditText mNotes;
    private int mPosition;
    private ArrayList<String> mCountries = new ArrayList<String>();
    private ArrayList<Integer> mCallingCodes = new ArrayList<Integer>();
    private ArrayList<String> mCountryCode = new ArrayList<String>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        final View view = inflater.inflate(R.layout.fragment_add_to_blacklist, null);
        
        mAddButton = (Button) view.findViewById(R.id.fragment_add_to_blacklist_btn_add);
        mCountrySpinner = (Spinner) view.findViewById(R.id.fragment_add_to_blacklist_et_contry);
        mNumber = (EditText) view.findViewById(R.id.fragment_add_to_blacklist_et_number);
        mName = (EditText) view.findViewById(R.id.fragment_add_to_blacklist_et_name);
        mNotes = (EditText) view.findViewById(R.id.fragment_add_to_blacklist_et_notes);
        mReverdApp = (ReverdApp) getActivity().getApplicationContext();
        
        mAddButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
              try {
                if (v.getId() == R.id.fragment_add_to_blacklist_btn_add) {
                    if ((mNumber.getText().length() > 0) && (mNotes.getText().length() > 0))
                    {
                        if ( mPosition >= mCountries.size() )
                        {
                          Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_please_select_country), getActivity());
                        }
                        else 
                        {
                          if (!checkEnteredNumber()) 
                          {
                            Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_number_already_exists), getActivity());
                          }
                          else
                          {
                            Util.hideSoftKeyboard(getActivity());
                            displayDialog();
                          }
                        }
                    } else {
                        Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_fill_all_required_field), getActivity());
                    }
                }
              } catch ( Exception e ) {
                Log.e(TAG, "Couldn't add to black-list", e);
              }
            }
        });
        
        mCountryAdapter = new CountryAdapter(getActivity(), mCountries);
        mCountrySpinner.setAdapter(mCountryAdapter);
        
        ((BaseActivity)getActivity()).setActionbarTextColorBlack();
        ((BaseActivity) getActivity()).setTitle(getString(R.string.add_toblack_list_title));

        final AppPreferences ap = new AppPreferences(getActivity());

        mCountrySpinner.setSelection(mReverdApp.getDatabase().getCountryPosition(ap.get(AppPreferences.PREF_SIM_COUNTRY_CODE, "+1")));
        mCountrySpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                AddToBlackListFragment.this.mPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }
        });

        final Bundle args = getArguments();
        if (args.getBoolean(OPTION_SETUP_FROM_ARGUMENTS, false))
        {
            Log.d(TAG, "Setting fragment with options: " + args.toString());
            setupFromArguments(args);
        }
        else {
            Log.d(TAG, "No fragment options");
        }

        return view;
    }

    // Return true if this number can be added (it is not present).
    private boolean checkEnteredNumber() {

        final String number = mNumber.getText().toString();
        final int callingCode = mCallingCodes.get(mPosition);
        final String fullNumber = Database.generateFullNumberInDbFormat(Integer.toString(callingCode), number);
        final int ret = mReverdApp.getDatabase().checkIfNumIsBlackOrWhite(fullNumber);
        if (ret == Constants.NUM_NOT_PRESENT) {
            Log.d(TAG, "Number: " + fullNumber + " can be added");
            return true;
        }
        Log.d(TAG, "Number: " + fullNumber + " cannot be added (" + ret + ")");
        return false;
    }

    // Setup this fragment with data passed to it.
    private void setupFromArguments(final Bundle b) {
        final String fullNumber = b.getString(OPTION_FULL_NUMBER);
        final String phone = Iso2Phone.stripCountryPrefix(fullNumber);
        final String prefix = Iso2Phone.getCountryPrefix(fullNumber);

        Log.d(TAG, "Full number: " + fullNumber);
        Log.d(TAG, "Phone: " + phone);
        Log.d(TAG, "Prefix: " + prefix);

        mNumber.setText(phone);
        int pos = mReverdApp.getDatabase().getCountryPosition(prefix);
        mCountrySpinner.setSelection(pos);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //mCountries.add("Country");
        Cursor cursor = mReverdApp.getDatabase().getAllCountry();

        if (cursor!=null && cursor.getCount()>0) {
            for(int i=0;i<cursor.getCount();i++) {
                cursor.moveToPosition(i);
                String country = "";
                if (mReverdApp.isDebugBuild()) {
                    final StringBuffer sb = new StringBuffer();
                    sb.append(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME)));
                    sb.append(" (");
                    sb.append(cursor.getString(cursor.getColumnIndex(DatabaseField.CALLING_CODE)));
                    sb.append(")");
                    country = sb.toString();
                }
                else {
                    country = cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME));
                }
                mCountries.add(country);

                mCallingCodes.add(cursor.getInt(cursor.getColumnIndex(DatabaseField.CALLING_CODE)));
                mCountryCode.add(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_CODE)));
            }
        }
        mCountryAdapter.notifyDataSetChanged();
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorBlack();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.add_toblack_list_title));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mNumber.getWindowToken(), 0);
    }

    private class CommonFragmentConnection implements IFragmentConnection {
        public static final int PRESS_BACK = 0;
        public static final int DO_NOTHING = 1;

        private boolean mRemoteAdd;

        public CommonFragmentConnection(boolean remoteAdd)
        {
            mRemoteAdd = remoteAdd;
        }

        @Override
        public Bundle getAsyncTaskOptions() {
            final Bundle b = new Bundle();

            b.putBoolean(IFragmentConnection.IFC_ADD_TO_COMM_BL_ID, mRemoteAdd);

            final String number = mNumber.getText().toString();
            final int callingCode = mCallingCodes.get(mPosition);
            final String fullNumber = Database.generateFullNumberInServerFormat(Integer.toString(callingCode), number);
            b.putString(IFragmentConnection.IFC_FULL_NUMBER_ID, fullNumber);

            b.putString(IFragmentConnection.IFC_NUMBER_WITHOUT_PREFIX_ID, number);
            b.putString(IFragmentConnection.IFC_COUNTRY_NAME_ID, mCountryAdapter.getItem(mPosition).toString());
            b.putString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID, mCountryCode.get(mPosition));

            b.putInt(IFragmentConnection.IFC_COUNTRY_NUM_CODE_ID, callingCode);
            b.putString(IFragmentConnection.IFC_NAME_ID, mName.getText().toString());
            b.putString(IFragmentConnection.IFC_NOTE_ID, mNotes.getText().toString());
            return b;
        }

        @Override
        public void handlePostExecute(int destination) {
            Log.d(TAG, "handlePostExecute, id=" + destination);

            switch (destination) {
                case DO_NOTHING:
                    break;
                case PRESS_BACK:
                    getActivity().onBackPressed();
                    break;
            }
        }
    }

    private void displayDialog() {
        final Activity act = getActivity();
        final CommonFragmentConnection connection = new CommonFragmentConnection(true);
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(getString(R.string.alert_report_this_number_to_the_community));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final AsyncAddToBlackList blackListTask = new AsyncAddToBlackList(connection, act, CommonFragmentConnection.DO_NOTHING);
                blackListTask.execute();
                final AsyncSubmitComplain complaintTask = new AsyncSubmitComplain(connection, act, CommonFragmentConnection.PRESS_BACK);
                complaintTask.execute();
            }
        });

        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final AsyncAddToBlackList blackListTask = new AsyncAddToBlackList(connection, act, CommonFragmentConnection.PRESS_BACK);
                blackListTask.execute();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }
}
