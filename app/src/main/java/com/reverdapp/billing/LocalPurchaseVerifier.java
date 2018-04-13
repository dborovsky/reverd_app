package com.reverdapp.billing;

import android.support.annotation.NonNull;
import android.util.Log;

import com.reverdapp.utils.LogConfig;

import org.solovyev.android.checkout.BasePurchaseVerifier;
import org.solovyev.android.checkout.Billing;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.PurchaseVerifier;
import org.solovyev.android.checkout.RequestListener;

import java.util.ArrayList;
import java.util.List;

import static android.text.TextUtils.isEmpty;

/**
 * Created by wojci on 9/10/15.
 */
public class LocalPurchaseVerifier extends BasePurchaseVerifier {

    private static final String TAG = LogConfig.genClassLogTag(LocalPurchaseVerifier.class);

    final String mPublicKey;

    public LocalPurchaseVerifier(final String publicKey) {
        mPublicKey = publicKey;
        // Log.d(TAG, "Created with public key: " + mPublicKey);
    }

    @Override
    protected void doVerify(List<Purchase> purchases, RequestListener<List<Purchase>> requestListener) {
        final List<Purchase> verifiedPurchases = new ArrayList<Purchase>(purchases.size());
        for (Purchase purchase : purchases) {
            if (Security.verifyPurchase(purchase.sku, mPublicKey, purchase.data, purchase.signature)) {
                Log.d(TAG, "Purchase of " + purchase.sku + "verified.");
                verifiedPurchases.add(purchase);
            } else {
                if (isEmpty(purchase.signature)) {
                    Log.e(TAG, "Cannot verify purchase: " + purchase + ". Signature is empty");
                } else {
                    Log.e(TAG, "Cannot verify purchase: " + purchase + ". Wrong signature");
                }
            }
        }
        requestListener.onSuccess(verifiedPurchases);
        Log.d(TAG, "Verification completed.");
    }
}
