/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.activities;

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
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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

public class ActivityStopWatchAll extends ActionBarActivity implements View.OnClickListener{

    // create object of views
    private TextView txtTimer, txtNextName, txtTimerRest;
    private TextSwitcher txtTitle;
    private ButtonFlat btnReset;
    private ButtonFloat btnStart, btnSound;
    private ViewFlipper flipper;
    private RelativeLayout lytRest;
    private AudioManager am;

    // create object of WakeLock class
    private PowerManager.WakeLock wl;
    private Context ctx;
    private  CounterClass timer = null;

    // set variables as FLAGs
    private boolean FLAG = false;
    private boolean paramPause = false;
    private boolean paramRest  = true;

    // create variable to store data
    private String currentTime ="00:00";
    private int mCurrenntWorkout=0;
    private int paramData=0;

    private ArrayList<String> mListId     = new ArrayList<>();
    private ArrayList<String> mListName   = new ArrayList<>();
    private ArrayList<String> mListTime   = new ArrayList<>();
    private ArrayList<String> Images      = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch);

        ctx = this;

        // get values that passed from previous page
        Intent i= getIntent();
        mListId    = i.getStringArrayListExtra(Utils.ARG_ID);
        mListName  = i.getStringArrayListExtra(Utils.ARG_NAME);
        mListTime  = i.getStringArrayListExtra(Utils.ARG_TIME);

        // connect views object and views id on xml
        txtTitle    = (TextSwitcher) findViewById(R.id.txtTitle);
        txtNextName = (TextView) findViewById(R.id.txtNextName);
        txtTimer    = (TextView) findViewById(R.id.txtTimer);
        txtTimerRest= (TextView) findViewById(R.id.txtTimerRest);
        btnStart    = (ButtonFloat) findViewById(R.id.btnStart);
        btnSound    = (ButtonFloat) findViewById(R.id.btnSound);
        btnReset    = (ButtonFlat) findViewById(R.id.btnReset);
        flipper     = (ViewFlipper) findViewById(R.id.flipper);
        lytRest     = (RelativeLayout) findViewById(R.id.lytRest);

        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);
        btnSound.setOnClickListener(this);

        txtTimer.setText(mListTime.get(paramData));

        // Customization Title text
        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        txtTitle.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(ActivityStopWatchAll.this);
                myText.setGravity(Gravity.CENTER_HORIZONTAL);
                myText.setTypeface(null, Typeface.BOLD);
                myText.setTextSize(getResources().getDimension(R.dimen.text_title_stopwatch));
                myText.setTextColor(getResources().getColor(R.color.color_primary));
                return myText;
            }
        });

        // Declare the in and out animations and initialize them
        Animation in = AnimationUtils.loadAnimation(this,android.R.anim.slide_in_left);
        Animation out = AnimationUtils.loadAnimation(this,android.R.anim.slide_out_right);

        // Set the animation type of textSwitcher
        txtTitle.setInAnimation(in);
        txtTitle.setOutAnimation(out);

        // Setting default volume
        am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, Utils.ARG_DEFAULT_VOLUME, 0);

        // get PowerManager to keep screen on
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SCREEN ON");
        wl.acquire();

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

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub
            btnStart.setEnabled(true);
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
            mCurrenntWorkout+=1;
            paramData +=1;

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = screenWidth / 2 + 50;

            if(paramData > 1){
                txtTitle.setText(mCurrenntWorkout+"/"+ mListName.size()+"  "+ mListName.get(paramData-1));
                wl.acquire();
                FLAG = true;
                btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_pause_36dp));
                btnReset.setEnabled(false);
                btnReset.setTextColor(getResources().getColor(R.color.btn_disable));
                flipper.removeAllViews();
                paramRest=true;
                startTimer((mListTime.get(paramData-1)));
            } else {
                txtTitle.setText(mCurrenntWorkout+"/"+ mListName.size()+"  "+ mListName.get(paramData-1));
            }

            for(int i=0;i<Images.size();i++){
                FrameLayout fl = new FrameLayout(ActivityStopWatchAll.this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth, screenHeight);
                fl.setLayoutParams(lp);

                ImageView imgWorkout = new ImageView(ActivityStopWatchAll.this);
                imgWorkout.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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


        ArrayList<ArrayList<Object>> data = dbWorkouts.getImages(mListId.get(paramData));

        Images.clear();

        // store data to arraylist variable
        for(int i=0;i< data.size();i++){
            ArrayList<Object> row = data.get(i);
            Images.add(row.get(0).toString());
        }
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

            // Condition when next data still exist
            if(paramData != (mListId.size())) {
                // Condition when Rest
                if(paramRest){
                    txtTimer.setText(getResources().getString(R.string.initial_time));
                    getRest();

                // Condition when exercise again
                } else {
                    txtTimerRest.setText(getResources().getString(R.string.initial_time));
                    lytRest.setVisibility(View.GONE);
                    new getDataList().execute();
                }

            // Condition when no next data
            } else {

                btnStart.setEnabled(false);
                btnReset.setEnabled(false);
                btnReset.setTextColor(getResources().getColor(R.color.btn_disable));
                btnStart.setBackgroundColor(getResources().getColor(R.color.btn_disable));
                
                new MaterialDialog.Builder(ctx)
                        .title(R.string.dialog_title)
                        .content(R.string.dialog_all_exercises)
                        .positiveText(R.string.dialog_button_positif)
                        .positiveColorRes(R.color.color_primary)
                        .titleGravity(GravityEnum.CENTER)
                        .titleColorRes(R.color.color_primary)
                        .contentColorRes(R.color.text_sub_title)
                        .backgroundColorRes(R.color.background_content_list)
                        .callback(new MaterialDialog.ButtonCallback() {
                            @Override
                            public void onPositive(MaterialDialog dialog) {
                                if (wl != null) {
                                    wl.release();
                                    wl=null;
                                }
                                finish();
                            }

                        })
                        .show();
            }
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

            if(paramRest){
                txtTimer.setText(mTimer);
            } else {
                txtTimerRest.setText(mTimer);
            }
        }

        public String timerPause(){
            return mTimer;
        }
        public Boolean timerCheck(){
            return isRunning;
        }

    }

    private void getRest(){
        paramRest=false;
        lytRest.setVisibility(View.VISIBLE);
        btnStart.setEnabled(false);
        btnReset.setEnabled(false);
        txtNextName.setText(mListName.get(paramData));
        startTimer(Utils.ARG_DEFAULT_REST);
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(timer.timerCheck())timer.cancel();

        if (wl != null) {
            wl.release();
            wl=null;
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
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

                    // Condition from pause and play again
                    if(paramPause){
                        timer.cancel();
                        startTimer(currentTime);
                    // Condition from beginning
                    } else {
                        startTimer((mListTime.get(paramData-1)));
                    }

                    btnReset.setEnabled(false);
                    btnReset.setTextColor(getResources().getColor(R.color.btn_disable));

                    // Condition when button pause push
                } else {
                    if(flipper.isFlipping()) flipper.stopFlipping();
                    wl.release();
                    FLAG = false;
                    paramPause = true;
                    btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play_36dp));
                    btnReset.setEnabled(true);
                    btnReset.setTextColor(getResources().getColor(R.color.btn_enable));
                    timer.cancel();
                    currentTime = timer.timerPause();
                    txtTimer.setText(currentTime);

                }
                break;

            case R.id.btnReset:
                // TODO Auto-generated method stub
                paramPause = false;
                timer.cancel();
                txtTimer.setText((mListTime.get(paramData-1)));

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

}


