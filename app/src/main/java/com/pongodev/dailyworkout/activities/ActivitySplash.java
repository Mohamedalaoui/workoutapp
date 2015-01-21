package com.pongodev.dailyworkout.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

import com.pongodev.dailyworkout.R;


public class ActivitySplash extends ActionBarActivity {

    private int progress = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Loading().execute();
    }

    public class Loading extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {

            while(progress < 100){
                try {
                    Thread.sleep(1000);
                    progress += 40;
                }catch(InterruptedException ie){
                    ie.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent i = new Intent(getApplicationContext(), ActivityHome.class);
            startActivity(i);
        }
    }


}
