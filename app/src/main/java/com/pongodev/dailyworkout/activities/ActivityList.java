package com.pongodev.dailyworkout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.ads.AdView;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.fragments.FragmentList;
import com.pongodev.dailyworkout.utils.Ads;
import com.pongodev.dailyworkout.utils.Utils;

import java.util.ArrayList;

/**
 * Created by keong on 12/29/2014.
 */
public class ActivityList extends ActionBarActivity implements
        FragmentList.OnSelectedListener,
        FragmentList.OnClickListener{

    private String mActivity, mName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        if (savedInstanceState == null) {
            // Get Data from ActivityHome
            Intent i = getIntent();
            String mSelectedId = i.getStringExtra(Utils.EXTRA_ID);
            mName              = i.getStringExtra(Utils.EXTRA_NAME);
            mActivity          = i.getStringExtra(Utils.EXTRA_ACTIVITY);

            Bundle arguments = new Bundle();
            arguments.putString(Utils.EXTRA_ID, mSelectedId);
            arguments.putString(Utils.EXTRA_NAME, mName);
            arguments.putString(Utils.EXTRA_ACTIVITY, mActivity);
            FragmentList fragment = new FragmentList();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.item_container, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }

        // connect view objects and xml ids
        AdView adView = (AdView) findViewById(R.id.adView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

         /* CHECK_PLAY_SERV = 1 means Google Play services version on the device
	    supports the version of the client library you are using */
        if(Utils.loadPreferences(Utils.CHECK_PLAY_SERV,this)==1){

            // Check the connection
            if(Utils.isNetworkAvailable(this)){
                // Condition for admob (0=gone, 1=visible)
                if(Utils.paramAdmob==true){

                    adView.setVisibility(View.VISIBLE);
                    Ads.loadAds(adView);
                }
            } else {
                Toast.makeText(this, getString(R.string.internet_alert), Toast.LENGTH_SHORT).show();
            }
        }

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case android.R.id.home:
                        finish();
                        overridePendingTransition(R.anim.open_main, R.anim.close_next);
                        return true;
                    default:
                        return true;
                }

            }
        });

    }

    @Override
    public void onSelected(String selectedID, String selectedName) {
        Intent detailIntent = new Intent(this, ActivityDetail.class);
        detailIntent.putExtra(Utils.EXTRA_ID, selectedID);
        detailIntent.putExtra(Utils.EXTRA_NAME, selectedName);
        detailIntent.putExtra(Utils.EXTRA_ACTIVITY, mActivity);
        startActivity(detailIntent.setClass(this, ActivityDetail.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);

    }

    @Override
    public void onClick(ArrayList<String> listId, ArrayList<String> listName, ArrayList<String> listTime) {
        Intent detailIntent = new Intent(this, ActivityStopWatchAll.class);
        detailIntent.putExtra(Utils.EXTRA_ID, listId);
        detailIntent.putExtra(Utils.EXTRA_NAME, listName);
        detailIntent.putExtra(Utils.EXTRA_TIME, listTime);

        startActivity(detailIntent.setClass(this, ActivityStopWatchAll.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}
