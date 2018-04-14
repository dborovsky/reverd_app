package com.reverdapp.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.billing.InventoryLoadedBase;
import com.reverdapp.subscription.SubscriptionManager;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.webservice.WSCountryAreaList;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSRegistration;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Sku;
import org.solovyev.android.checkout.RobotmediaDatabase;
import org.solovyev.android.checkout.RobotmediaInventory;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.microsoft.appcenter.AppCenter;
import com.microsoft.appcenter.analytics.Analytics;
import com.microsoft.appcenter.crashes.Crashes;

public class SplashActivity extends Activity implements InventoryLoadedBase.IUpdateView {

    private static final String TAG = LogConfig.genLogTag("SplashActivity");
    private SubscriptionManager mSubscriptionManager = null;
    private ActivityCheckout mCheckout;
    private boolean mGotInventory=false;
    private Timer mTimer = null;

    @NonNull
    protected Inventory mInventory;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    void PopupPermissions()
    {
        if (Util.needPermissionForBlocking(getApplicationContext()))
        {
            Log.d(TAG, "Spawning UI to let user enable the security settings this apps needs.");
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);

            // Tell the user to enable the permissions that this app needs.
            Toast.makeText(getApplicationContext(), R.string.permission_toast_msg, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTimer = new Timer(true);

        final AppPreferences ap = new AppPreferences(SplashActivity.this);
        Log.d( TAG, "app is initialized : " + ap.get(AppPreferences.INITIALIZED, false) );
        if (!ap.get(AppPreferences.HELPSHOWN, false)) {
           // showing the slide activity
           Log.d( TAG, "showing slides" );
           Intent intent=new Intent(SplashActivity.this, SlideActivity.class);  
           startActivityForResult(intent, AppPreferences.ACTION_SHOW_SLIDES);
        } else {
           initAppAndHome();
        }

        AppCenter.start(getApplication(),
                "7bcb9917-05fb-4cc0-9eab-12086a04b4f9",
                Analytics.class, Crashes.class);

    }

    class StartAnyway extends TimerTask {
     public void run() {
       if ( !mGotInventory )
       {
          Log.d(TAG, "Subscribed = false" );
          mSubscriptionManager.setState(false);
          new SplashTask().execute();
       }
     }
    };

    protected void initAppAndHome()
    {
        setContentView(R.layout.activity_splash);
        {
            Intent intent = new Intent(getApplicationContext(), LocalCheckCallService.class);
            getApplicationContext().startService(intent);
        }

        mSubscriptionManager = new SubscriptionManager(this);

        try {
          mCheckout = Checkout.forActivity(this, ((ReverdApp) getApplication()).getCheckout());
          mCheckout.start();

          mInventory = mCheckout.loadInventory();
          mInventory.whenLoaded(new InventoryLoadedListener(this));

          // sometimes, previous call so not returned so arm a time-out here ( 30 s )
          if ( mTimer != null ) mTimer.schedule(new StartAnyway(), 30000 );

        } catch ( Exception e ) {
          Log.e( TAG, "Couldn't get inventory, starting anyway..." , e );
          if ( mTimer != null ) mTimer.schedule(new StartAnyway(), 0 );
        }
    }

    public String getVersionString() {
        String v = "";
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // Huh? Really?
        }
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult " + requestCode + ", " + resultCode);
        if ( requestCode == AppPreferences.ACTION_SHOW_SLIDES )
        {
           Log.d( TAG, "initializeing app after showing the slides..." );
           final AppPreferences ap = new AppPreferences(SplashActivity.this);
           ap.set(AppPreferences.HELPSHOWN, true);
           initAppAndHome();
        } else {
           Log.d( TAG, "Checkout initialized." );
           mCheckout.onActivityResult(requestCode, resultCode, data);
        } 
    }

    @Override
    public void update(final boolean subscribed,
                       final HashMap<String, Sku> skus,
                       final HashMap<String, Boolean> purchasedSkus) {
          mGotInventory = true;
          Log.d(TAG, "Subscribed = " + subscribed);
          mSubscriptionManager.setState(subscribed);
          new SplashTask().execute();
    }

