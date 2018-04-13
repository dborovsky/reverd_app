package com.reverdapp.Receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.view.HomeActivity;

/**
 * Created by wojci on 8/19/15.
 */

public class NotificationReceiver extends BroadcastReceiver {

    private final String TAG = LogConfig.genLogTag("NotificationReceiver");

    public static final String ACTION_DELETE_NOTIFICATION = Util.generateAction(NotificationReceiver.class, "delete-notification");
    public static final String ACTION_CLICKED_NOTIFICATION = Util.generateAction(NotificationReceiver.class, "clicked-notification");

    void startService(final Context context)
    {
        Log.d(TAG, "Starting service.");

        final Intent intent = new Intent(context, LocalCheckCallService.class);
        context.startService(intent);
    }

    static void forwardBroadcastToLocalService(final Context c, final String action)
    {
        final Intent intent = new Intent(action);
        LocalBroadcastManager.getInstance(c).sendBroadcast(intent);
    }

    @Override
    public void onReceive(final Context context, final Intent i) {

        Log.d(TAG, "Received: " + i.getAction());

        final String action = i.getAction();

        if (action.equals(ACTION_DELETE_NOTIFICATION) || action.equals(ACTION_CLICKED_NOTIFICATION)) {
            if (!LocalCheckCallService.isRunning()) {
                startService(context);
            }
            forwardBroadcastToLocalService(context, action);

            if (action.equals(ACTION_CLICKED_NOTIFICATION)) {
                final Intent intent = new Intent(context, HomeActivity.class);
                final Bundle b = new Bundle();
                b.putInt(HomeActivity.ARG_START_FRAGMENT, R.id.ID_BLOCKEDCALLLIST);
                intent.putExtras(b);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        }
        else {
            Log.w(TAG, "Unhandled action: " + action);
        }
    }
}
