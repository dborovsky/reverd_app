package com.reverdapp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;

/**
 * Created by wojci on 4/14/15.
 */
public class RingerReceiver extends BroadcastReceiver {
    private final String TAG = LogConfig.genLogTag("RingerReceiver");

    void startService(final Context context)
    {
        Log.d(TAG, "Starting service.");

        final Intent intent = new Intent(context, LocalCheckCallService.class);
        context.startService(intent);
    }

    static void sendRingerModeToService(final Context c,
                                        final int mode)
    {
        final Intent intent = new Intent(LocalCheckCallService.ACTION_SET_RINGERMODE);
        intent.putExtra(LocalCheckCallService.PARAM_RINGERMODE, mode);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }


    @Override
    public void onReceive(final Context context, final Intent i) {

        if (i.getAction().equals(AudioManager.RINGER_MODE_CHANGED_ACTION)) {
            final Bundle b = i.getExtras();

            final int ringerMode = b.getInt(AudioManager.EXTRA_RINGER_MODE);

            Log.d(TAG, "RingerReceiver, ringerMode = " + ringerMode + " (" + Util.ringerModeToString(ringerMode) + ")" );

            // Check if service is running and start it if not.
            if (!LocalCheckCallService.isRunning()) {
                startService(context);
            }

            sendRingerModeToService(context, ringerMode);
        }


    }
}
