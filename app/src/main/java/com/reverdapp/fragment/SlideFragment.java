package com.reverdapp.fragment;

import android.app.Fragment;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.text.Html;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;

public class SlideFragment extends Fragment {

    private static final String TAG = LogConfig.genLogTag("SlideFragment");

    private ReverdApp mReverdApp;

    private TextView mText;

    private int mLayout;
    private int mMessageId;

    public SlideFragment() {};

    @Override
    public void onResume()
    {
        super.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = this.getArguments();
        mLayout = bundle.getInt("layout", 0);
        mMessageId = bundle.getInt("textid", 0);
        final View view = inflater.inflate(mLayout, null);
        mText = (TextView)view.findViewById(R.id.slide_text);
        mText.setText(Html.fromHtml(getActivity().getString(mMessageId)));

        return view;
    }
}
