package com.reverdapp.view;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.reverdapp.R;
import com.reverdapp.ReverdApp;
import com.reverdapp.utils.LogConfig;

import org.solovyev.android.checkout.ActivityCheckout;
import org.solovyev.android.checkout.Checkout;

public class BaseActivity extends Activity {

    private static final String TAG = LogConfig.genLogTag("BaseActivity");

    ActionBar actionBar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;

    private ActivityCheckout mCheckout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayShowTitleEnabled(true);
        
        ((ImageView)findViewById(android.R.id.home)).setPadding(10, 0, 0, 0);
        
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open, /* "open drawer" description for accessibility */
                R.string.drawer_close /* "close drawer" description for accessibility */
                ) {
                    public void onDrawerClosed(View view) {
                        // getSupportActionBar().setTitle(mTitle);
                        // invalidateOptionsMenu(); // creates call to
                        // onPrepareOptionsMenu()
                    }

                    public void onDrawerOpened(View drawerView) {
                        // getSupportActionBar().setTitle(mDrawerTitle);
                        // invalidateOptionsMenu(); // creates call to
                        // onPrepareOptionsMenu()
                    }
                };
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        mCheckout = Checkout.forActivity(this, ((ReverdApp) getApplication()).getCheckout());
        mCheckout.start();
    }
    
    public void setActionbarTextColorBlack(){
        
        int actionBarTitleId = getResources().getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.BLACK);
            }
        }
        
    }
    public void setActionbarTextColorWhite(){
        
        int actionBarTitleId = getResources().getSystem().getIdentifier("action_bar_title", "id", "android");
        if (actionBarTitleId > 0) {
            TextView title = (TextView) findViewById(actionBarTitleId);
            if (title != null) {
                title.setTextColor(Color.WHITE);
            }
        }
        
    }

    public void closeDrawer() {
        if (mDrawerLayout.isDrawerOpen(getFragmentManager().findFragmentById(R.id.left_drawer).getView()))
            mDrawerLayout.closeDrawers();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // MenuInflater inflater = getMenuInflater();
        // inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle action buttons
        switch (item.getItemId()) {
        // case R.id.action_websearch:
        // // create intent to perform web search for this planet
        // Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
        // intent.putExtra(SearchManager.QUERY,
        // getSupportActionBar().getTitle());
        // // catch event that there's no activity to handle intent
        // if (intent.resolveActivity(getPackageManager()) != null) {
        // startActivity(intent);
        // } else {
        // Toast.makeText(this, R.string.app_not_available,
        // Toast.LENGTH_LONG).show();
        // }
        // return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        mCheckout.stop();
        super.onDestroy();
    }

    @NonNull
    public ActivityCheckout getCheckout() {
        return mCheckout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mCheckout.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult " + requestCode + ", " + resultCode);
    }
}
