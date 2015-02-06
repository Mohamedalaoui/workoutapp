/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/

package com.pongodev.dailyworkout.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.utils.DBHelperPrograms;
import com.pongodev.dailyworkout.utils.DBHelperWorkouts;
import com.pongodev.dailyworkout.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ActivityDetail extends ActionBarActivity implements View.OnClickListener{

    private ProgressBarCircularIndeterminate prgLoading;
    private DBHelperPrograms dbPrograms;

    private ArrayList<Object> data;

    // Declare view objects
    private ImageView imgThumbnail;
    private TextView  txtSteps;
    private TextView txtSubtitle;
    private LinearLayout lytContent;
    private RelativeLayout lytNoResult;

    // String to save data from activityHome
    private String mName, mSteps, mTime, mImage, mWorkoutId, mActivity, mProgramId;
    private int mSelectedDay=0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    // Configuration in Android API 21 to set window to full screen.
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (hasFocus) {
                getWindow().getDecorView()
                        .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Intent i= getIntent().getExtras();
        if (getIntent().getExtras() != null) {
            Intent i    = getIntent();
            mWorkoutId  = i.getStringExtra(Utils.ARG_ID);
            mName       = i.getStringExtra(Utils.ARG_NAME);
            mActivity   = i.getStringExtra(Utils.ARG_PAGE);

            Utils.saveString(Utils.ARG_ID, mWorkoutId, this);
            Utils.saveString(Utils.ARG_NAME, mName, this);
            Utils.saveString(Utils.ARG_PAGE, mActivity, this);

        } else {
            mWorkoutId= Utils.loadString(Utils.ARG_ID, this);
            mName     = Utils.loadString(Utils.ARG_NAME, this);
            mActivity = Utils.loadString(Utils.ARG_PAGE, this);
        }

        // connect view objects and xml ids
        AdView adView       = (AdView) findViewById(R.id.adView);
        lytContent          = (LinearLayout) findViewById(R.id.lytContent);
        lytNoResult         = (RelativeLayout) findViewById(R.id.lytNoResult);
        txtSteps            = (TextView) findViewById(R.id.txtSteps);
        TextView txtTitle   = (TextView) findViewById(R.id.txtTitle);
        txtSubtitle         = (TextView) findViewById(R.id.txtSubTitle);
        ButtonFloat btnStart= (ButtonFloat) findViewById(R.id.btnStart);
        ButtonFloat btnAdd  = (ButtonFloat) findViewById(R.id.btnAdd);
        prgLoading          = (ProgressBarCircularIndeterminate) findViewById(R.id.prgLoading);
        imgThumbnail        = (ImageView)findViewById(R.id.imgThumbnail);

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        txtTitle.setText(mName);

        if(mActivity.equals(Utils.ARG_WORKOUT)){
            btnAdd.setIconDrawable(getResources().getDrawable(R.drawable.ic_add_36dp));
        } else {
            btnAdd.setIconDrawable(getResources().getDrawable(R.drawable.ic_clear_36dp));
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnStart.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

        // Check ad visibility. If visible, display ad banner and interstitial.
        InterstitialAd interstitialAd = new InterstitialAd(this);
        boolean isAdmobVisible = Utils.admobVisibility(adView, Utils.ARG_ADMOB_VISIBILITY);
        if(isAdmobVisible) {
            Utils.loadAdmob(adView);

            // When interstitialTrigger equals ARG_TRIGGER_VALUE, display interstitial ad.
            int interstitialTrigger = Utils.loadPreferences(Utils.ARG_TRIGGER, this);
            if(interstitialTrigger == Utils.ARG_TRIGGER_VALUE) {
                Utils.loadAdmobInterstitial(interstitialAd, this);
                Utils.savePreferences(Utils.ARG_TRIGGER, 0, this);
            }else{
                Utils.savePreferences(Utils.ARG_TRIGGER, (interstitialTrigger+1), this);
            }
        }

        // call asynctask class to get data from database
        new getDataList().execute();

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.menuShare:
                        createShareIntent();
                        return true;
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

    // asynctask class that is used to fetch data from database in background
    private class getDataList extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // show progress dialog when fetching data from database
            super.onPreExecute();
            prgLoading.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            getDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            // when finishing fetching data, close progress dialog and show data on listview
            // if available, otherwise show label no result
            super.onPostExecute(aVoid);
            prgLoading.setVisibility(View.GONE);


            if(!data.isEmpty()){
                txtSubtitle.setText(mTime);
                txtSteps.setText(mSteps);
                //int image = getResources().getIdentifier(mImage, "drawable", getPackageName());
                int image = getResources().getIdentifier("ic_dummy_image", "drawable", getPackageName());
                Picasso.with(getApplicationContext())
                        .load(image)
                        .into(imgThumbnail);
            } else {
                lytNoResult.setVisibility(View.VISIBLE);
                lytContent.setVisibility(View.GONE);
            }

        }
    }

    // asynctask class that is used to fetch data from database in background
    private class discardData extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            // show progress dialog when fetching data from database
            super.onPreExecute();
            prgLoading.setVisibility(View.VISIBLE);

        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            discardDataFromDatabase();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // TODO Auto-generated method stub
            // when finishing fetching data, close progress dialog and show data on listview
            // if available, otherwise show label no result
            super.onPostExecute(aVoid);
            prgLoading.setVisibility(View.GONE);
            new SnackBar(ActivityDetail.this,
                    getString(R.string.success_discard)).show();

        }
    }

    // Method to fetch data from database
    public void getDataFromDatabase() {

        if(mActivity.equals(Utils.ARG_PROGRAM)){
            mProgramId = mWorkoutId;
            // Check Program database
            DBHelperPrograms dbPrograms = new DBHelperPrograms(this);
            dbPrograms.checkDBPrograms();
            mWorkoutId = dbPrograms.getWorkoutId(mWorkoutId);
            dbPrograms.close();
        }

        // Check Workout database
        DBHelperWorkouts dbWorkouts = new DBHelperWorkouts(this);
        dbWorkouts.checkDBWorkouts();
        data = dbWorkouts.getDetail(mWorkoutId);

        if(!data.isEmpty()){
            mWorkoutId     = data.get(0).toString();
            mName   = data.get(1).toString();
            mImage  = data.get(2).toString();
            mTime   = data.get(3).toString();
            mSteps  = data.get(4).toString();
        }
        dbWorkouts.close();

    }

    // Method to fetch data from database
    public void discardDataFromDatabase() {
        // Check database
        dbPrograms = new DBHelperPrograms(getApplicationContext());
        dbPrograms.checkDBPrograms();
        dbPrograms.deleteData(mProgramId);
        dbPrograms.close();
    }

    // method to create add dialog
    private void addDialog(){
        String title = getString(R.string.pick_day);
        String[] day_name = getResources().getStringArray(R.array.day_name);
        String positive = getString(R.string.add);
        String negative = getString(R.string.cancel);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the dialog title
        builder.setTitle(title);

        // specify the list array
        builder.setSingleChoiceItems(day_name, 0, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int position) {
                // TODO Auto-generated method stub
                // get selected day
                mSelectedDay = position;
            }
        });

        // set positive button
        builder.setPositiveButton(positive, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {

                // Check database
                dbPrograms = new DBHelperPrograms(getApplicationContext());
                dbPrograms.checkDBPrograms();

                String[] day_name = getResources().getStringArray(R.array.day_name);

                // check if data already available in this day program
                boolean isAvailable = dbPrograms.isDataAvailable(mSelectedDay, Integer.valueOf(mWorkoutId));

                // if data is not available add data to this day program, otherwise, show toast message
                if(!isAvailable){
                    dbPrograms.addData(Integer.valueOf(mWorkoutId), mName, mSelectedDay, mImage, mTime, mSteps);
                    new SnackBar(ActivityDetail.this,
                            getString(R.string.success_add)+" "+day_name[mSelectedDay]).show();

                }else{
                    new SnackBar(ActivityDetail.this,
                            getString(R.string.failed_add)+" "+day_name[mSelectedDay]).show();
                }
                dbPrograms.close();

                // Setting mSelectedDay to default
                mSelectedDay=0;
            }
        });
        // set negative button
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Setting mSelectedDay to default
                mSelectedDay=0;

                // close update dialog if cancel button clicked
                dialog.dismiss();
            }
        });

        // show dialog
        AlertDialog alert = builder.create();
        alert.show();

        // Setting mSelectedDay to default
        mSelectedDay=0;
    }

    private Intent createShareIntent() {
        String intro = getResources().getString(R.string.intro_message);
        String extra = getResources().getString(R.string.extra_message);
        String gPlayURL = getResources().getString(R.string.google_play_url);
        String appName = getResources().getString(R.string.app_name);
        String here = getResources().getString(R.string.here);
        String message = intro+" "+mName+extra+" "+appName+" "+here+" "+gPlayURL;
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, message);
        startActivity(Intent.createChooser(i, getResources().getString(R.string.share_to)));
        return i;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.open_main, R.anim.close_next);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnStart:
                Intent i = new Intent(getApplicationContext(), ActivityStopWatch.class);
                i.putExtra(Utils.ARG_ID, mWorkoutId);
                i.putExtra(Utils.ARG_NAME, mName);
                i.putExtra(Utils.ARG_TIME, mTime);
                startActivity(i);

                 break;

            case R.id.btnAdd:
                if(mActivity.equals(Utils.ARG_WORKOUT)){
                    addDialog();
                } else {
                    new MaterialDialog.Builder(ActivityDetail.this)
                            .content(R.string.dialog_content_discard)
                            .positiveText(R.string.dialog_button_positif_discard)
                            .negativeText(R.string.dialog_button_negatif)
                            .positiveColorRes(R.color.color_primary)
                            .negativeColorRes(R.color.color_primary)
                            .contentColorRes(R.color.text_sub_title)
                            .backgroundColorRes(R.color.background_content_list)
                            .callback(new MaterialDialog.ButtonCallback() {
                                @Override
                                public void onPositive(MaterialDialog dialog) {
                                    new discardData().execute();
                                }

                            })
                            .show();
                }

                 break;

            default:
                break;

        }

    }
}
