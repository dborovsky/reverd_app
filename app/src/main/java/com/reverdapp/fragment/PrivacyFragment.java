package com.reverdapp.fragment;

import java.util.Locale;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.view.BaseActivity;

public class PrivacyFragment extends Fragment{

	private WebView wvFeedback;
	private TextView tvHelp;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View view = inflater.inflate(R.layout.fragment_complaints, null);
		wvFeedback = (WebView) view.findViewById(R.id.fragment_feedback_wv_feedback);
		wvFeedback.getSettings().setJavaScriptEnabled(true);
		wvFeedback.getSettings().setBuiltInZoomControls(true);
		wvFeedback.getSettings().setDisplayZoomControls(false);
		wvFeedback.setWebViewClient(client);
		wvFeedback.setWebChromeClient(new WebChromeClient());
		if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("cn"))
			wvFeedback.loadUrl("file:///android_asset/privacy_cn.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("zh"))
			wvFeedback.loadUrl("file:///android_asset/privacy_zh.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("de"))
			wvFeedback.loadUrl("file:///android_asset/privacy_de.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("es"))
			wvFeedback.loadUrl("file:///android_asset/privacy_es.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("fr"))
			wvFeedback.loadUrl("file:///android_asset/privacy_fr.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("pt"))
			wvFeedback.loadUrl("file:///android_asset/privacy_pt.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("ru"))
			wvFeedback.loadUrl("file:///android_asset/privacy_ru.html");
		else wvFeedback.loadUrl("file:///android_asset/privacy.html");
		
		((BaseActivity)getActivity()).setActionbarTextColorWhite();
		((BaseActivity)getActivity()).setTitle(getString(R.string.privacy_title));
		return view;
	}
	
	WebViewClient client = new MyClient();
	 class MyClient extends WebViewClient {
			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}
		}
	 
	 
	 @Override
		public void onHiddenChanged(boolean hidden) {
			super.onHiddenChanged(hidden);
			if(!hidden){
				((BaseActivity)getActivity()).setActionbarTextColorWhite();
				((BaseActivity)getActivity()).setTitle(getString(R.string.privacy_title));
			}
		}


}
