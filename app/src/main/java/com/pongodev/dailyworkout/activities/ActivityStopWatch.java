/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.gc.materialdesign.views.ButtonFlat;
import com.gc.materialdesign.views.ButtonFloat;
import com.pongodev.dailyworkout.R;
import com.pongodev.dailyworkout.utils.DBHelperWorkouts;
import com.pongodev.dailyworkout.utils.Utils;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ActivityStopWatch extends ActionBarActivity implements View.OnClickListener{

    // create object of views
    private TextView txtTimer;
    private TextSwitcher txtTitle;
    private ButtonFlat btnReset;
    private ButtonFloat btnStart, btnSound;
    private ViewFlipper flipper;
    private AudioManager am;

    // create object of WakeLock class
    private PowerManager.WakeLock wl;
    private Context ctx;
    private CounterClass timer = null;

    // set variables as FLAGs
    private boolean FLAG = false;
    private boolean paramPause = false;

    private String mName;
    private String mTime;
    private ArrayList<String> Images = new ArrayList<>();
    private String currentTime ="00:00";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        ctx = this;

        // get values that passed from previous page
        Intent i    = getIntent();
        mName       = i.getStringExtra(Utils.ARG_NAME);
        mTime       = i.getStringExtra(Utils.ARG_TIME);

        // connect views object and views id on xml
        txtTitle = (TextSwitcher) findViewById(R.id.txtTitle);
        txtTimer = (TextView) findViewById(R.id.txtTimer);
        btnStart = (ButtonFloat) findViewById(R.id.btnStart);
        btnSound = (ButtonFloat) findViewById(R.id.btnSound);
        btnReset = (ButtonFlat) findViewById(R.id.btnReset);
        flipper  = (ViewFlipper) findViewById(R.id.flipper);

        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnSound.setOnClickListener(this);

        // Setting button reset disable in the beginning
        btnReset.setEnabled(false);
        btnReset.setTextColor(getResources().getColor(R.color.btnflat_disable));

        txtTimer.setText(mTime);

        // Customization Title text
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        txtTitle.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(ActivityStopWatch.this);
                myText.setGravity(Gravity.CENTER_HORIZONTAL);
                myText.setTypeface(null, Typeface.BOLD);
                myText.setTextSize(getResources().getDimension(R.dimen.text_title_stopwatch));
                myText.setTextColor(getResources().getColor(R.color.color_primary));
                return myText;
            }
        });

        // Setting default volume
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Utils.ARG_DEFAULT_VOLUME, 0);

        // get PowerManager to keep screen on
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SCREEN ON");

        // Setting default timer and button start icon
        timer = new CounterClass(3000,1000);
        btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play_36dp));

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Condition for sound (1 = on, 0 = off)
        if(Utils.loadPreferences(Utils.ARG_SOUND, this)==Utils.ARG_SOUND_OFF){
            btnSound.setIconDrawable(getResources().getDrawable(R.drawable.ic_volume_off_36dp));
        } else {
            btnSound.setIconDrawable(getResources().getDrawable(R.drawable.ic_volume_up_36dp));
        }

        //set the animation of the slideshow
        flipper.setInAnimation (AnimationUtils.loadAnimation(this,
                R.anim.fade_in_out));
        flipper.setFlipInterval(2000);
        flipper.startFlipping ();

        // call synctask to get images
        new getDataList().execute();

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

    // asynctask class to fetch data from database in background
    private class getDataList extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub

            // show progress dialog when fetching data from database
            progress= ProgressDialog.show(
                    ActivityStopWatch.this,
                    "",
                    getString(R.string.loading_data),
                    true);
        }

        @Override
        protected Void doInBackground(Void... params) {
            // TODO Auto-generated method stub
            getDataFromDatabase();

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            // TODO Auto-generated method stub

            // when finishing fetching data close progress dialog and show data to views
            progress.dismiss();

            txtTitle.setText(mName);

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = screenWidth / 2 + 50;

            for(int i=0;i<Images.size();i++){
                FrameLayout fl = new FrameLayout(ActivityStopWatch.this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);

                fl.setLayoutParams(lp);

                ImageView imgWorkout = new ImageView(ActivityStopWatch.this);
                imgWorkout.setScaleType(ImageView.ScaleType.CENTER_CROP);
                int imagedata = getResources().getIdentifier(Images.get(i), "drawable", getPackageName());
                imgWorkout.setImageResource(imagedata);
                fl.addView(imgWorkout, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                flipper.addView(fl);

            }
        }
    }

    // method to get data from database
    private void getDataFromDatabase(){
        DBHelperWorkouts dbWorkouts = new DBHelperWorkouts(getApplicationContext());
        dbWorkouts.checkDBWorkouts();

        // Clear array
        Images.clear();

        // store data to arraylist variable
        //for(int i=0;i< data.size();i++){
          //  ArrayList<Object> row = data.get(i);

            //Images.add(row.get(0).toString());
        //}
        Images.add("ic_dummy_image");
        Images.add("ic_dummy_image_2");
        dbWorkouts.close();
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStart:
                // TODO Auto-generated method stub
                // Condition when button start push
                if(!FLAG){
                    if(!flipper.isFlipping()) flipper.startFlipping();
                    wl.acquire();
                    btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_pause_36dp));
                    FLAG = true;
                    if(paramPause){
                        timer.cancel();
                        startTimer(currentTime);
                        btnReset.setEnabled(false);
                        btnReset.setTextColor(getResources().getColor(R.color.btnflat_disable));

                    } else {
                        startTimer(mTime);
                        btnReset.setEnabled(false);
                        btnReset.setTextColor(getResources().getColor(R.color.btnflat_disable));
                    }

                // Condition when button pause push
                } else {
                    if(flipper.isFlipping()) flipper.stopFlipping();
                    wl.release();
                    FLAG = false;
                    paramPause = true;
                    btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play_36dp));
                    btnReset.setEnabled(true);
                    btnReset.setTextColor(getResources().getColor(R.color.btnflat_enable));
                    timer.cancel();
                    currentTime = timer.timerPause();
                    txtTimer.setText(currentTime);

                }
                break;

            case R.id.btnReset:
                // TODO Auto-generated method stub
                paramPause = false;
                timer.cancel();
                txtTimer.setText(mTime);

                break;

            case R.id.btnSound:
                // TODO Auto-generated method stub
                // Condition for sound (1 = on, 0 = off)
                if(Utils.loadPreferences(Utils.ARG_SOUND, this)==Utils.ARG_SOUND_OFF){
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 7, 0);
                    btnSound.setIconDrawable(getResources().getDrawable(R.drawable.ic_volume_up_36dp));
                    Utils.savePreferences(Utils.ARG_SOUND, Utils.ARG_SOUND_ON, this);

                } else {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                    btnSound.setIconDrawable(getResources().getDrawable(R.drawable.ic_volume_off_36dp));
                    Utils.savePreferences(Utils.ARG_SOUND, Utils.ARG_SOUND_OFF, this);
                }

                break;


            default:
                break;
        }
    }

    private void startTimer(String time){
        String[] splitTime = time.split(":");

        int splitMinute = Integer.valueOf(splitTime[0]);
        int splitSecond = Integer.valueOf(splitTime[1]);

        Long mMilisSecond = (long) (((splitMinute * 60) + splitSecond) * 1000);
        timer = new CounterClass(mMilisSecond,1000);
        timer.start();
    }

    public class CounterClass extends CountDownTimer {

        private long mAlert=4000;
        private int paramAlert=1;
        private String mTimer;
        boolean isRunning = false;

        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override public void onFinish() {
            isRunning = false;
            txtTimer.setText(getResources().getString(R.string.initial_time));

            new MaterialDialog.Builder(ctx)
                    .title(R.string.dialog_title)
                    .content(R.string.dialog_exercise)
                    .positiveText(R.string.dialog_button_positif)
                    .positiveColorRes(R.color.color_primary)
                    .titleGravity(GravityEnum.CENTER)
                    .titleColorRes(R.color.color_primary)
                    .contentColorRes(R.color.text_sub_title)
                    .backgroundColorRes(R.color.background_content_list)
                    .callback(new MaterialDialog.ButtonCallback() {
                        @Override
                        public void onPositive(MaterialDialog dialog) {
                            finish();
                        }

                    })
                    .show();

        }

        @Override
        public void onTick(long millisUntilFinished) {
            isRunning = true;
            if (millisUntilFinished < mAlert && paramAlert==1){

                if(Utils.loadPreferences(Utils.ARG_SOUND, ctx)==Utils.ARG_SOUND_ON){
                    MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alert_beep);
                    mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    switch (am.getRingerMode()) {
                        case AudioManager.RINGER_MODE_SILENT:
                            mp.setVolume(0, 0);
                            break;
                    }
                    mp.start();
                }



                paramAlert+=1;
            }

            mTimer = String.format("%02d:%02d", TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished), TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

            txtTimer.setText(mTimer);

        }

        public String timerPause(){
            return mTimer;
        }
        public Boolean timerCheck(){
            return isRunning;
        }

    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer.timerCheck())timer.cancel();
        wl.acquire();

    }

    /** Called before the activity is destroyed */
    @Override
    public void onStop() {
        super.onStop();
        if(timer.timerCheck())timer.cancel();
        wl.acquire();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

}
