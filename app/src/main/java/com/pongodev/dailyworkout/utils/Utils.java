/*
* Copyright (c) 2015 Pongodev. All Rights Reserved.
*/
package com.pongodev.dailyworkout.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.pongodev.dailyworkout.R;

public class Utils {

    // Application parameters. do not change this parameters.
    public static final String ARG_PAGE         = "page";
    public static final String ARG_WORKOUT 	    = "workout";
    public static final String ARG_PROGRAM 	    = "program";
	public static final String ARG_ID 	        = "idSelected";
    public static final String ARG_NAME 	    = "NameSelected";
    public static final String ARG_TIME 	    = "TimeSelected";
    public static final String ARG_LIST_ID      = "activityListId";
    public static final String ARG_LIST_NAME    = "activityListName";
    public static final String ARG_LIST_PAGE    = "listPage";
    public static final String ARG_SOUND        = "sound";
    public static final int ARG_SOUND_ON        = 1;
    public static final int ARG_SOUND_OFF   = 0;
    public static final int ARG_GONE        = 8;
    public static final int ARG_DEBUGGING   = 1;

    // Admob visibility parameter. set 0 to show admob and 8 to hide.
    public static final int ARG_ADMOB_VISIBILITY = 0;
    // Set value to 1 if you are still in development process, and zero if you are ready to publish the app.
    public static final int ARG_ADMOB_DEVELOPMENT_TYPE = 1;
    // Set default category data, you can see the name id in sqlite database.
    public static final String ARG_DEFAULT_REST = "00:15";
    // Set default volume
    public static final int ARG_DEFAULT_VOLUME = 7;
    // Set default category data, you can see the name id in sqlite database.
    public static final String ARG_DEFAULT_PREFERENCE = "unknown";

    public static void loadAdmob(final AdView ad){
        // Create an ad request.
        AdRequest adRequest;
        if(ARG_ADMOB_DEVELOPMENT_TYPE == ARG_DEBUGGING) {
            adRequest = new AdRequest.Builder().
                    addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        }else {
            adRequest = new AdRequest.Builder().build();
        }

        // Start loading the ad.
        ad.loadAd(adRequest);

        ad.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                if (ad != null) {
                    ad.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public static boolean admobVisibility(AdView ad, int parameter){
        ad.setVisibility(parameter);
        return parameter != ARG_GONE;
    }

    public static void loadAdmobInterstitial(final InterstitialAd interstitialAd, Context c){

        interstitialAd.setAdUnitId(c.getResources().getString(R.string.interstitial_ad_unit_id));
        // Create an ad request.
        AdRequest adRequest;
        if(ARG_ADMOB_DEVELOPMENT_TYPE == ARG_DEBUGGING) {
            adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
        }else {
            adRequest = new AdRequest.Builder().build();
        }

        // Start loading the ad.
        interstitialAd.loadAd(adRequest);

        // Set the AdListener.
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                if (interstitialAd.isLoaded()) {
                    interstitialAd.show();
                }
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

            }

            @Override
            public void onAdClosed() {

            }

        });

    }

    // Method to load map type setting
    public static int loadPreferences(String param, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        return sharedPreferences.getInt(param, 0);
    }

    // Method to save map type setting to SharedPreferences
    public static void savePreferences(String param, int value, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(param, value);
        editor.apply();
    }

    // Method to load map type setting
    public static String loadString(String param, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data1", 0);

        return sharedPreferences.getString(param, ARG_DEFAULT_PREFERENCE);
    }

    // Method to save map type setting to SharedPreferences
    public static void saveString(String param, String value, Context c){
        SharedPreferences sharedPreferences = c.getSharedPreferences("user_data1", 0);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(param, value);
        editor.apply();
    }

}

