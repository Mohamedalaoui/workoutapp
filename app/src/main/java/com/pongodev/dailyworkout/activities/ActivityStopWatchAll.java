package com.pongodev.dailyworkout.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.SystemClock;
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

/**
 * Created by keong on 1/2/2015.
 */
public class ActivityStopWatchAll extends ActionBarActivity implements View.OnClickListener{

    // create object of views
    private TextView txtTimer, txtTimerRest, txtNextName;
    private TextSwitcher txtTitle;
    private ButtonFlat btnReset;
    private ButtonFloat btnStart;
    private ViewFlipper flipper;
    private RelativeLayout lytRest;

    // create object of WakeLock class
    private PowerManager.WakeLock wl;

    // set variables as FLAGs
    private boolean FLAG = false;
    private boolean STOP = false;
    private boolean START = true;

    private int paramData=0;
    // create object of handler class
    // and set variables for time
    private Handler myHandler = new Handler();
    private long startTime = 0L;
    private long timeInMillies = 0L;
    private long timeSwap = 0L;
    private long finalTime = 0L;

    // create variable to store data
    private String mId;
    private ArrayList<String> mListId     = new ArrayList<String>();
    private ArrayList<String> mListName   = new ArrayList<String>();
    private ArrayList<String> mListImage  = new ArrayList<String>();
    private ArrayList<String> mListTime   = new ArrayList<String>();
    private ArrayList<String> Images      = new ArrayList<String>();

    private int ScreenHeight = 0;

    private ArrayList<ArrayList<Object>> data;

