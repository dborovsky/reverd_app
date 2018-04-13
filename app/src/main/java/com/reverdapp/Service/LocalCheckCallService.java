package com.reverdapp.Service;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.telephony.TelephonyManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.WindowManager;

import com.reverdapp.R;
import com.reverdapp.Receiver.NotificationReceiver;
import com.reverdapp.ReverdApp;
import com.reverdapp.database.Database;
import com.reverdapp.notification.CallNotifier;
import com.reverdapp.notification.ICallNotifier;
import com.reverdapp.stats.DbCallStatLogger;
import com.reverdapp.stats.ICallStatLogger;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Constants;
import com.reverdapp.utils.HandleUserSelection;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.google.i18n.phonenumbers.PhoneNumberUtil;

import java.security.InvalidParameterException;

// Service used to check for blocked/whitelisted calls.
// Uses an a local broadcast receiver.

// For testing using the emulator:
// telnet localhost 5554
//
public class LocalCheckCallService extends Service {
    private static final String TAG = LogConfig.genLogTag("LocalCheckCallService");

    // Call related actions:
    public static final String ACTION_RINGING = Util.generateAction(LocalCheckCallService.class, "RINGING");

    public static final String ACTION_OFFHOOK = Util.generateAction(LocalCheckCallService.class, "OFFHOOK");
    public static final String ACTION_IDLE = Util.generateAction(LocalCheckCallService.class, "IDLE");
    // Call related parameters:
    public static final String PARAM_PHONENUM = Util.generateParameter(LocalCheckCallService.class, "PHONENUM");

    // Ringer related actions:
    public static final String ACTION_SET_RINGERMODE = Util.generateAction(LocalCheckCallService.class, "SETRINGERMODE");
    // Ringer related parameters:
    public static final String PARAM_RINGERMODE = Util.generateParameter(LocalCheckCallService.class, "RINGERMODE");

    // Forwarded events:
    public static final String ACTION_DELETE_NOTIFICATION = NotificationReceiver.ACTION_DELETE_NOTIFICATION;
    public static final String ACTION_CLICKED_NOTIFICATION = NotificationReceiver.ACTION_CLICKED_NOTIFICATION;

    private static final String INVALID_NUMBER = "+0000000000";

    private ReverdApp mReverdApp;

    private LocalBroadcastManager mLocalBroadcastManager;
    private ActivityManager mActivityManager;
    private BroadcastReceiver mReceiver;

    private static boolean mRunning = false;
    private int ringerMode;

    public static boolean isRunning()
    {
        return mRunning;
    }

    public static boolean wasRinging = false;

    private static final String[] mInCallScreenClassNames = {
            // com.android.phone.InCallScreen
            "phone.InCallScreen"
            // Other names?
    };

    // Indicates if the ringer volume is muted.
    public boolean mRingerMuted;

    private AudioManager mAudioManager;
    private int mSavedRingerMode;
    private int mRingerMode;
    private boolean mExpectedRingerModeChange;

    private ICallStatLogger mStatLogger;

    private static PhoneNumberUtil mPhoneNumberUtil;

