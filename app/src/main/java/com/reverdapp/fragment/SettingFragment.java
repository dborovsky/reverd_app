package com.reverdapp.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.adapter.CountryAreaAdapter;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.model.CountryAreaModel;
import com.reverdapp.model.CountryAreaModel.AreaModel;
import com.reverdapp.subscription.SubscriptionManager;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.ToastUtil;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSSyncWithEmail;

import java.util.ArrayList;

public class SettingFragment extends Fragment {

    private static final String TAG = LogConfig.genLogTag("SettingFragment");

    private ExpandableListView elCountryArea;
    private CountryAreaAdapter adapter;
    private ToggleButton tbCountryArea,tbBlockCall;
    private Button btnSyncWithEmail;
    private ArrayList<CountryAreaModel> countryAreaList = new ArrayList<CountryAreaModel>();
    private ReverdApp reverdApp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_setting, null);
        elCountryArea = (ExpandableListView) view.findViewById(R.id.fragment_setting_el_country_area);
        //elCountryArea.setOnChildClickListener(adapter);

        tbCountryArea = (ToggleButton) view.findViewById(R.id.fragment_settings_toggle_block_area_country);
        btnSyncWithEmail = (Button) view.findViewById(R.id.frgment_setting_bnt_sync_with_email);
        tbBlockCall = (ToggleButton) view.findViewById(R.id.fragment_settings_toggle_block_calls);
        
        reverdApp = (ReverdApp) getActivity().getApplicationContext();
        Cursor cursor = reverdApp.getDatabase().getAllCountry();

        if (reverdApp.isDebugBuild()) {
            TextView forceText = (TextView) view.findViewById(R.id.fragment_settings_debug_force_sync_text);
            forceText.setVisibility(View.VISIBLE);

            Button forceSyncButton = (Button) view.findViewById(R.id.fragment_settings_debug_force_sync);
            forceSyncButton.setVisibility(View.VISIBLE);
            forceSyncButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Forced update of BL/WL");
                    SubscriptionManager mgr = new SubscriptionManager(getActivity());
                    mgr.updateNumberLists();
                }
            });

            TextView resetText = (TextView) view.findViewById(R.id.fragment_settings_debug_reset_app_text);
            resetText.setVisibility(View.VISIBLE);

            Button ResetButton = (Button) view.findViewById(R.id.fragment_settings_debug_reset_app);
            ResetButton.setVisibility(View.VISIBLE);
            ResetButton.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                        Log.d(TAG, "Reset app settings.");

                                        final AppPreferences ap = new AppPreferences(getActivity());
                                        ap.set(AppPreferences.INITIALIZED, false);

                                        // Finishing activity.
                                        getActivity().finish();
                                    }
                            });
                    }

        final AppPreferences ap = new AppPreferences(getActivity());
        if (ap.contains(AppPreferences.PREF_SYNC_EMAIL)) {
            btnSyncWithEmail.setVisibility(View.GONE);
        }
        
        String cname = "";
        for(int i=0 ; i<cursor.getCount();i++){
          try {
            cursor.moveToPosition(i);
            cname = cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME));
            CountryAreaModel countryAreaModel = new CountryAreaModel();
            countryAreaModel.setCallingCode(cursor.getInt(cursor.getColumnIndex(DatabaseField.CALLING_CODE)));
            countryAreaModel.setCountryCode(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_CODE)));
            countryAreaModel.setCountryName(cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME)));
            countryAreaModel.setSelected(cursor.getInt(cursor.getColumnIndex(DatabaseField.COUNTRY_IS_SELECTED)));
            countryAreaModel.areaModelList = new ArrayList<CountryAreaModel.AreaModel>();
            
            Cursor cursorArea = reverdApp.getDatabase().getAllAreaOfCountry(
                                          cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_CODE)),
                                          cursor.getString(cursor.getColumnIndex(DatabaseField.COUNTRY_NAME)));
                    
           if(cursorArea!=null && cursorArea.getCount()>0){
             for(int j=0;j<cursorArea.getCount();j++){
                cursorArea.moveToPosition(j);
                AreaModel areaModel = countryAreaModel.new AreaModel();
                final int chk = cursorArea.getInt(cursorArea.getColumnIndex(DatabaseField.AREA_IS_SELECTED));
                areaModel.setChecked(chk);
                areaModel.setAreaCode(cursorArea.getString(cursorArea.getColumnIndex(DatabaseField.AREA_CODE)));
                areaModel.setAreaName(cursorArea.getString(cursorArea.getColumnIndex(DatabaseField.AREA_NAME)));
                countryAreaModel.areaModelList.add(areaModel);
                if (chk == 1) {
                    Log.d(TAG, "Loaded areaModel: " + areaModel.getAreaCode() + " " + areaModel.getAreaName() + " " + areaModel.isChecked());
                }
              }
            }
            countryAreaList.add(countryAreaModel);
            cursorArea.close();
          } catch ( Exception e ) {
            Log.e( TAG, "Couldn't get area codes for country : " + cname, e );
          }
        }
        cursor.close();

        // Indicator drawable.
        /*
        Drawable groupIndicatorDrawable = null;
        {
            Context context = getActivity().getApplicationContext();

            TypedArray expandableListViewStyle = context.getTheme().obtainStyledAttributes(new int[]{android.R.attr.expandableListViewStyle});
            //obtain attr from style
            TypedArray groupIndicator = context.getTheme().obtainStyledAttributes(expandableListViewStyle.getResourceId(0, 0), new int[]{android.R.attr.groupIndicator});
            groupIndicatorDrawable = groupIndicator.getDrawable(0);
            elCountryArea.setGroupIndicator(groupIndicatorDrawable);
            expandableListViewStyle.recycle();
            groupIndicator.recycle();
        }
        */

        //elCountryArea.setGroupIndicator(R.drawable.group_indicator);

        adapter = new CountryAreaAdapter(getActivity(),countryAreaList);
        elCountryArea.setAdapter(adapter);
        elCountryArea.setOnChildClickListener(adapter);
        elCountryArea.setOnGroupClickListener(adapter);

        btnSyncWithEmail.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(getString(R.string.app_name));
                alertDialogBuilder.setCancelable(false);
                final EditText input = new EditText(getActivity());
                alertDialogBuilder.setView(input);
                alertDialogBuilder.setMessage(getString(R.string.enter_your_email));
                alertDialogBuilder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {

                            if (Util.validateEmail(input.getText().toString())) {
                                dialog.dismiss();
                                //btnSyncWithEmail.setVisibility(View.GONE);
                                if (Util.isNetworkAvailable(getActivity())) {
                                    btnSyncWithEmail.setText(input.getText().toString());
                                    AsyncEmail asyncEmail = new AsyncEmail();
                                    asyncEmail.execute(input.getText().toString());
                                } else {
                                    Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
                                }
                            } else {
                                Toast.makeText(getActivity(), getString(R.string.alert_enter_valid_email), Toast.LENGTH_LONG).show();
                            }
                        }
                });

                alertDialogBuilder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                });

                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
            }
        });

        ((BaseActivity) getActivity()).setActionbarTextColorWhite();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.settings_title));

        boolean checked = ap.get(AppPreferences.PREF_BLOCK_COUNTRY_AREA, false);
        tbCountryArea.setChecked(checked);

        if (tbCountryArea.isChecked()){
            elCountryArea.setVisibility(View.VISIBLE);
        }else{
            elCountryArea.setVisibility(View.INVISIBLE);
        }
        
        tbCountryArea.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                ap.set(AppPreferences.PREF_BLOCK_COUNTRY_AREA, isChecked);

                if (isChecked)
                    elCountryArea.setVisibility(View.VISIBLE);
                else
                    elCountryArea.setVisibility(View.INVISIBLE);
                
            }
        });

        checked = ap.get(AppPreferences.PREF_BLOCK_WITH_NO_CALLER_ID, true);
        tbBlockCall.setChecked(checked);
        tbBlockCall.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ap.set(AppPreferences.PREF_BLOCK_WITH_NO_CALLER_ID, isChecked);
            }
        });
        
        
        return view;
    }
    
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorWhite();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.settings_title));
        }
    }

    private class AsyncEmail extends AsyncTask<String, Void, Void>{

        private ProgressDialog progressDialog;
        private WSSyncWithEmail wsSyncWithEmail;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(String... params) {
            final Context context = getActivity();
            final WSParameterContainer c = WSParameterContainer.createSync(context, params[0]);
            wsSyncWithEmail = new WSSyncWithEmail(context, c);
            try {
              wsSyncWithEmail.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Sync with email failed", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

            ToastUtil.showToast(getActivity(), wsSyncWithEmail.getMessage());

            if (!wsSyncWithEmail.getStatus()){
                
                btnSyncWithEmail.setVisibility(View.GONE);
                AppPreferences ap = new AppPreferences(SettingFragment.this.getActivity());
                ap.set(AppPreferences.PREF_SYNC_EMAIL, btnSyncWithEmail.getText().toString());
            }else{
                
                btnSyncWithEmail.setText(getString(R.string.sync_with_email));
            }
            
        }
        
    }
}
