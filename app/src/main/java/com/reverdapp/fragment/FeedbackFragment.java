package com.reverdapp.fragment;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.view.inputmethod.InputMethodManager;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.Util;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.webservice.WSFeedback;
import com.reverdapp.webservice.WSParameterContainer;

public class FeedbackFragment extends Fragment implements OnClickListener{

    private static final String TAG = LogConfig.genLogTag("FeedbackFragment");

    //private WebView wvFeedback;
    private EditText etComment;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        
        View view = inflater.inflate(R.layout.fragment_feedback, null);
        etComment = (EditText) view.findViewById(R.id.fragment_feedback_et_my_comment);
        Button btnSend = (Button) view.findViewById(R.id.fragment_feedback_btn_send);
//        wvFeedback = (WebView) view.findViewById(R.id.fragment_feedback_wv_feedback);
//        wvFeedback.getSettings().setJavaScriptEnabled(true);
//        wvFeedback.setWebViewClient(client);
//        wvFeedback.setWebChromeClient(new WebChromeClient());
//        wvFeedback.loadUrl("https://www.google.co.in/");
        
        ((BaseActivity)getActivity()).setActionbarTextColorWhite();
        ((BaseActivity)getActivity()).setTitle(getString(R.string.feedback_title));
        
        
        btnSend.setOnClickListener(this);
        
        return view;
    }

    
    WebViewClient client = new MyClient();
    class MyClient extends WebViewClient {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                    //String ht = "javascript:window.droid.print(document.getElementsByTagName('html')[0].innerHTML);";
                    //webView.loadUrl(ht);
            
            }
    }
     
    private void callFeedback(){
        if(Util.isNetworkAvailable(getActivity())){
            AsyncFeedback asyncFeedback = new AsyncFeedback();
            asyncFeedback.execute();
        }else{
            Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), getActivity());
        }
    }
    private class AsyncFeedback extends AsyncTask<Void, Void, Void>{

        private ProgressDialog progressDialog;
        private WSFeedback wsFeedback;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(getActivity(), null, getString(R.string.please_wait));
        }

        @Override
        protected Void doInBackground(Void... params) {
            Context context = getActivity();
            //ReverdApp reverdApp = (ReverdApp) context.getApplicationContext();
            WSParameterContainer c = WSParameterContainer.createFeedback(context, etComment.getText().toString());
            wsFeedback = new WSFeedback(context, c);
            try {
              wsFeedback.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Feedback failed", e ); 
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            if(progressDialog!=null && progressDialog.isShowing())
                progressDialog.dismiss();
            
            etComment.setText("");
            
            if(wsFeedback.getMessage()!=null)
                Util.displayDialog(getString(R.string.app_name), wsFeedback.getMessage(), getActivity());
        }
        
    }
     
    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorWhite();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.feedback_title));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {

        if(v.getId() == R.id.fragment_feedback_btn_send){
            
            if(etComment.getText().toString().length()>0)
                callFeedback();
            else
                Util.displayDialog(getString(R.string.app_name), "Please enter comment", getActivity());
        }
    }
}
