package com.pongodev.dailyworkout.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonFloat;
import com.gc.materialdesign.views.ProgressBarCircularIndeterminate;
import com.gc.materialdesign.widgets.SnackBar;
import com.google.android.gms.ads.AdView;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.utils.Ads;
import com.pongodev.dailyworkout.utils.DBHelperPrograms;
import com.pongodev.dailyworkout.utils.DBHelperWorkouts;
import com.pongodev.dailyworkout.utils.Utils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by keong on 12/29/2014.
 */
public class ActivityDetail extends ActionBarActivity implements View.OnClickListener{

    private ProgressBarCircularIndeterminate prgLoading;
    private DBHelperWorkouts dbWorkouts;
    private DBHelperPrograms dbPrograms;

    private ArrayList<Object> data;

    // Declare view objects
    private ImageView imgThumbnail;
    private TextView  txtSteps;
    private LinearLayout lytContent;
    private AdView adView;
    private TextView lblNoResult, lblToolbarText, lblToolbarSubtext;

    // String to save data from activityHome
    private String mName, mSteps, mTime, mImage, mId;
    private int mSelectedDay;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        // Get Data from ActivityHome
        Intent i= getIntent();
        mId         = i.getStringExtra(Utils.EXTRA_ID);
        mName       = i.getStringExtra(Utils.EXTRA_NAME);

        // connect view objects and xml ids
        adView      = (AdView) findViewById(R.id.adView);
        lytContent  = (LinearLayout) findViewById(R.id.lytContent);
        txtSteps    = (TextView) findViewById(R.id.txtSteps);
        ButtonFloat btnStart = (ButtonFloat) findViewById(R.id.btnStart);
        ButtonFloat btnAdd = (ButtonFloat) findViewById(R.id.btnAdd);
        prgLoading  = (ProgressBarCircularIndeterminate) findViewById(R.id.prgLoading);

        imgThumbnail        = (ImageView)findViewById(R.id.imgThumbnail);
        lblToolbarText      = (TextView) findViewById(R.id.lblToolbarText);
        lblToolbarSubtext   = (TextView) findViewById(R.id.lblToolbarSubtext);
        lblNoResult         = (TextView) findViewById(R.id.lblNoResult);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        lblToolbarText.setText(mName);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnStart.setOnClickListener(this);
        btnAdd.setOnClickListener(this);

         /* CHECK_PLAY_SERV = 1 means Google Play services version on the device
	    supports the version of the client library you are using */
        if(Utils.loadPreferences(Utils.CHECK_PLAY_SERV, this)==1){

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

        // Check database
        dbWorkouts = new DBHelperWorkouts(this);
        dbWorkouts.checkDBWorkouts();

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
                lblToolbarSubtext.setText(mTime);
                txtSteps.setText(mSteps);
                int image = getResources().getIdentifier(mImage, "drawable", getPackageName());

                Picasso.with(getApplicationContext())
                        .load(image)
                        .into(imgThumbnail);
            } else {
                lblNoResult.setVisibility(View.VISIBLE);
                lytContent.setVisibility(View.GONE);
            }

        }
    }

    // Method to fetch data from database
    public void getDataFromDatabase() {
        data = dbWorkouts.getDetail(mId);

        if(!data.isEmpty()){
            mId     = data.get(0).toString();
            mName   = data.get(1).toString();
            mImage  = data.get(2).toString();
            mTime   = data.get(3).toString();
            mSteps  = data.get(4).toString();
        }

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
                boolean isAvailable = dbPrograms.isDataAvailable(mSelectedDay, Integer.valueOf(mId));

                // if data is not available add data to this day program, otherwise, show toast message
                if(!isAvailable){
                    dbPrograms.addData(Integer.valueOf(mId), mName, Integer.valueOf(mSelectedDay), mImage, mTime, mSteps);
                    new SnackBar(ActivityDetail.this,
                            getString(R.string.success_add)+" "+day_name[mSelectedDay]).show();

                }else{
                    new SnackBar(ActivityDetail.this,
                            getString(R.string.failed_add)+" "+day_name[mSelectedDay]).show();
                }
            }
        });
        // set negative button
        builder.setNegativeButton(negative, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // close update dialog if cancel button clicked
                dialog.dismiss();
            }
        });

        // show dialog
        AlertDialog alert = builder.create();
        alert.show();
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
                 i.putExtra(Utils.EXTRA_ID, mId);
                 i.putExtra(Utils.EXTRA_NAME, mName);
                 i.putExtra(Utils.EXTRA_WORKOUT_TIME, mTime);
                 startActivity(i);
                 break;

            case R.id.btnAdd:
                addDialog();
                 break;

            default:
                break;

        }

    }
}
