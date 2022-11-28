package com.android.gps.main;

import android.app.Activity;

public class App {
    // //////////////////////////////////////////////////////////////
    // INSTANTIATED ACTIVITY VARIABLES
    // //////////////////////////////////////////////////////////////

    public static Activity MainActivity;
    public static Activity ViewRunningActivity;
    public static Activity HelpActivity;
    public static Activity HelpTopicActivity;

    // //////////////////////////////////////////////////////////////
    // CLOSE APP METHOD
    // //////////////////////////////////////////////////////////////

    public static void close() {
	if (App.MainActivity != null) {
	    App.MainActivity.finish();
	}
	if (App.ViewRunningActivity != null) {
	    App.ViewRunningActivity.finish();
	}
	if (App.HelpActivity != null) {
	    App.HelpActivity.finish();
	}
	if (App.HelpTopicActivity != null) {
	    App.HelpTopicActivity.finish();
	}
    }
}
