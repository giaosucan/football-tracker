package com.android.gps.gui;

import com.android.gps.util.RunnerTrack;
import android.app.Application;

public class RunApplication extends Application {

    public boolean runtrackOK;
    public RunnerTrack runnerTrack = null;

    // public int mUnitType;

    @Override
    public void onCreate() {
	super.onCreate();
	// mUnitType = Constant.UNIT_KMH;

    }

    @Override
    public void onTerminate() {
	super.onTerminate();
    }
}
