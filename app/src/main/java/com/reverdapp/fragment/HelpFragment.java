package com.reverdapp.fragment;

import java.util.Locale;

import com.reverdapp.R;
import com.reverdapp.fragment.ComplaintsFragment.MyClient;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HomeActivity;
import com.reverdapp.view.SlideActivity;
import com.reverdapp.utils.AppPreferences;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.content.Intent;

public class HelpFragment extends Fragment {

	private WebView wvFeedback;
	private TextView tvHelp;
        private Menu menu;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
                setHasOptionsMenu(true);

		View view = inflater.inflate(R.layout.fragment_complaints, null);
		wvFeedback = (WebView) view.findViewById(R.id.fragment_feedback_wv_feedback);
		wvFeedback.getSettings().setJavaScriptEnabled(true);
		wvFeedback.getSettings().setBuiltInZoomControls(true);
		wvFeedback.getSettings().setDisplayZoomControls(false);
		wvFeedback.setWebViewClient(client);
		wvFeedback.setWebChromeClient(new WebChromeClient());
		if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("cn"))
			wvFeedback.loadUrl("file:///android_asset/help_cn.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("zh"))
			wvFeedback.loadUrl("file:///android_asset/help_zh.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("de"))
			wvFeedback.loadUrl("file:///android_asset/help_de.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("es"))
			wvFeedback.loadUrl("file:///android_asset/help_es.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("fr"))
			wvFeedback.loadUrl("file:///android_asset/help_fr.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("pt"))
			wvFeedback.loadUrl("file:///android_asset/help_pt.html");
		else if(Locale.getDefault().getLanguage().toString().equalsIgnoreCase("ru"))
			wvFeedback.loadUrl("file:///android_asset/help_ru.html");
		else wvFeedback.loadUrl("file:///android_asset/help.html");
		
		
		((BaseActivity)getActivity()).setActionbarTextColorWhite();
		((BaseActivity)getActivity()).setTitle(getString(R.string.help_title));
		return view;
	}

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
           super.onCreateOptionsMenu(menu, inflater);

           inflater.inflate(R.menu.main, menu);
           this.menu = menu;
           setActionbarWithPlayOption();
        }

        private void setActionbarWithPlayOption(){
           menu.findItem(R.id.action_play_slides).setVisible(true);
        }

        private void setActionbarWithoutPlayOption(){
          menu.findItem(R.id.action_play_slides).setVisible(false);
       }


        @Override
        public boolean onOptionsItemSelected(MenuItem item) {

           if (item.getItemId() == R.id.action_play_slides) {
              // showing the slide activity
              HomeActivity.mDoHalt = false;
              Intent intent=new Intent(getActivity(),SlideActivity.class);
              startActivityForResult(intent, AppPreferences.ACTION_SHOW_SLIDES);
           }

           return super.onOptionsItemSelected(item);
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
				((BaseActivity)getActivity()).setTitle(getString(R.string.help_title));
			}
		}
	 

}
