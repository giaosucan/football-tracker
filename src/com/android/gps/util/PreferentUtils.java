package com.android.gps.util;

import com.android.gps.gui.SprintExpandedAdapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferentUtils {
    private Context mContext;

    public PreferentUtils(Context context) {
	this.mContext = context;
    }

    public String getFTPUserName() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	// return mShareRefs_day_night.getString(Constant.FTP_USERNAME,
	// "sergiodelv@raditel.net");
	return mShareRefs_day_night.getString(Constant.FTP_USERNAME, "");
    }

    public void saveFTPUserName(String username) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putString(Constant.FTP_USERNAME, username);
	editor.commit();
    }

    public String getFTPPass() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	// return mShareRefs_day_night.getString(Constant.FTP_PASSWORD,
	// "athletetracker2013");
	return mShareRefs_day_night.getString(Constant.FTP_PASSWORD, "");
    }

    public void saveFTPPass(String pass) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putString(Constant.FTP_PASSWORD, pass);
	editor.commit();
    }

    public long getFTPPort() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	return mShareRefs_day_night.getLong(Constant.FTP_PORT, 21);
    }

    public void saveLoginStatus(boolean status) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putBoolean(Constant.LOGIN_STATUS, status);
	editor.commit();
    }

    public boolean getLoginStatus() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	return mShareRefs_day_night.getBoolean(Constant.LOGIN_STATUS, false);
    }

    public void saveFTPPort(long port) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putLong(Constant.FTP_PORT, Long.valueOf(port));
	editor.commit();
    }

    public String getFTPServer() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	// return mShareRefs_day_night.getString(Constant.FTP_SERVER,
	// "ftp.ausemed.com.au");
	return mShareRefs_day_night.getString(Constant.FTP_SERVER, "");

    }

    public void saveFTPServer(String server) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putString(Constant.FTP_SERVER, server);
	editor.commit();
    }

    public Float getThresholdValue(String thresold, int threshType) {
	float ThreshValue = 0.0f;
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	switch (threshType) {
	case Constant.THRESH_TYPE_A:
	    ThreshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_A;
	    break;
	case Constant.THRESH_TYPE_B:
	    ThreshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_B;

	    break;
	case Constant.THRESH_TYPE_C:
	    ThreshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_C;
	    break;
	case Constant.THRESH_TYPE_D:
	    ThreshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_D;

	    break;
	default:
	    break;
	}
	return mShareRefs_day_night.getFloat(thresold, ThreshValue);
	// return ThreshValue;
    }

    public void saveThresholdValue(String threadshold, float value) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putFloat(threadshold, value);
	editor.commit();
    }

    public float getMinTimeUpdate() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	return mShareRefs_day_night.getFloat(Constant.MIN_UPDATE_TIME,
		Constant.MIN_TIME_BW_UPDATES);
    }

    public void saveMinTimeUpdate(float value) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putFloat(Constant.MIN_UPDATE_TIME, value);
	editor.commit();
    }

    public float getDistanceUpdate() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	return mShareRefs_day_night.getFloat(Constant.MIN_UPDATE_DISTANCE,
		Constant.MIN_DISTANCE_CHANGE_FOR_UPDATES);
    }

    public void saveMinDistanceUpdate(float value) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putFloat(Constant.MIN_UPDATE_DISTANCE, value);
	editor.commit();
    }

    public void saveCoodinateValue(String coordinateType, float value) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putFloat(coordinateType, value);
	editor.commit();
    }

    public Float getCoodinate(String coordinateType) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	float defaultval;
	// 21.032146&lon=105.783470
	if (coordinateType.equals(Constant.TOP_LEFT_LAT)) {
	    defaultval = (float) 21.032146;
	} else if (coordinateType.equals(Constant.TOP_LEFT_LON)) {
	    defaultval = (float) 105.783470;
	} else // 21.031955&lon=105.786656
	if (coordinateType.equals(Constant.TOP_RIGHT_LAT)) {
	    defaultval = (float) 21.031955;
	} else if (coordinateType.equals(Constant.TOP_RIGHT_LON)) {
	    defaultval = (float) 105.786656;
	} else // 21.029893&lon=105.783073
	if (coordinateType.equals(Constant.BOTTOM_LEFT_LAT)) {
	    defaultval = (float) 21.029893;
	} else if (coordinateType.equals(Constant.BOTTOM_LEFT_LON)) {
	    defaultval = (float) 105.783073;
	} else {
	    defaultval = 0;
	}

	return mShareRefs_day_night.getFloat(coordinateType, defaultval);
    }

    public void saveUnitType(int value) {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	Editor editor = mShareRefs_day_night.edit();
	editor.putInt(Constant.UNIT_TYPE, value);
	editor.commit();
    }

    public int getUnitType() {
	SharedPreferences mShareRefs_day_night = mContext.getSharedPreferences(
		Constant.PREF_NAME, Activity.MODE_PRIVATE);
	return mShareRefs_day_night.getInt(Constant.UNIT_TYPE,
		Constant.UNIT_KMH);
    }

}
