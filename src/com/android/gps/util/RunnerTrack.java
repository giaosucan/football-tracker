package com.android.gps.util;

import java.util.ArrayList;

import com.android.gps.calc.Sprints;
import com.google.android.gms.maps.model.LatLng;

public class RunnerTrack {

    public final static int SPRINT_TYPE_A = 0;
    public final static int SPRINT_TYPE_B = 1;
    public final static int SPRINT_TYPE_C = 2;
    public final static int SPRINT_TYPE_D = 3;

    private ArrayList<LatLng> mListPoint = new ArrayList<LatLng>();

    public ArrayList<LatLng> getmListPoint() {
	return mListPoint;
    }

    public void setmListPoint(ArrayList<LatLng> mListPoint) {
	this.mListPoint = mListPoint;
    }

    private long mDuration;
    private double mDistance;
    public String timeText;

    private ArrayList<Sprints> listSprint = new ArrayList<Sprints>();

    public float htopleftlat;
    public float htopleftlon;
    public float htoprightlat;
    public float htoprightlon;
    public float hbottomleftlat;
    public float hbottomleftlon;
    public double thresh[] = new double[4];
    public int unitType = Constant.UNIT_KMH;

    public ArrayList<Sprints> getListSprint() {
	return listSprint;
    }

    public void setListSprint(ArrayList<Sprints> listSprint) {
	this.listSprint = listSprint;
    }

    public void addSprint(Sprints spr) {
	listSprint.add(spr);
    }

    public RunnerTrack() {

    }

    public RunnerTrack(long interval, ArrayList<Sprints> sprints) {
	mDuration = interval;
	listSprint = sprints;
    }

    public void setDuration(long mDuration) {
	this.mDuration = mDuration;
    }

    public long getDuration() {
	return mDuration;
    }

    public void setDistance(double mDistance) {
	this.mDistance = mDistance;
    }

    public double getDistance() {
	return mDistance;
    }

}