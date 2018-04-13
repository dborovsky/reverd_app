package com.reverdapp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.utils.LogConfig;

public class CallCheckReceiver extends BroadcastReceiver {
    final String TAG = LogConfig.genLogTag("CallCheckReceiver");

    public CallCheckReceiver() {
    }

    void startService(final Context context)
    {
        Log.d(TAG, "Starting service.");
        Intent intent = new Intent(context, LocalCheckCallService.class);
        context.startService(intent);
    }

    static void sendRingingToService(final Context c,
                                     final String number)
    {
        final Intent intent = new Intent(LocalCheckCallService.ACTION_RINGING);
        intent.putExtra(LocalCheckCallService.PARAM_PHONENUM, number);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    static void sendOffHookToService(final Context c)
    {
        final Intent intent = new Intent(LocalCheckCallService.ACTION_OFFHOOK);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    static void sendIdleToService(final Context c) {
        final Intent intent = new Intent(LocalCheckCallService.ACTION_IDLE);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    // Info: for testing use
    // "gsm call +4522403503"
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String callState = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);

        // Check if service is running and start it if not.
        if (!LocalCheckCallService.isRunning()) {
            startService(context);
        }

        Log.d(TAG, "onReceive of CallCheckReceiver");

        if (callState.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            // Send messages to service.
            final String incomingNumber = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            Log.d( TAG, "incoming number : " + incomingNumber ); 
            sendRingingToService(context, incomingNumber);
        } else if (callState.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
            sendOffHookToService(context);
        }
        else if (callState.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            sendIdleToService(context);
        }

    }
}
