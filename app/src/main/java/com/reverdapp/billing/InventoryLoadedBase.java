package com.reverdapp.billing;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;

import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.Sku;
import android.provider.Settings.Secure;

import java.util.HashMap;
import java.util.Map;

import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

/**
 * Created by wojci on 9/24/15.
 */
public abstract class InventoryLoadedBase implements Inventory.Listener {
    private static final String TAG = LogConfig.genClassLogTag(InventoryLoadedBase.class);

    private final IUpdateView mUpdateView;

    private HashMap<String, Sku> mSkus = new HashMap<String, Sku>();
    private HashMap<String, Boolean> mPurchasedSkus = new HashMap<String, Boolean>();

    protected boolean mSubscribed;

    public static String getSubscribedTypeAsString(final Context c, final HashMap<String, Boolean> purchasedSkus) {

        for (Map.Entry<String, Boolean> e: purchasedSkus.entrySet()) {
            // Log.d(TAG, "ID: " + e.getKey() + ", purchased: " + e.getValue());

            if (e.getValue()) {
                final String key = e.getKey();
                switch (key) {
                    case ReverdApp.ONEMONTHSUBSCRIPTION_SKUID:
                        return c.getString(R.string.monthly_subscription);
                    case ReverdApp.SIXMONTHSSUBSCRIPTION_SKUID:
                        return c.getString(R.string.halfyear_subscription);
                    case ReverdApp.ONEYEARSUBSCRIPTION_SKUID:
                        return c.getString(R.string.yearly_subscription);
                }
                break;
            }
        }

        return c.getString(R.string.unknown_subscription);
    }

    public interface IUpdateView {
        void update(final boolean subscribed,
                    final HashMap<String, Sku> skus,
                    final HashMap<String, Boolean> purchasedSkus);
    }

    public InventoryLoadedBase(final IUpdateView uv) {
        mUpdateView = uv;
        mSubscribed = false;
    }

    //protected abstract void setupUI(final View v);

    public boolean isSubscribed() {
        return mSubscribed;
    }

    @Override
    public void onLoaded(@NonNull Inventory.Products products) {
        Log.d(TAG, "onLoaded " + products.toString());

        final Inventory.Product product = products.get(SUBSCRIPTION);

        mSkus.clear();
        mPurchasedSkus.clear();

        if (product.supported) {
            Log.e(TAG, "Billing supported!");

            for (Sku sku : product.getSkus()) {
                Log.d(TAG, "Checking SKU: " + sku.id);

                mSkus.put(sku.id, sku);
                boolean skuPurchased = false;
                final Purchase purchase = product.getPurchaseInState(sku, Purchase.State.PURCHASED);
                if (purchase != null) {
                    Log.d(TAG, "Purchase: " + purchase.toString());
                    if (purchase.token != null) {
                        skuPurchased = true;
                    }
                }

                mPurchasedSkus.put(sku.id, skuPurchased);
            }
        } else {
            Log.e(TAG, "Billing not supported!");
        }

        setupInternals();
        mUpdateView.update(mSubscribed, mSkus, mPurchasedSkus);
        //setupUI(mView);
    }

    private void setupInternals() {
        // List purchased SKUs.

        for (Map.Entry<String, Boolean> e: mPurchasedSkus.entrySet()) {
            Log.d(TAG, "ID: " + e.getKey() + ", purchased: " + e.getValue());

            // enable testing on my device ( chevil )
            if ( ReverdApp.mAndroidId.equals( "e5c7e3076a115032" ) ) {
                continue;
            }

            if (e.getValue()) {
                mSubscribed = true;
                //final String key = e.getKey();
                //subscribed(key);
                break;
            }
        }

        /*
        if (!mSubscribed) {
            notSubscribed();
        }
        */

    }

    //protected abstract void subscribed(final String key);
    //protected abstract void notSubscribed();
}

