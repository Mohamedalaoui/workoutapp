package com.pongodev.dailyworkout.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class Utils {
	
	// Declare object of Context class
	Context ctx;
	public static final String EXTRA_ID 	= "idSelected";
    public static final String EXTRA_NAME 	= "NameSelected";
    public static final String EXTRA_WORKOUT_TIME 	= "workoutTimeSelected";
    public static final String EXTRA_ACTIVITY 	    = "activity";

    public static final String ACTIVITY_WORKOUT 	= "workout";
    public static final String ACTIVITY_PROGRAM 	= "program";

    public static final String CHECK_PLAY_SERV	= "playService";

	// Condition for admob and social media (true=visible, false=gone)
    public static final boolean paramAdmob=true;

    public static final int ARG_GONE = 8;

    // Admob visibility parameter. set 0 to show admob and 8 to hide.
    public static final int ARG_ADMOB_VISIBILITY = 0;

	public Utils(Context c){
		ctx = c;
	}
	
	// Method to check internet connection
	public static boolean isNetworkAvailable(Context c) {
		ConnectivityManager connectivity = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity == null) {
			return false;
		} else {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
				}
			}
		}
		return false;
	}

    // Method to load map type setting
    public static int loadPreferences(String param, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        int value = sharedPreferences.getInt(param, 0);


        return value;
    }

    // Method to save map type setting to SharedPreferences
    public static void savePreferences(String param, int value, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(param, value);
        editor.commit();
    }

}