    static {
        mPhoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    private int mCurrentStatus;
    private String mPhoneNumber;
    private String mPhoneNumberL;

    private ICallNotifier mCallNotifier;

    public LocalCheckCallService() {
        mReverdApp = null;
        mLocalBroadcastManager = null;
        mActivityManager = null;
        mReceiver = null;
        mRingerMuted = false;
        mSavedRingerMode = -1;
        mRingerMode = -1;
        mExpectedRingerModeChange = false;
        mAudioManager = null;
        mStatLogger = null;
        mCurrentStatus = Constants.NUM_INVALID_STATUS;
        mPhoneNumber = INVALID_NUMBER;
        mCallNotifier = null;

        Log.d(TAG, "constructed");
    }

    // No one is going to bind to this service.
    // It is not possible anyway.
    @Override
    public IBinder onBind(final Intent intent) {
        Log.d(TAG, "onBind()");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        mReverdApp = (ReverdApp) getApplicationContext();
        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        final AppPreferences ap = new AppPreferences(getApplicationContext());
        mActivityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        mRunning = true;

        // We are going to watch for interesting local broadcasts.
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_RINGING);
        filter.addAction(ACTION_OFFHOOK);
        filter.addAction(ACTION_IDLE);
        filter.addAction(ACTION_SET_RINGERMODE);
        // Forwarded actions:
        filter.addAction(ACTION_DELETE_NOTIFICATION);
        filter.addAction(ACTION_CLICKED_NOTIFICATION);

        mReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                Log.d( TAG, "onReceive : action : " + action );

                if (action.equals(ACTION_RINGING)) {
                    final String phoneNumber = Iso2Phone.convertPhoneNumberToInternationalFormatNoThrow(intent.getStringExtra(PARAM_PHONENUM),mReverdApp);
                    Log.d(TAG, "action:" + ACTION_RINGING + ", num = " + phoneNumber);
                    // checking country prefix
                    mPhoneNumber = phoneNumber;
                    TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
                    final String simCountryIso = telephonyManager.getSimCountryIso();
                    final String cprefix = Iso2Phone.getPhone(simCountryIso.toUpperCase());
                    Log.d( TAG, "sim country prefix : " + cprefix );
                    mPhoneNumberL = "+" + cprefix + mPhoneNumber.substring(1);
                    Log.d( TAG, "local phone number : " + mPhoneNumberL );
                    handleIncomingCall();
                } else if (action.equals(ACTION_OFFHOOK)) {
                    Log.d(TAG, "action:" + ACTION_OFFHOOK);
                    handleOffHook();
                } else if (action.equals(ACTION_IDLE)) {
                    Log.d(TAG, "action:" + ACTION_IDLE + "wasRinging = " + wasRinging);
                    handleIdle();
                } else if (action.equals(ACTION_SET_RINGERMODE)) {
                    int newMode = intent.getIntExtra(PARAM_RINGERMODE, -1);
                    Log.d(TAG, "action:" + ACTION_SET_RINGERMODE + ", ringer mode = " + newMode);
                    handleRingerModeChange(newMode);
                } else if (action.equals(ACTION_DELETE_NOTIFICATION)) {
                    mCallNotifier.onNotificationCancel();
                } else if (action.equals(ACTION_CLICKED_NOTIFICATION)) {
                    mCallNotifier.onNotificationActivated();
                } else {
                    Log.e(TAG, "Unknown local broadcast received: " + action);
                }
            }
        };

        // The mode set by the user.
        mAudioManager = (AudioManager) this.getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        mSavedRingerMode = getSystemRingerMode();
        Log.d(TAG, "System ringer mode: " + mSavedRingerMode);
        mRingerMode = mSavedRingerMode;

        mStatLogger = new DbCallStatLogger(mReverdApp.getDatabase());

        mCallNotifier = new CallNotifier(this);

        mLocalBroadcastManager.registerReceiver(mReceiver, filter);
    }

    private void setRingerMode(final int newMode, final boolean expected)
    {
        mExpectedRingerModeChange = expected;
        Log.d(TAG, "Setting system ringer mode to " + newMode + " (" + Util.ringerModeToString(newMode) + ")" + ", expected flag = " + expected);
        mAudioManager.setRingerMode(newMode);
    }

    private void handleRingerModeChange(final int newMode) {
        if (mExpectedRingerModeChange)
        {
            Log.d(TAG, "Expected ringer mode change to " + newMode + " (" + Util.ringerModeToString(newMode) + "). Ignoring.");
            mExpectedRingerModeChange = false;
            return;
        }

        switch(newMode)
        {
            case AudioManager.RINGER_MODE_SILENT:
            case AudioManager.RINGER_MODE_VIBRATE:
            case AudioManager.RINGER_MODE_NORMAL: {
                Log.d(TAG, "Setting saved system ringer mode changed from " + mSavedRingerMode + " (" + Util.ringerModeToString(mSavedRingerMode) + ") to " + newMode + "(" + Util.ringerModeToString(newMode) +  ")");
                Log.d(TAG, "System current system ringer mode changed from " + mRingerMode + " (" + Util.ringerModeToString(mRingerMode) + ")" +  " to " + newMode + " (" + Util.ringerModeToString(newMode) + ")");
                mSavedRingerMode = newMode;
                mRingerMode = newMode;
                break;
            }
            default:
                Log.w(TAG, "Unhandled mode change: " + newMode);
                break;
        }
    }

    private void muteRinger() {
        if (!mRingerMuted)
        {
            Log.d(TAG, "Ringer muted.");
            mRingerMuted = true;
            setRingerMode(AudioManager.RINGER_MODE_SILENT, true);
        }
    }

    private void restoreRinger() {
        if (mRingerMuted)
        {
            mRingerMuted = false;
            setRingerMode(mSavedRingerMode, true);
            Log.d(TAG, "Ringer restored to saved setting.");
        }
    }


    private void handleIdle() {
        if (wasRinging)
        {
            // Call was cancelled.
            Log.d(TAG, "Call cancelled.");

            if (mCurrentStatus == Constants.NUM_NOT_PRESENT || mCurrentStatus == Constants.NUM_WHITELISTED) {
                // Logging:
                mStatLogger.onReceivedCall(mCurrentStatus, mPhoneNumber);
            }

            mPhoneNumber = INVALID_NUMBER;
        }
        wasRinging = false;
        restoreRinger();
    }

    private void handleOffHook() {
        if (wasRinging)
        {
            Log.d(TAG, "Call answered.");
            switch (mCurrentStatus)
            {
                case Constants.NUM_NOT_PRESENT:
                    mCallNotifier.onCall(mPhoneNumber);
                    break;
                case Constants.NUM_WHITELISTED:
                    mCallNotifier.onWhiteListedCall(mPhoneNumber);
                    break;
            }

            if (mCurrentStatus == Constants.NUM_NOT_PRESENT || mCurrentStatus == Constants.NUM_WHITELISTED) {
                // Logging:
                Log.d(TAG, "Call answered.");
                mStatLogger.onReceivedCall(mCurrentStatus, mPhoneNumber);
            }

            mPhoneNumber = INVALID_NUMBER;
        }
        wasRinging = false;
        restoreRinger();
    }

    private void handleIncomingCall() {
        Log.d(TAG, "Incoming call.");
        wasRinging = true;

        mCurrentStatus = checkIfNumberIsBlackOrWhite(mPhoneNumber);
        Log.d(TAG, "Number status is: " + mCurrentStatus);

        // The ringer volume is unmuted per default.

        switch (mCurrentStatus) {
            case Constants.NUM_NOT_PRESENT:
                break;
            case Constants.NUM_WHITELISTED:
                break;
            case Constants.NUM_BLACKLISTED: {
                muteRinger();
                //Disconnect the call.
                HandleUserSelection.rejectIncomingCall(this, mPhoneNumber);
                restoreRinger();
                mStatLogger.onReceivedCall(mCurrentStatus, mPhoneNumber);

                // Get statistics of blocked calls during last numberOfSeconds.
                final long numberOfSeconds = 10 * 60;
                final long blockedCallCounter = mReverdApp.getDatabase().getDetailedCallCounter(Constants.NUM_BLACKLISTED, numberOfSeconds);
                final String callStats = formatMessage(blockedCallCounter, getString(R.string.ten_minutes));

                mCallNotifier.onBlockedCall(mPhoneNumber, callStats);
                break;
            }
            case Constants.NUM_INVALID_STATUS:
                Log.d(TAG, "Invalid call received.");
                break;
        }

        mCurrentStatus = checkIfNumberIsBlackOrWhite(mPhoneNumberL);
        Log.d(TAG, "Number status is: " + mCurrentStatus);

        // The ringer volume is unmuted per default.

        switch (mCurrentStatus) {
            case Constants.NUM_NOT_PRESENT:
                break;
            case Constants.NUM_WHITELISTED:
                break;
            case Constants.NUM_BLACKLISTED: {
                muteRinger();
                //Disconnect the call.
                HandleUserSelection.rejectIncomingCall(this, mPhoneNumberL);
                restoreRinger();
                mStatLogger.onReceivedCall(mCurrentStatus, mPhoneNumberL);

                // Get statistics of blocked calls during last numberOfSeconds.
                final long numberOfSeconds = 10 * 60;
                final long blockedCallCounter = mReverdApp.getDatabase().getDetailedCallCounter(Constants.NUM_BLACKLISTED, numberOfSeconds);
                final String callStats = formatMessage(blockedCallCounter, getString(R.string.ten_minutes));

                mCallNotifier.onBlockedCall(mPhoneNumber, callStats);
                break;
            }
            case Constants.NUM_INVALID_STATUS:
                Log.d(TAG, "Invalid call received.");
                break;
        }
    }

    private String formatMessage(final long numberOfCalls, final String duration) {
        StringBuffer sb = new StringBuffer("");

        if (numberOfCalls < 0) {
            throw new InvalidParameterException("");
        }

        if (numberOfCalls > 1) {
            sb.append(numberOfCalls).
                    append(" ").
                    append(getString(R.string.blocked_calls_in_the_last)).
                    append(" ").
                    append(duration);
        }

        return sb.toString();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLocalBroadcastManager.unregisterReceiver(mReceiver);
        mReceiver = null;
        mRunning = false;

        // Unmute the ringer moveBackDays this service is terminating.
        restoreRinger();

        Log.d(TAG, "onDestroy()");
    }

    private int checkIfNumberIsBlackOrWhite(String number) {
        final Database db = mReverdApp.getDatabase();
        return db.checkIfNumIsBlackOrWhite(number);
    }

    public int getSystemRingerMode() {
        return mAudioManager.getRingerMode();
    }
}
