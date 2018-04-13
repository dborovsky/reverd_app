package com.reverdapp.webservice;

import android.content.Context;

import com.reverdapp.utils.LogConfig;

public class WSWhitelistDelete extends WSDeleteBase {

	private static final String TAG = LogConfig.genLogTag("WSWhitelistDelete");

	public WSWhitelistDelete(final Context context, final WSParameterContainer c) {
		super(context, c, TAG, WSName.WL_DELETE);
	}
}
