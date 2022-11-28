/*
 * Copyright (C) 2013
 * Setting class
 */

package com.android.gps.calc;

/*
 *
 * Class for setting basic parameter
 * Minimum distance to update GPS
 * Minimum interval time to update GPS
 */

public class Setting {
    long minDistUpdate;
    long minTimeUpdate;
    double Threashold;

    public Setting(long minDistUpdate, long minTimeUpdate, double Threashold) {
	super();
	this.minDistUpdate = minDistUpdate;
	this.minTimeUpdate = minTimeUpdate;
	this.Threashold = Threashold;
    }

    public double getMinDistUpdate() {
	return minDistUpdate;
    }

    public double getMintTimeUpdate() {
	return minTimeUpdate;
    }

    public double getThreashold() {
	return Threashold;
    }
}
