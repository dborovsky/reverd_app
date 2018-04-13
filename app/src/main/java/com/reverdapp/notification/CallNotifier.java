package com.reverdapp.notification;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.reverdapp.R;
import com.reverdapp.Receiver.NotificationReceiver;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.view.HomeActivity;

/**
 * Created by wojci on 8/19/15.
 */
public class CallNotifier implements ICallNotifier {

    private static final String TAG = LogConfig.genLogTag("CallNotifier");
    private static final int INVALID_ID = -1;

    private final Context mContext;
    private final NotificationManager mNotificationManager;
    private int mCurrentId;

    public CallNotifier(final Context c)
    {
        mContext = c;
        mNotificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
        mCurrentId = INVALID_ID;
    }

    @Override
    public void onBlockedCall(final String phone, final String statsText) {
        Log.d(TAG, "onBlockedCall " + phone);

        final StringBuffer sb = new StringBuffer();
        sb.append("Blocked call from ").append(phone);

        showNotification(sb.toString(), statsText);
    }

    @Override
    public void onNotificationCancel() {
        if (mCurrentId != INVALID_ID) {
            Log.d(TAG, "Cancelled notification: " + mCurrentId);
            mCurrentId++;
        }
    }

    @Override
    public void onNotificationActivated() {
        if (mCurrentId != INVALID_ID) {
            Log.d(TAG, "Activated notification: " + mCurrentId);
            mNotificationManager.cancel(mCurrentId);
            mCurrentId++;
        }
    }

    @Override
    public void onWhiteListedCall(final String phone) {
        Log.d(TAG, "onWhiteListedCall (not implemented) " + phone);
    }

    @Override
    public void onCall(final String phone) {
        Log.d(TAG, "onCall (not implemented) " + phone);
    }

    private void showNotification(final String contentText, final String subText) {
        final String title = mContext.getString(R.string.app_name);
        //final String text = "Blocked call from " + phone;

        final NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(mContext)
                //.setSmallIcon(R.drawable.ic_launcher)
                .setSmallIcon(R.drawable.blacklist25x25) //  notification_icon
                .setContentTitle(title)
                .setContentText(contentText)
                .setSubText(subText);

        final Class c = HomeActivity.class;
        final Intent resultIntent = new Intent(mContext, c);

        final TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
        stackBuilder.addParentStack(c);
        stackBuilder.addNextIntent(resultIntent);

        final Intent resultIndent = new Intent(mContext, NotificationReceiver.class);
        resultIndent.setAction(NotificationReceiver.ACTION_CLICKED_NOTIFICATION);
        final PendingIntent resultPI = PendingIntent.getBroadcast(mContext.getApplicationContext(), -1, resultIndent, 0);
        mBuilder.setContentIntent(resultPI);

        final Intent intent = new Intent(mContext, NotificationReceiver.class);
        intent.setAction(NotificationReceiver.ACTION_DELETE_NOTIFICATION);
        final PendingIntent deletePI = PendingIntent.getBroadcast(mContext.getApplicationContext(), -1, intent, 0);
        mBuilder.setDeleteIntent(deletePI);

        if (mCurrentId == INVALID_ID) {
            mCurrentId = 0;
        }
        mNotificationManager.notify(mCurrentId, mBuilder.build());
        Log.d(TAG, "Created notification with id=" + mCurrentId + ", contentText = " + contentText + ", subText = " + subText);
    }
}
