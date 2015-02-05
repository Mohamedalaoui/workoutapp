/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.google.android.gms.ads.AdView;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.fragments.FragmentList;
import com.pongodev.dailyworkout.utils.Utils;

import java.util.ArrayList;

public class ActivityList extends ActionBarActivity implements
        FragmentList.OnSelectedListener,
        FragmentList.OnClickListener{

    private String mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        String mName;
        String mId;
        if (getIntent().getExtras() != null) {
            Intent i=getIntent();
            mId         = i.getStringExtra(Utils.ARG_ID);
            mName       = i.getStringExtra(Utils.ARG_NAME);
            mActivity   = i.getStringExtra(Utils.ARG_PAGE);

            Utils.saveString(Utils.ARG_LIST_ID, mId, this);
            Utils.saveString(Utils.ARG_LIST_NAME, mName, this);
            Utils.saveString(Utils.ARG_LIST_PAGE, mActivity, this);
        } else {
            mId       = Utils.loadString(Utils.ARG_LIST_ID, this);
            mName     = Utils.loadString(Utils.ARG_LIST_NAME, this);
            mActivity = Utils.loadString(Utils.ARG_LIST_PAGE, this);
        }

        Bundle arguments = new Bundle();
        arguments.putString(Utils.ARG_ID, mId);
        arguments.putString(Utils.ARG_NAME, mName);
        arguments.putString(Utils.ARG_PAGE, mActivity);
        FragmentList fragment = new FragmentList();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .add(R.id.item_container, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                .commit();

        // connect view objects and xml ids
        AdView adView = (AdView) findViewById(R.id.adView);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(mName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        boolean isAdmobVisible = Utils.admobVisibility(adView, Utils.ARG_ADMOB_VISIBILITY);
        if(isAdmobVisible)
            Utils.loadAdmob(adView);

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
        detailIntent.putExtra(Utils.ARG_ID, selectedID);
        detailIntent.putExtra(Utils.ARG_NAME, selectedName);
        detailIntent.putExtra(Utils.ARG_PAGE, mActivity);
        startActivity(detailIntent.setClass(this, ActivityDetail.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);

    }

    @Override
    public void onClick(ArrayList<String> listId, ArrayList<String> listName, ArrayList<String> listTime) {
        Intent detailIntent = new Intent(this, ActivityStopWatchAll.class);
        detailIntent.putExtra(Utils.ARG_ID, listId);
        detailIntent.putExtra(Utils.ARG_NAME, listName);

        startActivity(detailIntent.setClass(this, ActivityStopWatchAll.class));
        overridePendingTransition(R.anim.open_next, R.anim.close_main);

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }
}