    private Utils utils;
    private DBHelperWorkouts dbWorkouts;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stopwatch_all);

        utils = new Utils(this);

        // get values that passed from previous page
        Intent i = getIntent();
        mListId    = i.getStringArrayListExtra(utils.EXTRA_ID);
        mListName  = i.getStringArrayListExtra(utils.EXTRA_NAME);
        mListTime  = i.getStringArrayListExtra(utils.EXTRA_TIME);

        // connect views object and views id on xml
        txtTitle = (TextSwitcher) findViewById(R.id.txtTitle);
        txtTimer = (TextView) findViewById(R.id.txtTimer);
        txtTimerRest = (TextView) findViewById(R.id.txtTimerRest);
        txtNextName  = (TextView) findViewById(R.id.txtNextName);
        btnStart = (ButtonFloat) findViewById(R.id.btnStart);
        btnReset = (ButtonFlat) findViewById(R.id.btnReset);
        flipper  = (ViewFlipper) findViewById(R.id.flipper);
        toolbar  = (Toolbar) findViewById(R.id.toolbar);
        lytRest  = (RelativeLayout) findViewById(R.id.lytRest);

        // Set the ViewFactory of the TextSwitcher that will create TextView object when asked
        txtTitle.setFactory(new ViewSwitcher.ViewFactory() {

            public View makeView() {
                // TODO Auto-generated method stub
                // create new textView and set the properties like clolr, size etc
                TextView myText = new TextView(ActivityStopWatchAll.this);
                myText.setGravity(Gravity.CENTER_HORIZONTAL);
                myText.setTypeface(null, Typeface.BOLD);
                myText.setTextSize(24);
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

        // Get PowerManager to keep screen on
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "SCREEN ON");

        // Set icon Drawable start button
        btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play));

        // Set up the toolbar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        btnStart.setOnClickListener(this);
        btnReset.setOnClickListener(this);

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
    public class getDataList extends AsyncTask<Void, Void, Void> {
        ProgressDialog progress;

        @Override
        protected void onPreExecute() {
            // TODO Auto-generated method stub

            // show progress dialog when fetching data from database
            progress= ProgressDialog.show(
                    ActivityStopWatchAll.this,
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

            txtTitle.setText(mListName.get(paramData));

            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            ScreenHeight = screenWidth / 2 + 50;

            if(paramData > 0){
                //flipper.startFlipping();
                wl.acquire();
                FLAG = START;
                btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_pause));
                btnReset.setEnabled(false);
                startTime = SystemClock.uptimeMillis();
                myHandler.postDelayed(updateTimerMethod, 0);
                flipper.removeAllViews();
            }

            for(int i=0;i<Images.size();i++){
                FrameLayout fl = new FrameLayout(ActivityStopWatchAll.this);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(screenWidth, ScreenHeight);
                fl.setLayoutParams(lp);

                ImageView imgWorkout = new ImageView(ActivityStopWatchAll.this);
                imgWorkout.setScaleType(ImageView.ScaleType.FIT_CENTER);
                int imagedata = getResources().getIdentifier(Images.get(i), "drawable", getPackageName());
                imgWorkout.setImageResource(imagedata);

                fl.addView(imgWorkout, new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

                flipper.addView(fl);
                flipper.startFlipping();

            }

        }

    }

    // method to get data from database
    private void getDataFromDatabase(){
        dbWorkouts = new DBHelperWorkouts(getApplicationContext());
        dbWorkouts.checkDBWorkouts();


        data = dbWorkouts.getImages(mListId.get(paramData));

        Images.clear();

        // store data to arraylist variable
        for(int i=0;i<data.size();i++){
            ArrayList<Object> row = data.get(i);

            Images.add(row.get(0).toString());
        }

    }

    // thread to run stopwatch
    private Runnable updateTimerMethod = new Runnable() {

        public void run() {
            String timer = "";
            String Paramtimer = "";
            timeInMillies = SystemClock.uptimeMillis() - startTime;

            finalTime = timeSwap + timeInMillies;

            int seconds = (int) (finalTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliseconds = (int) (finalTime / 10);
            milliseconds = milliseconds % 100;

            timer = String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":" + String.format("%02d", milliseconds);
            Paramtimer = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
            txtTimer.setText(timer);

            if(Paramtimer.equals(mListTime.get(paramData))){
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alert_beep);
                mp.start();

                flipper.stopFlipping();
                FLAG = STOP;
                btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play));
                btnReset.setEnabled(true);
                myHandler.removeCallbacks(this);

                paramData +=1;

                if(paramData != (mListId.size())){
                    // call synctask to get images
                    getRest();
                } else {
                    new MaterialDialog.Builder(ActivityStopWatchAll.this)
                            .title(R.string.dialog_title)
                            .content(R.string.dialog_content)
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


            }else{
                myHandler.postDelayed(this, 0);
            }
        }

    };

    // thread to run stopwatch
    private Runnable updateTimerRest = new Runnable() {

        public void run() {
            String timer = "";
            String Paramtimer = "";
            timeInMillies = SystemClock.uptimeMillis() - startTime;

            finalTime = timeSwap + timeInMillies;

            int seconds = (int) (finalTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliseconds = (int) (finalTime / 10);
            milliseconds = milliseconds % 100;

            timer = String.format("%02d", minutes) + ":" + String.format("%02d", seconds) + ":" + String.format("%02d", milliseconds);
            Paramtimer = String.format("%02d", minutes) + ":" + String.format("%02d", seconds);
            txtTimerRest.setText(timer);

            if (Paramtimer.equals("00:15")) {
                MediaPlayer mp = MediaPlayer.create(getApplicationContext(), R.raw.alert_beep);
                mp.start();

                myHandler.removeCallbacks(this);

                lytRest.setVisibility(View.GONE);
                new getDataList().execute();

            } else {
                myHandler.postDelayed(this, 0);
            }

        }

    };
    private void getRest(){
        lytRest.setVisibility(View.VISIBLE);
        myHandler.postDelayed(updateTimerRest, 0);
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
                if(!FLAG){

                    flipper.startFlipping();
                    wl.acquire();
                    FLAG = START;
                    btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_pause));
                    btnReset.setEnabled(false);
                    startTime = SystemClock.uptimeMillis();
                    myHandler.postDelayed(updateTimerMethod, 0);

                }else{
                    flipper.stopFlipping();
                    wl.release();
                    FLAG = STOP;
                    btnStart.setIconDrawable(getResources().getDrawable(R.drawable.ic_play));
                    btnReset.setEnabled(true);
                    timeSwap += timeInMillies;
                    myHandler.removeCallbacks(updateTimerMethod);
                }
                break;

            case R.id.btnReset:
                // TODO Auto-generated method stub
                startTime = 0L;

                timeInMillies = 0L;
                timeSwap = 0L;
                finalTime = 0L;
                txtTimer.setText(getString(R.string.initial_time));
                break;

            default:
                break;
        }
    }
}
