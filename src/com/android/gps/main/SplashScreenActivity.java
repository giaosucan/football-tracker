package com.android.gps.main;

/**
 * Display a Splash Screen when application start up
 */

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Intent;

public class SplashScreenActivity extends Activity {

    // Splash screen timer
    private static int SPLASH_TIME_OUT = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_splash_screen);

	/* Showing splash screen with a timer */

	new Handler().postDelayed(new Runnable() {

	    @Override
	    public void run() {
		// TODO Auto-generated method stub
		Intent i = new Intent(SplashScreenActivity.this,
			MainActivity.class);
		startActivity(i);
		// close this activity
		finish();
	    }
	}, SPLASH_TIME_OUT);
    }

}
