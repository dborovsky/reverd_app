package com.reverdapp.webservice;

import android.content.Context;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WSBlacklistDelete extends WSDeleteBase {

	private static final String TAG = LogConfig.genLogTag("WSBlacklistDelete");

	public WSBlacklistDelete(final Context context, final WSParameterContainer c) {
		super(context, c, TAG, WSName.BL_DELETE);
	}
}
