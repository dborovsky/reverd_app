package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.reverdapp.R;
import com.reverdapp.view.BaseActivity;

public class ComplaintsFragment extends Fragment {

    private Menu menu;
    private WebView wvFeedback;
    private ProgressDialog progressDialog;
    private String mUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        setHasOptionsMenu(true);
        View view = inflater.inflate(R.layout.fragment_complaints, null);
        
        progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait));
        progressDialog.setCancelable(true);
        wvFeedback = (WebView) view.findViewById(R.id.fragment_feedback_wv_feedback);
        wvFeedback.getSettings().setJavaScriptEnabled(true);
        wvFeedback.getSettings().setBuiltInZoomControls(true);
        wvFeedback.getSettings().setDisplayZoomControls(false);
        wvFeedback.getSettings().setLoadWithOverviewMode(true);
        wvFeedback.getSettings().setUseWideViewPort(true);
        wvFeedback.setWebViewClient(client);
        wvFeedback.setWebChromeClient(new WebChromeClient());
                mUrl = getActivity().getString(R.string.ReverdBlogUrl);
        wvFeedback.loadUrl(mUrl);

        ((BaseActivity)getActivity()).setActionbarTextColorWhite();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.complaints_title));
        
        return view;
    }
    
    public void setUrl(String url){
        this.mUrl = url;
    }

    WebViewClient client = new MyClient();
     class MyClient extends WebViewClient {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (progressDialog!=null && progressDialog.isShowing())
                    progressDialog.dismiss();
                
            }
        }
     
    
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        
        inflater.inflate(R.menu.main, menu);
        this.menu = menu;
        setActionbarWithEditOption();
    }
    
    private void setActionbarWithEditOption(){
        menu.findItem(R.id.action_plus).setVisible(true);
    }

    private void setActionbarWithoutEditOption(){
        menu.findItem(R.id.action_plus).setVisible(false);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_plus){
            
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out);
            fragmentTransaction.add(R.id.content_frame, new SubmitComplainFragment());
            fragmentTransaction.hide(this);
            fragmentTransaction.addToBackStack(SubmitComplainFragment.class.getSimpleName());
            fragmentTransaction.commit();
        }
        return super.onOptionsItemSelected(item);
    }
    
     @Override
        public void onHiddenChanged(boolean hidden) {
            super.onHiddenChanged(hidden);
            if(!hidden){
                ((BaseActivity)getActivity()).setActionbarTextColorWhite();
                ((BaseActivity)getActivity()).setTitle(getString(R.string.complaints_title));
            }
     }
}
