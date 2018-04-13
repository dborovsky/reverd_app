package com.reverdapp.fragment;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
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

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.adapter.CountryAdapter;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.task.AsyncAddToBlackList;
import com.reverdapp.fragment.task.AsyncSubmitComplain;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;

public class SubmitComplainFragment extends Fragment implements OnClickListener {
    private static final String TAG = LogConfig.genLogTag("SubmitComplainFragment");

    private Button btnSubmit;
    private Spinner spnrCountry;
    private CountryAdapter mCountryAdapter;

    //
    private ArrayList<String> mCountries = new ArrayList<String>();
    private ArrayList<String> mCountryCodes = new ArrayList<String>();
    private ArrayList<Integer> mCallingCodes = new ArrayList<Integer>();

    private ReverdApp reverdApp;
    private int mPosition;
    private EditText mNumber;
    private EditText mName;
    private EditText mNotes;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_submit_complain, null);

        btnSubmit = (Button) view.findViewById(R.id.fragment_submit_complaints_btn_submit);
        spnrCountry = (Spinner) view.findViewById(R.id.fragment_submit_complaints_et_contry);

        reverdApp = (ReverdApp) getActivity().getApplicationContext();
        btnSubmit.setOnClickListener(this);

        mNumber = (EditText) view.findViewById(R.id.fragment_submit_complaints_et_number);
        mName = (EditText) view.findViewById(R.id.fragment_submit_complaints_et_name);
        mNotes = (EditText) view.findViewById(R.id.fragment_submit_complaints_et_notes);


        mCountryAdapter = new CountryAdapter(getActivity(), mCountries);
        spnrCountry.setAdapter(mCountryAdapter);

        ((BaseActivity) getActivity()).setActionbarTextColorWhite();
        ((BaseActivity) getActivity()).setTitle(getString(R.string.submit_complaints_title));

        AppPreferences ap = new AppPreferences(getActivity());

        spnrCountry.setSelection(reverdApp.getDatabase().getCountryPosition(ap.get(AppPreferences.PREF_SIM_COUNTRY_CODE, "+1")));
        spnrCountry.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                SubmitComplainFragment.this.mPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        final Cursor cursor = reverdApp.getDatabase().getAllCountry();
        if (cursor != null && cursor.getCount() > 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
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
        if (!hidden) {
            ((BaseActivity) getActivity()).setActionbarTextColorWhite();
            ((BaseActivity) getActivity()).setTitle(getString(R.string.submit_complaints_title));
        }
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
            b.putString(IFragmentConnection.IFC_COUNTRY_ISO3166_1_ID, mCountryCodes.get(mPosition));
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
        alertDialogBuilder.setMessage(getString(R.string.alert_add_this_number_to_blacklist));
        alertDialogBuilder.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if (Util.isNetworkAvailable(getActivity())) {
                    final AsyncAddToBlackList blackListTask = new AsyncAddToBlackList(connection, act, CommonFragmentConnection.DO_NOTHING);
                    blackListTask.execute();
                    final AsyncSubmitComplain task = new AsyncSubmitComplain(connection, act, CommonFragmentConnection.PRESS_BACK);
                    task.execute();
                }else{
                    Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
                }
            }
        });
        alertDialogBuilder.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                final AsyncSubmitComplain task = new AsyncSubmitComplain(connection, act, CommonFragmentConnection.PRESS_BACK);
                task.execute();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fragment_submit_complaints_btn_submit) {

            if (mNumber.getText().toString().length() > 0 && mNotes.getText().toString().length() > 0) {
                displayDialog();
            } else {
                Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_fill_all_required_field), getActivity());
            }
        }
    }

}