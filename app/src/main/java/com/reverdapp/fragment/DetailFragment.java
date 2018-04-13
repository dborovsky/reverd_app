package com.reverdapp.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.fragment.task.AsyncDetail;
import com.reverdapp.model.NumberListModel;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.ToastUtil;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HandleBack;
import com.reverdapp.webservice.WSBlacklistToWhiteList;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSWhitelistToBlacklist;

import java.util.ArrayList;

public class DetailFragment extends Fragment implements HandleBack, OnClickListener{
    private static final String TAG = LogConfig.genLogTag("DetailFragment");

    public static final String OPTION_MODEL = "OPTION_MODEL";
    public static final String OPTION_SOURCE = "OPTION_SOURCE";

    public static final int SOURCE_BLACKLIST = 1;
    public static final int SOURCE_WHITELIST = 2;

    private Button btnViewCompain;
    private Menu menu;
    private TextView tvNumber;
    private TextView tvName;
    private TextView tvNote;
    private ProgressBar progressBar;
    private ReverdApp reverdApp;

    private NumberListModel mNumberListModel;
    private int mSource;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_detail, null);
        
        btnViewCompain  = (Button) view.findViewById(R.id.fragment_detail_btn_view_complaints);
        tvNumber  = (TextView) view.findViewById(R.id.fragment_detail_tv_number);
        tvName  = (TextView) view.findViewById(R.id.fragment_detail_tv_name);
        tvNote  = (TextView) view.findViewById(R.id.fragment_detail_tv_notes);
        progressBar = (ProgressBar) view.findViewById(R.id.fragment_detail_pb);
        
        btnViewCompain.setOnClickListener(this);
        
        reverdApp = (ReverdApp) getActivity().getApplicationContext();

        ((BaseActivity)getActivity()).setActionbarTextColorWhite();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.detail_title));

        final Bundle b = getArguments();

        Log.d(TAG, "Options: " + b.toString());
        mNumberListModel = b.getParcelable(OPTION_MODEL);
        Log.d(TAG, " ID: " + mNumberListModel.getId());

        mSource = b.getInt(OPTION_SOURCE, -1);

        //data set calculation 
        if (mSource == SOURCE_BLACKLIST) {
            btnViewCompain.setVisibility(View.VISIBLE);
        } else if (mSource == SOURCE_WHITELIST) {
            btnViewCompain.setVisibility(View.GONE);
        }
        else {
            Log.e(TAG, "Unknown source " + mSource);
        }

        tvNumber.setText(mNumberListModel.getFullNumber());

        if (mNumberListModel.isLocal() == 0) {
            callDetailService();
        }
        else {
            if (mNumberListModel.getCaller() !=null)
                tvName.setText(mNumberListModel.getCaller());

            if (mNumberListModel.getNote() !=null)
                tvNote.setText(mNumberListModel.getNote());
        }

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        
        setActionbarWithEditOption();
        
    }
    
    private void setActionbarWithEditOption(){

        final Bundle b = getArguments();
        int source = b.getInt(OPTION_SOURCE);

        if (source == SOURCE_BLACKLIST) {
            menu.findItem(R.id.action_white_list).setVisible(true);
        }
        else if (source == SOURCE_WHITELIST) {
            menu.findItem(R.id.action_blacklist).setVisible(true);
        }
        else {
            Log.e(TAG, "Unknown source " + source);
        }
    }
    
    private void callDetailService(){
        
        if (Util.isNetworkAvailable(getActivity())) {
            Log.d(TAG, "callDetailService");
            AsyncDetail task = new AsyncDetail(getActivity(),
                    mNumberListModel.getFullNumber(),
                    progressBar,
                    tvName,
                    tvNote);
            task.execute();
        } else {
            Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
        }
    }

    private void callMoveNumberBlackListToWhiteList() {
        if (Util.isNetworkAvailable(getActivity())) {
            AsyncMoveBlacklistToWhitelist asyncMoveBlacklistToWhitelist = new AsyncMoveBlacklistToWhitelist();
            asyncMoveBlacklistToWhitelist.execute();
        } else {
            Util.displayDialog(getString(R.string.app_name),
                    getString(R.string.alert_no_internet),
                    getActivity());
        }
    }

    private void callMoveNumberWhiteListToBlackList(){
        if (Util.isNetworkAvailable(getActivity())) {
            AsyncMoveWhitelistToBlacklist asyncMoveWhitelistToBlacklist = new AsyncMoveWhitelistToBlacklist();
            asyncMoveWhitelistToBlacklist.execute();
        } else {
            Util.displayDialog(getString(R.string.app_name),
                    getString(R.string.alert_no_internet),
                    getActivity());
        }
    }

    private class AsyncMoveBlacklistToWhitelist extends AsyncTask<Void, Void, Void>{

        private ProgressDialog progressDialog;
        private WSBlacklistToWhiteList wsBlacklistToWhiteList;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait));
            Context context = getActivity();
            final ArrayList<NumberListModel> l = new ArrayList<NumberListModel>();
            l.add(mNumberListModel);
            WSParameterContainer c = WSParameterContainer.createMoveBLtoWL(context, l);
            wsBlacklistToWhiteList = new WSBlacklistToWhiteList(context, c);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
               wsBlacklistToWhiteList.execute();
            } catch ( Exception e ) {
               Log.e( TAG, "Move to white list failed", e );
               // store in lost request
               String parts[] = e.getMessage().split("#");
               reverdApp.getDatabase().insertLostRequest( parts[0], parts[1] ); 

               // do the operation anyway, the lost request will be sent later
               ToastUtil.showToast(getActivity(), wsBlacklistToWhiteList.getMessage());
               // Moving.
               final int id = mNumberListModel.getId();
               reverdApp.getDatabase().moveFromBlackListToWhiteList(id);
               getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
               getFragmentManager().popBackStack();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if (progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

            ToastUtil.showToast(getActivity(), wsBlacklistToWhiteList.getMessage());
            // Moving.
            final int id = mNumberListModel.getId();
            reverdApp.getDatabase().moveFromBlackListToWhiteList(id);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
            getFragmentManager().popBackStack();
        }
    }
    
    private class AsyncMoveWhitelistToBlacklist extends AsyncTask<Void, Void, Void>{

        private ProgressDialog progressDialog;
        private WSWhitelistToBlacklist wsWhitelistToBlacklist;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait));

            final ArrayList<NumberListModel> l = new ArrayList<NumberListModel>();
            l.add(mNumberListModel);
            Context context = getActivity();
            WSParameterContainer c = WSParameterContainer.createMoveWLtoBL(context, l);
            wsWhitelistToBlacklist = new WSWhitelistToBlacklist(getActivity(), c);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
               wsWhitelistToBlacklist.execute();
            } catch ( Exception e ) {
               Log.e( TAG, "Move to black list failed", e );
               // store in lost request
               String parts[] = e.getMessage().split("#");
               reverdApp.getDatabase().insertLostRequest( parts[0], parts[1] ); 

               // do the operation anyway, the lost request will be sent later
               ToastUtil.showToast(getActivity(), wsWhitelistToBlacklist.getMessage());
               if (!wsWhitelistToBlacklist.getStatus()) {
                   // Moving
                   final int id = mNumberListModel.getId();
                   reverdApp.getDatabase().moveFromWhiteListToBlackList(id);
                   getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                   getFragmentManager().popBackStack();
               }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();

            ToastUtil.showToast(getActivity(), wsWhitelistToBlacklist.getMessage());
            if (!wsWhitelistToBlacklist.getStatus()) {
                // Moving
                final int id = mNumberListModel.getId();
                reverdApp.getDatabase().moveFromWhiteListToBlackList(id);
                getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
                getFragmentManager().popBackStack();
            }
        }
        
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        
        if(item.getItemId() == R.id.action_blacklist){
            callMoveNumberWhiteListToBlackList();
        }else if(item.getItemId() == R.id.action_white_list){
            callMoveNumberBlackListToWhiteList();
        }
        
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPress() {
        getFragmentManager().popBackStack();
        //Toast.makeText(getActivity(), "DetailFragment", Toast.LENGTH_LONG).show();
    }
    
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorWhite();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.detail_title));
        }
    }

    @Override
    public void onClick(View v) {
        
        if (v.getId() == R.id.fragment_detail_btn_view_complaints) {
            
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out);
            ComplaintsFragment complaintsFragment = new ComplaintsFragment();
            final String url = getString(R.string.ReverdBlogUrl);
            complaintsFragment.setUrl(url + tvNumber.getText().toString().substring(1, tvNumber.getText().length()));
            fragmentTransaction.add(R.id.content_frame,complaintsFragment);
            fragmentTransaction.hide(this);
            fragmentTransaction.addToBackStack(ComplaintsFragment.class.getSimpleName());
            fragmentTransaction.commit();
        }
        
    }
    
}
