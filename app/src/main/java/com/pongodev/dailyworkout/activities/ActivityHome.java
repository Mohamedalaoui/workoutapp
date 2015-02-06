/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;

import com.astuetz.PagerSlidingTabStrip;
import com.google.android.gms.ads.AdView;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.fragments.FragmentSectionsPagerAdapter;
import com.pongodev.dailyworkout.fragments.FragmentTabPrograms;
import com.pongodev.dailyworkout.fragments.FragmentTabWorkouts;
import com.pongodev.dailyworkout.utils.Utils;


public class ActivityHome extends ActionBarActivity implements
        FragmentTabWorkouts.OnSelectedListener,
        FragmentTabPrograms.OnSelectedListener{

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
        /*
      The {@link android.support.v4.view.PagerAdapter} that will provide
      fragments for each of the sections. We use a
      {@link FragmentPagerAdapter} derivative, which will keep every
      loaded fragment in memory. If this becomes too memory intensive, it
      may be best to switch to a
      {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
        FragmentSectionsPagerAdapter adapter = new FragmentSectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        /*
      The {@link ViewPager} that will host the section contents.
     */
        ViewPager mViewPager = (ViewPager) findViewById(R.id.pager);

        mViewPager.setAdapter(adapter);
        mViewPager.setCurrentItem(Utils.loadPreferences(Utils.ARG_TAB_POSITION, this));

        // Bind the tabs to the ViewPager
        PagerSlidingTabStrip tabs = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        tabs.setViewPager(mViewPager);

        final int pageMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources()
                .getDisplayMetrics());
        mViewPager.setPageMargin(pageMargin);

        // Connect view objects and xml ids
        AdView adView = (AdView) findViewById(R.id.adView);

        boolean isAdmobVisible = Utils.admobVisibility(adView, Utils.ARG_ADMOB_VISIBILITY);
        if(isAdmobVisible)
            Utils.loadAdmob(adView);

        toolbar.setLogo(getResources().getDrawable(R.drawable.ic_logo));

        // Handle item menu in toolbar.
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuAbout:
                        // Open about screen.
                        Intent aboutIntent = new Intent(getApplicationContext(), ActivityAbout.class);
                        startActivity(aboutIntent);
                        overridePendingTransition(R.anim.open_next, R.anim.close_main);
                        return true;
                    default:
                        return true;
                }
            }
        });

    }

    // Method from FragmentTabsWorkouts to open workout list page
    @Override
    public void onSelected(String selectedID, String workoutName) {
        Intent detailIntent = new Intent(this, ActivityList.class);
        detailIntent.putExtra(Utils.ARG_ID, selectedID);
        detailIntent.putExtra(Utils.ARG_NAME, workoutName);
        detailIntent.putExtra(Utils.ARG_PAGE, Utils.ARG_WORKOUT);
        startActivity(detailIntent.setClass(this, ActivityList.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);
    }

    // Method from FragmentTabsPrograms to open programs list page
    @Override
    public void onSelectedDay(String selectedID, String dayName) {
        Intent detailIntent = new Intent(this, ActivityList.class);
        detailIntent.putExtra(Utils.ARG_ID, selectedID);
        detailIntent.putExtra(Utils.ARG_NAME, dayName);
        detailIntent.putExtra(Utils.ARG_PAGE, Utils.ARG_PROGRAM);
        startActivity(detailIntent.setClass(this, ActivityList.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        Utils.savePreferences(Utils.ARG_TAB_POSITION, 0, this);

    }

    /** Called before the activity is stoped */
    @Override
    public void onStop() {
        super.onStop();
        Utils.savePreferences(Utils.ARG_TAB_POSITION, 0, this);

    }

}
