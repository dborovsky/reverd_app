package com.reverdapp.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.LoaderManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.app.AlertDialog;
import android.content.DialogInterface;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.adapter.CountryAreaAdapter;
import com.reverdapp.billing.InventoryLoadedBase;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.fragment.loader.AbstractTaskLoader;
import com.reverdapp.fragment.task.AsyncBuySubscription;
import com.reverdapp.fragment.task.IFragmentConnection;
import com.reverdapp.model.CountryAreaModel;
import com.reverdapp.model.CountryAreaModel.AreaModel;
import com.reverdapp.subscription.SubscriptionManager;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.ToastUtil;
import com.reverdapp.utils.Util;
import com.reverdapp.view.BaseActivity;
import com.reverdapp.view.HomeActivity;
import com.reverdapp.webservice.WSCheckSubscription;
import com.reverdapp.webservice.WSParameterContainer;
import com.reverdapp.webservice.WSSyncWithEmail;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.BillingRequests;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Purchase;
import org.solovyev.android.checkout.RequestListener;
import org.solovyev.android.checkout.ResponseCodes;
import org.solovyev.android.checkout.Sku;

import static org.solovyev.android.checkout.ProductTypes.SUBSCRIPTION;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SubscriptionFragment extends Fragment implements LoaderManager.LoaderCallbacks<WSCheckSubscription>, InventoryLoadedBase.IUpdateView {

    private static final String TAG = LogConfig.genLogTag("SubscriptionFragment");
    private final int TASK_ID = 1;

    private View mView = null;

    @Override
    public Loader<WSCheckSubscription> onCreateLoader(int id, Bundle args) {
        return new WSLoadingTask(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<WSCheckSubscription> loader, WSCheckSubscription result) {
        if (result == null) {
            //mSubscribed = false;
            //setupUI(mView);
            Log.d(TAG, "No result, isSubscribed = false");
            return;
        }

        Log.d(TAG, "Got result, isSubscribed = " + result.isSubscribed());

        //mSubscribed = result.isSubscribed();
        //mExpiration = result.getExpires();
        //boolean re = result.isRecurring();
        //setupUI(mView);
    }

    @Override
    public void onLoaderReset(Loader<WSCheckSubscription> loader) {
    }

    @Override
    public void update(final boolean subscribed,
                    final HashMap<String, Sku> skus,
                    final HashMap<String, Boolean> purchasedSkus)
    {
        if (subscribed) {
            setupSubscribedUI(skus, purchasedSkus);
        } else {
            setupUnsubscribedUI(skus, purchasedSkus);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private class SubscriptionFragmentConnection implements IFragmentConnection {
        private SubscriptionFragment mFragment;
        private int mTerm;
        private int mRecurring;

        public SubscriptionFragmentConnection(SubscriptionFragment f) {
            mFragment = f;
            mTerm = 0;
        }

        public void setTerm(final int term) {
            mTerm = term;
        }

        public void setRecurring(final int recurring) {
            mRecurring = recurring;
        }

        @Override
        public Bundle getAsyncTaskOptions() {
            final Bundle b = new Bundle();
            b.putInt(IFragmentConnection.IFC_IAP_TERM, mTerm);
            b.putInt(IFragmentConnection.IFC_IAP_RECOURRING, mRecurring);
            return b;
        }

        @Override
        public void handlePostExecute(final int destination) {
            mFragment.handleNavigation(destination);
        }
    }

    private void handleNavigation(int destination) {
        switch (destination) {
            case AsyncBuySubscription.NAVIGATION_SUCCESS:
                // Refresh page:
                Bundle b = new Bundle();
                getActivity().getLoaderManager().restartLoader(TASK_ID, b, this);
                break;
            case AsyncBuySubscription.NAVIGATION_ERROR:
                // Show error page.
                break;
        }
    }

    private SubscriptionFragmentConnection mFragmentConnection;

    private static class WSLoadingTask extends AbstractTaskLoader<WSCheckSubscription> {

        public WSLoadingTask (Context context) {
            super(context);
        }

        @Override
        public WSCheckSubscription loadInBackground() {
            WSParameterContainer c = WSParameterContainer.createSubscriptionCheck(getContext());
            WSCheckSubscription call = new WSCheckSubscription(getContext(), c);
            try {
              call.execute();
            } catch ( Exception e ) {
              Log.e( TAG, "Loading subscription failed", e );
            }
            return call;
        }
    }

    private ProgressBar mProgressBar;
    private LinearLayout mSubscribedLayout;
    private LinearLayout mUnsubscribedLayout;

    private class InventoryLoadedListener extends InventoryLoadedBase {

        InventoryLoadedListener(final InventoryLoadedBase.IUpdateView updateView) {
            super(updateView);
        }
    };

    private class PurchaseListener implements RequestListener<Purchase> {
        @Override
        public void onSuccess(@NonNull Purchase purchase) {
            Log.d(TAG, "onSuccess: " + purchase.token);
            // Tell the server about this purchase.
            int term = AsyncBuySubscription.INVALID_TERM;
            switch (purchase.sku) {
                case ReverdApp.ONEMONTHSUBSCRIPTION_SKUID:
                    term = AsyncBuySubscription.TERM_1;
                    break;
                case ReverdApp.SIXMONTHSSUBSCRIPTION_SKUID:
                    term = AsyncBuySubscription.TERM_6;
                    break;
                case ReverdApp.ONEYEARSUBSCRIPTION_SKUID:
                    term = AsyncBuySubscription.TERM_12;
                    break;
                default:
                    term = AsyncBuySubscription.INVALID_TERM;
                    break;
            }
            mFragmentConnection.setTerm(term);
            mFragmentConnection.setRecurring(AsyncBuySubscription.RECURRING_OFF);

            if (term != AsyncBuySubscription.INVALID_TERM) {
                Log.d(TAG, "Informing backend about purchase.");
                final AsyncBuySubscription task = new AsyncBuySubscription(getActivity(), mFragmentConnection);
                task.execute();
            }
            else {
                Log.e(TAG, "Unable to inform backend about purchase!");
            }
            onPurchased();
        }

        private void onPurchased() {
            Log.d(TAG, "onPurchased");
            mInventory.load().whenLoaded(new InventoryLoadedListener(SubscriptionFragment.this));
        }

        @Override
        public void onError(int response, @NonNull Exception e) {
            // it is possible that our data is not synchronized with data on Google Play => need to handle some errors
            if (response == ResponseCodes.ITEM_ALREADY_OWNED) {
                Log.d(TAG, "ITEM_ALREADY_OWNED");
                onPurchased();
            } else {
                Log.e(TAG, "Exception, response code: " + Integer.toString(response), e);
            }
        }
    }

    @NonNull
    protected Inventory mInventory;

    @NonNull
    protected ActivityCheckout mCheckout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
          mCheckout = ((BaseActivity) getActivity()).getCheckout();
          mInventory = mCheckout.loadInventory();
          mInventory .whenLoaded(new InventoryLoadedListener(this));

          mCheckout.createPurchaseFlow(new PurchaseListener());
        } catch ( Exception e ) {
          Log.e(TAG, "Couldn't init in-app purchase", e);
        }
    }

    @Override
    public void onDestroy() {
        mCheckout.destroyPurchaseFlow();
        super.onDestroy();
    }

    /* Purchase something. */
    private void purchase(@NonNull final Sku sku) {
        mCheckout.whenReady(new Checkout.ListenerAdapter() {
            @Override
            public void onReady(@NonNull BillingRequests requests) {
                Log.d(TAG, "Purchasing " + sku.id + ", " + sku.price);
                requests.purchase(sku, null, mCheckout.getPurchaseFlow());
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_subscription, null);

        mFragmentConnection = new SubscriptionFragmentConnection(this);

        mProgressBar = (ProgressBar)mView.findViewById(R.id.pb);
        mProgressBar.setVisibility(View.VISIBLE);

        mSubscribedLayout = (LinearLayout)mView.findViewById(R.id.subscribed_ll);
        mUnsubscribedLayout = (LinearLayout)mView.findViewById(R.id.not_subscribed_ll);

        mSubscribedLayout.setVisibility(View.GONE);
        mUnsubscribedLayout.setVisibility(View.GONE);

        //mSubscriptionTypeString = "";

        final String title = getString(R.string.subscription_title);
        ((BaseActivity)getActivity()).setTitle(title);

        final Bundle b = new Bundle();
        getActivity().getLoaderManager().initLoader(TASK_ID, b, this);

        return mView;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if(!hidden){
            ((BaseActivity)getActivity()).setActionbarTextColorWhite();
            ((BaseActivity)getActivity()).setTitle(getString(R.string.subscription_title));
        }
    }

    /*
    private void setupUI(final View v) {
        if (mSubscribed) {
            setupSubscribedUI(v);
        } else {
            setupUnsubscribedUI(v);
        }
        mProgressBar.setVisibility(View.GONE);
    }
    */

    private void setupSubscribedUI(final HashMap<String, Sku> skus,
                                   final HashMap<String, Boolean> purchasedSkus) {
        mSubscribedLayout.setVisibility(View.VISIBLE);
        mUnsubscribedLayout.setVisibility(View.GONE);

        TextView status1 = (TextView)mView.findViewById(R.id.statusText1);
        TextView status2 = (TextView)mView.findViewById(R.id.statusText2);

        final Context c = getActivity();
        final String subscribed = c.getString(R.string.you_re_subscribed_using);
        String subscriptionTypeString = InventoryLoadedBase.getSubscribedTypeAsString(getActivity(), purchasedSkus);
        status1.setText(subscribed);
        status2.setText(subscriptionTypeString);
    }

    private void setupUnsubscribedUI(final HashMap<String, Sku> skus,
                                     final HashMap<String, Boolean> purchasedSkus) {
        mUnsubscribedLayout.setVisibility(View.VISIBLE);
        mSubscribedLayout.setVisibility(View.GONE);

        OnClickListener l = new OnClickListener() {
            @Override
            public void onClick(final View v) {
              
              try {
                if (!Util.isNetworkAvailable(getActivity()))
                {
                   final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                   alertDialogBuilder.setTitle(getActivity().getString(R.string.app_name));
                   alertDialogBuilder.setCancelable(false);
                   alertDialogBuilder.setMessage(getActivity().getString(R.string.alert_no_internet));
                   alertDialogBuilder.setPositiveButton(getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                       }
                   });
                   alertDialogBuilder.create().show();
                   return;
                }

                String skuId = "";
                switch (v.getId()) {
                    case R.id.onemonthbtn:
                        skuId = ReverdApp.ONEMONTHSUBSCRIPTION_SKUID;
                        break;
                    case R.id.sixmonthsbtn:
                        skuId = ReverdApp.SIXMONTHSSUBSCRIPTION_SKUID;
                        break;
                    case R.id.oneyearbtn:
                        skuId = ReverdApp.ONEYEARSUBSCRIPTION_SKUID;
                        break;
                }

                final Sku sku = skus.get(skuId);
                Log.d(TAG, "Purchasing " + skuId );
                HomeActivity.mDoHalt = false;
                purchase(sku);

              } catch ( Exception e ) {
                final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle(getActivity().getString(R.string.app_name));
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setMessage(getActivity().getString(R.string.alert_inapp_purchase_failed));
                alertDialogBuilder.setPositiveButton(getActivity().getString(android.R.string.ok), new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int which) {
                           dialog.dismiss();
                       }
                });
                alertDialogBuilder.create().show();
              }
            }
        };

        final Button oneMonthButton = (Button)mView.findViewById(R.id.onemonthbtn);
        oneMonthButton.setOnClickListener(l);

        final Button sixMonthsButton = (Button)mView.findViewById(R.id.sixmonthsbtn);
        sixMonthsButton.setOnClickListener(l);

        final Button oneYearButton = (Button)mView.findViewById(R.id.oneyearbtn);
        oneYearButton.setOnClickListener(l);
    }
}
