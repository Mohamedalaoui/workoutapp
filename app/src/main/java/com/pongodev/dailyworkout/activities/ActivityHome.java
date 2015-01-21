package com.pongodev.dailyworkout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.astuetz.PagerSlidingTabStrip;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.fragments.FragmentSectionsPagerAdapter;
import com.pongodev.dailyworkout.fragments.FragmentTabPrograms;
import com.pongodev.dailyworkout.fragments.FragmentTabWorkouts;
import com.pongodev.dailyworkout.utils.Ads;
import com.pongodev.dailyworkout.utils.Utils;


public class ActivityHome extends ActionBarActivity implements
        FragmentTabWorkouts.OnSelectedListener,
        FragmentTabPrograms.OnSelectedListener{

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    FragmentSectionsPagerAdapter adapter;

    // Use in checkPlayServices
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Show the Up button in the action bar.
        // Set up the action bar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        adapter = new FragmentSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(adapter);

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        mViewPager.setPageMargin(pageMargin);

        // Connect view objects and xml ids
        AdView adView = (AdView) findViewById(R.id.adView);

        /****
         * Check device for Play Services APK. If check succeeds, can use AddView
         * ***/
        if(Utils.isNetworkAvailable(this)){
            if (checkPlayServices()) {

                // Condition for admob (0=gone, 1=visible)
                if(Utils.paramAdmob==true){
                    // Check Connection
                    if(Utils.isNetworkAvailable(this)){
                        boolean isAdmobVisible = Ads.admobVisibility(adView, Utils.ARG_ADMOB_VISIBILITY);
                        if(isAdmobVisible)
                            Ads.loadAdmob(adView);
                    }
                }

            } else {
                Log.i("checkPlayServices", "No valid Google Play Services APK found.");
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i("checkPlayServices", "This device is not supported.");
                finish();
            }
            return false;
        }

	    /* CHECK_PLAY_SERV = 1 means Google Play services version on the device
	    supports the version of the client library you are using */
        Utils.savePreferences(Utils.CHECK_PLAY_SERV,1,this);
        return true;
    }

    // Method from FragmentTabsWorkouts to open workout list page
    @Override
    public void onSelected(String selectedID, String workoutName) {
        Intent detailIntent = new Intent(this, ActivityList.class);
        detailIntent.putExtra(Utils.EXTRA_ID, selectedID);
        detailIntent.putExtra(Utils.EXTRA_NAME, workoutName);
        detailIntent.putExtra(Utils.EXTRA_ACTIVITY, Utils.ACTIVITY_WORKOUT);
        startActivity(detailIntent.setClass(this, ActivityList.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);
    }

    // Method from FragmentTabsPrograms to open workout list page
    @Override
    public void onSelectedDay(String selectedID, String workoutName) {
        Intent detailIntent = new Intent(this, ActivityList.class);
        detailIntent.putExtra(Utils.EXTRA_ID, selectedID);
        detailIntent.putExtra(Utils.EXTRA_NAME, workoutName);
        detailIntent.putExtra(Utils.EXTRA_ACTIVITY, Utils.ACTIVITY_PROGRAM);
        startActivity(detailIntent.setClass(this, ActivityList.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);
    }

}