    // It does not make sense to run this app (the UI) without network access.
    public void showNoNetworDialog(final Context context) {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        alertDialogBuilder.setTitle(getString(R.string.app_name));
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage(getString(R.string.alert_no_internet));
        alertDialogBuilder.setPositiveButton(context.getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialogBuilder.create().show();
    }

    private class SplashTask extends AsyncTask<Void, Void, Void> {

        private WSRegistration wsRegistration;
        private WSCountryAreaList wsCountryAreaList;

        public SplashTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            
            final ReverdApp reverdApp = (ReverdApp) getApplicationContext();
            TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            final String strGetSimCountryIso = telephonyManager.getSimCountryIso();

            // Log version numbers:
            final String versionStr = getVersionString();
            Log.d(TAG, "Starting version " + versionStr);

            final String uniquePhoneId = Iso2Phone.getIMEI(SplashActivity.this);
            Log.d(TAG, "Client identification: " + uniquePhoneId);
            final AppPreferences ap = new AppPreferences(SplashActivity.this);
            ap.set(AppPreferences.PREF_SIM_COUNTRY_CODE, Iso2Phone.getPhone(strGetSimCountryIso.toUpperCase()).replace("+", ""));
            reverdApp.setPhoneId(uniquePhoneId);

            if (!ap.get(AppPreferences.INITIALIZED, false)) {
                if (onFirstRun(uniquePhoneId)) {
                    ap.set(AppPreferences.INITIALIZED, true);
                }
            }
            else {
                mSubscriptionManager.updateNumberLists();
                mSubscriptionManager.cleanUp();
            }

            return null;
        }

        // First time this app is run this code is executed.
        private boolean onFirstRun(final String uniquePhoneId) {
            Log.d(TAG, "onFirstRun");

            // Update the BL and WL lists.
            if (mSubscriptionManager.initialUpdate())
            {
                mSubscriptionManager.writeInitialSettings();
            } else {
                Log.e(TAG, "initialUpdate failed");
            }

            final Context context = SplashActivity.this;
            if (!Util.isNetworkAvailable(context)) {
                Log.w(TAG, "No network, aborting onFirstRun");
                runOnUiThread(new Runnable() {
                   @Override
                   public void run() {
                     // Util.displayDialog(getString(R.string.app_name), getString(R.string.alert_no_internet), context);
                   }
                });
                return false;
            }

            WSParameterContainer c = WSParameterContainer.createRegistration(context);
            wsRegistration = new WSRegistration(context, c);
            try {
                Log.d(TAG, "Registrating...");
                wsRegistration.execute();
            } catch ( Exception e ) {
                Log.e(TAG, "Registration failed", e);
                return false;
            }

            c = WSParameterContainer.createAreaList(context);
            wsCountryAreaList = new WSCountryAreaList(context, c);
            try {
                Log.d(TAG, "Getting area list");
                wsCountryAreaList.execute();
            } catch ( Exception e ) {
                Log.e(TAG, "Area list failed", e);
                return false;
            }

            //final AppPreferences ap = new AppPreferences(SplashActivity.this);
            //long stamp = Util.getTimestamp();
            //ap.set(AppPreferences.PREF_LAST_SYNC_DATE, Long.toString(stamp));

            Log.d(TAG, "onFirstRun finished");
            return true;

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            
            //if(wsRegistration!=null)
            //Toast.makeText(SplashActivity.this, wsRegistration.getMessage(), Toast.LENGTH_LONG).show();

            Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.right_in, R.anim.left_out);
            Log.d(TAG, "Finished splash..");

            /*
            if (Build.VERSION.SDK_INT >= 21) {
                PopupPermissions();
            }
            */
            finish();
        }
        
    }

    private class InventoryLoadedListener extends InventoryLoadedBase {
        public InventoryLoadedListener(final IUpdateView uv) {
            super(uv);
        }
    }

    /*
    private String convertInputStreamToString(HttpResponse response) {
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            final StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            final String LineSeparator = System.getProperty("line.separator");

            while ((line = bufferedReader.readLine()) != null) {
                stringBuffer.append(line + LineSeparator);
            }
            return stringBuffer.toString();
        } catch (IllegalStateException e) {
            Log.e(TAG, "illegal state", e);
        } catch (IOException e) {
            Log.e(TAG, "I/O", e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.e(TAG, "I/O", e);
                }
            }
        }
        return "";
    }
    */
}
