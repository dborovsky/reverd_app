package com.reverdapp;

import android.app.Application;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.provider.Settings.Secure;
import android.util.Log;

import com.reverdapp.billing.Helper;
import com.reverdapp.billing.LocalPurchaseVerifier;
import com.reverdapp.database.Database;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.webservice.cache.DetailsCache;
import com.reverdapp.webservice.cache.ICache;

import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Cache;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Products;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.solovyev.android.checkout.RobotmediaDatabase;
import org.solovyev.android.checkout.RobotmediaInventory;

import java.util.concurrent.Executor;

import static java.util.Arrays.asList;
//import static org.solovyev.android.checkout.ProductTypes.IN_APP;
import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

public class ReverdApp extends Application {

    private static final String TAG = LogConfig.genLogTag("ReverdApp");

    // public SharedPreferences sharedPreferences;
    private String mPhoneId;
    private Database mDatabase;

    // TODO: this can be removed.
    public static boolean isListening=false;
    public static boolean blackListUpdated=false;
    public static boolean whiteListUpdated=false;
    public static String mAndroidId;

    private ICache mDetailsCache;

    //private final String mTestProductSku = "android.test.purchased";
    //private ArrayList<String> mSkus = new ArrayList<String>();

    @NonNull
    private static ReverdApp mInstance;

    public ReverdApp() {
        mInstance = this;
    }

    @NonNull
    public static ReverdApp get() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(TAG, "Application created.");

        if (isDebugBuild()) {
            Log.d(TAG, "Setting strict mode!");

            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        mDatabase = new Database(this);
        try {
            mDatabase.createDataBase();
            mDatabase.openDataBase();
        } catch (Exception e) {

            Log.e(TAG, "database init", e);
        }

        mDetailsCache = new DetailsCache();

        mAndroidId = Secure.getString(getContentResolver(),Secure.ANDROID_ID);

        // Used for testing.
        // mSkus.add(mTestProductSku);

        mCheckout.start();
    }

    // For testing.
    public void setDatabase(Database database) {
        mDatabase = database;
    }

    public Database getDatabase() {
        return mDatabase;
    }
    
    public String getPhoneId() {
        return mPhoneId;
    }

    public void setPhoneId(String id) {
        this.mPhoneId = id;
    }
    
    @Override
    public void onTerminate() {
        super.onTerminate();
        if (mDatabase != null)
            mDatabase.closeDatabase();
    }

    public boolean isDebugBuild() {
        return BuildConfig.DEBUG;
    }

    public ICache getDetailsCache() {
        return mDetailsCache;
    }

    @NonNull
    public Billing getBilling() {
        return mBilling;
    }

    @NonNull
    public String getPublicKey() {
        //Log.d(TAG, "Encrypted key: " + Helper.encryptKey(AppPreferences.AppBillingKey));
        //return AppPreferences.AppBillingKey;

        final String key = Helper.decryptKey(AppPreferences.AppBillingKey);
        // Log.d(TAG, "Decrypted key: " + key);

        return key;
    }

    @NonNull
    private final Billing mBilling = new Billing(this, new Billing.Configuration() {

        @NonNull
        @Override
        public String getPublicKey() {
            return ReverdApp.this.getPublicKey();
        }

        @Nullable
        @Override
        public Cache getCache() {
            return Billing.newCache();
        }

        @Override
        public PurchaseVerifier getPurchaseVerifier() {
            //return new DefaultPurchaseVerifier();
            return new LocalPurchaseVerifier(ReverdApp.this.getPublicKey());
        }

        @Override
        public Inventory getFallbackInventory(Checkout checkout, Executor executor) {
            Log.e(TAG, "getFallbackInventory called");
            if (RobotmediaDatabase.exists(mBilling.getContext())) {
                return new RobotmediaInventory(checkout, executor);
            } else {
                return null;
            }
        }

        @Override
        public boolean isAutoConnect() {
            return false;
        }
    });

    public static final String ONEMONTHSUBSCRIPTION_SKUID = "1_month_subscription";
    public static final String SIXMONTHSSUBSCRIPTION_SKUID = "six_months_subscription";
    public static final String ONEYEARSUBSCRIPTION_SKUID = "1_year_subscription";

    @NonNull
    private static final Products mProducts = Products.create().add(SUBSCRIPTION, asList(ONEMONTHSUBSCRIPTION_SKUID, SIXMONTHSSUBSCRIPTION_SKUID, ONEYEARSUBSCRIPTION_SKUID));

    @NonNull
    private final Checkout mCheckout = Checkout.forApplication(mBilling, mProducts);

    @NonNull
    public Checkout getCheckout() {
        return mCheckout;
    }
}
