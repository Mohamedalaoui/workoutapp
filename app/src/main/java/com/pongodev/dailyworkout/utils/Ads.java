package com.pongodev.dailyworkout.utils;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Ads {

    public static void loadAdmob(AdView ad){
        AdRequest adRequest = new AdRequest.Builder().
                addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        ad.loadAd(adRequest);
    }

    public static boolean admobVisibility(AdView ad, int parameter){
        ad.setVisibility(parameter);
        if(parameter == Utils.ARG_GONE )
            return false;
        else
            return true;
    }

	public static void loadAds(AdView ads){
		
        
		/**
      	 * code below is used to test admob on device during development process.
      	 */
		AdRequest adRequest = new AdRequest.Builder().addTestDevice("YOUR_DEVICE_ID").build();
		//AdRequest adRequest = new AdRequest.Builder().build();
		/**
      	 * code below is used to publish admob when the app launched.
      	 * remove the comment tag below and delete block of code 
      	 * that used for testing admob.
      	 */
		
		ads.loadAd(adRequest);

      	/**
      	 * the end of admob code
      	 */
	}
}
