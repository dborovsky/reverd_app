package com.reverdapp.view;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.fragment.BlackListFragment;
import com.reverdapp.fragment.BlockedListFragment;
import com.reverdapp.fragment.TrashFragment;
import com.reverdapp.fragment.WhiteListFragment;
import com.reverdapp.database.Database;
import com.reverdapp.database.DatabaseField;
import com.reverdapp.utils.LogConfig;
import com.reverdapp.utils.Util;
import com.reverdapp.webservice.WebServiceUtil;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.database.Cursor;

public class HomeActivity extends BaseActivity {

    private static final String TAG = LogConfig.genLogTag("HomeActivity");
    public static final String ARG_START_FRAGMENT = Util.generateParameter(HomeActivity.class, "STARTFRAGMENT");
    private boolean mSentRequest = false;
    public static boolean mDoHalt = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Bundle b = getIntent().getExtras();
        int startFragment = R.id.ID_INVALID;

        if (b == null) {
            Log.w(TAG, "No value for " + ARG_START_FRAGMENT + " provided!");
        }
        else {
            startFragment = b.getInt(ARG_START_FRAGMENT, R.id.ID_INVALID);
            Log.d(TAG, ARG_START_FRAGMENT + ": " + startFragment);
        }

        Fragment f = null;

        switch (startFragment) {
            case R.id.ID_BLACKLIST:
                Log.d(TAG, "Showing blacklist fragment");
                f = new BlockedListFragment();
                break;
            case R.id.ID_BLOCKEDCALLLIST:
                Log.d(TAG, "Showing blocked list fragment");
                f = new BlockedListFragment();
                break;
            default:
                Log.d(TAG, "Showing default fragment");
                f = new BlackListFragment();
                break;
        }

        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.content_frame, f);
        fragmentTransaction.commit();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if ( mDoHalt )
        {
          // force the end of the app for a start fresh, needed?
          // finish();
        }
        else
        {
          mDoHalt = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // send all lost requests
        if (Util.isNetworkAvailable(this)) {
          try {
            Cursor c = ((ReverdApp) getApplicationContext()).getDatabase().getAllLostRequests();
            Log.d( TAG, "Got " + c.getCount() + " lost requests" );
            c.moveToFirst();
            while ( !c.isAfterLast() )
            {
               Log.d( TAG, "lost request for : " + c.getString(c.getColumnIndex(DatabaseField.LOSTREQUEST_URL)) );
               final int id = c.getInt(c.getColumnIndex(DatabaseField.LOSTREQUEST_ID));
               final String url = c.getString(c.getColumnIndex(DatabaseField.LOSTREQUEST_URL));
               final String postData = c.getString(c.getColumnIndex(DatabaseField.LOSTREQUEST_POSTDATA));
               final int count = c.getInt(c.getColumnIndex(DatabaseField.LOSTREQUEST_RETRYCOUNT));
               mSentRequest = false;
               Runnable r = new Runnable() {
                   @Override
                   public void run() {
                     try {
                       WebServiceUtil.postMethod( HomeActivity.this, url, postData );
                       mSentRequest = true;
                     } catch( Exception e ) {
                       Log.e( TAG, "Sending lost request failed", e );
                       ((ReverdApp) getApplicationContext()).getDatabase().updateLostRequest( id, url, postData, count+1 );
                     }
                   }
               };
               Thread t = new Thread(r);
               t.start();
               try {
                 t.join();
               } catch ( Exception e ) {
                 Log.e( TAG, "Sending thread interrupted", e );
               }
               if ( mSentRequest )
               {
                  Log.d( TAG, "lost request sent, deleting it id:" + c.getInt(c.getColumnIndex(DatabaseField.LOSTREQUEST_ID)) );
                  ((ReverdApp) getApplicationContext()).getDatabase().deleteLostRequest( id  );
                  Log.d( TAG, "lost request deleted" );
               }
               c.moveToNext();
            }
            c.close();
          } catch (Exception e) {
            Log.e( TAG, "Couldn't get lost requests", e);
          }
        }
    }

}
