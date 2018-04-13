package com.reverdapp.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;
import android.widget.FrameLayout;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.Service.LocalCheckCallService;
import com.reverdapp.billing.InventoryLoadedBase;
import com.reverdapp.subscription.SubscriptionManager;
import com.reverdapp.utils.AppPreferences;
import com.reverdapp.utils.Iso2Phone;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.fragment.SlideFragment;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;
import org.solovyev.android.checkout.Inventory;
import org.solovyev.android.checkout.Sku;

import java.util.HashMap;

public class SlideActivity extends Activity {

    private static final String TAG = LogConfig.genLogTag("SlideActivity");
    private int backpress = 0;
    private int mCurrentSlide = -1;
    private FrameLayout mContent;
    private Handler mHandler = null;

    private int[] layouts = new int[] {
      R.layout.slide_01,
      R.layout.slide_02,
      R.layout.slide_03,
      R.layout.slide_04,
      R.layout.slide_05,
      R.layout.slide_06,
      R.layout.slide_07,
      R.layout.slide_08,
      R.layout.slide_09a,
      R.layout.slide_09b,
      R.layout.slide_10
    };

    private int[] textids = new int[] {
      R.string.anim_0,
      R.string.anim_1,
      R.string.anim_2,
      R.string.anim_3,
      R.string.anim_4,
      R.string.anim_5,
      R.string.anim_6,
      R.string.anim_7,
      R.string.anim_8a,
      R.string.anim_8b,
      R.string.anim_9
    };

    private int[] exposures = new int[] {
      0,
      3000,
      2000,
      3000,
      4000,
      3000,
      3000,
      3000,
      2000,
      4000,
      4000,
      6000
    };

    @Override
    protected void onDestroy() {
        if ( mHandler != null )
        {
           mHandler.removeCallbacks( showNextSlide );
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
      super.onPause();
      finish();
    }

    private Runnable showNextSlide = new Runnable() {
      @Override
      public void run() {

        Log.d( TAG, "Showing slide : " + mCurrentSlide );
        try {
          if ( mCurrentSlide >= layouts.length )
          {
             Log.d( TAG, "Ending activity : " + mCurrentSlide );
             setResult( RESULT_OK );
             finish();
             return;
          }

          final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
          fragmentTransaction.setCustomAnimations(R.animator.right_in, R.animator.left_out, R.animator.left_in, R.animator.right_out);
          Fragment newFragment = null;
          newFragment = new SlideFragment();
          Bundle bundle = new Bundle();
          bundle.putInt("layout", layouts[mCurrentSlide]);
          bundle.putInt("textid", textids[mCurrentSlide]);
          newFragment.setArguments(bundle);
          fragmentTransaction.replace(R.id.content_frame, newFragment);
          fragmentTransaction.addToBackStack(SlideFragment.class.getSimpleName());
          fragmentTransaction.commit();

          if ( mCurrentSlide < layouts.length )
          {
             mCurrentSlide = mCurrentSlide + 1;
             mHandler.postDelayed( this, exposures[mCurrentSlide] );
          }

         } catch (Exception e) {
          Log.e( TAG, "Couldn't show slide : " + mCurrentSlide, e );
         } 
      }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide);
        mContent = (FrameLayout) findViewById( R.id.content_frame );

        mCurrentSlide = 0;
        if ( mHandler == null )
        {
           mHandler = new Handler();
           mHandler.postDelayed(showNextSlide, 0);
        }
    }

    @Override
    public void onBackPressed(){
        backpress = (backpress + 1);
        if ( backpress > 1 )
        {
           setResult( RESULT_OK );
           finish();
        }
    }
}
