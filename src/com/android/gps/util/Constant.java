package com.android.gps.util;

public class Constant {

    // The minimum distance to change Updates in meters
    public static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 2; // 10 meters

    // The minimum time between updates in milliseconds
    public static final long MIN_TIME_BW_UPDATES = 2 * 1 * 1; // 1 s

    //
    public static final int DEFAULT_ZOOM_LEVEL = 18;

    // default threshold m/sec
    public static final float DEFAULT_THRESHOLD_VALUE_MS_A = 2.5f;
    public static final float DEFAULT_THRESHOLD_VALUE_MS_B = 4.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_MS_C = 5.5f;
    public static final float DEFAULT_THRESHOLD_VALUE_MS_D = 7.0f;

    // default threshold kmh
    public static final float DEFAULT_THRESHOLD_VALUE_KMH_A = 10.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_KMH_B = 15.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_KMH_C = 20.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_KMH_D = 25.0f;

    // default threshold mhph
    public static final float DEFAULT_THRESHOLD_VALUE_MPH_A = 10.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_MPH_B = 25.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_MPH_C = 20.0f;
    public static final float DEFAULT_THRESHOLD_VALUE_MPH_D = 25.0f;

    public static final int THRESH_TYPE_A = 0;
    public static final int THRESH_TYPE_B = 1;
    public static final int THRESH_TYPE_C = 2;
    public static final int THRESH_TYPE_D = 3;

    // default threshold
    public static final int TRACKING_START = 1;
    public static final int TRACKING_PAUSE = 0;

    // define menu item
    public static final int MENU_TRACKER = 1;
    public static final int MENU_SETTING = 2;
    public static final int MENU_HISTORY = 3;
    public static final int MENU_UNIT = 4;
    public static final int MENU_MAP_VIEW = 5;
    public static final int MENU_HELP = 6;
    public static final int MENU_EXIT = 7;

    // define setting menu item
    public static final int SETTING_MIN_TIME_UPDATE = 1;
    public static final int SETTING_DIST_TIME_UPDATE = 2;
    public static final int SETTING_THRESHOLD = 3;
    public static final int SETTING_BRIGHTNESS = 4;
    public static final int SETTING_DIST_UNIT = 5;

    //
    public static final int VIEW_HISTORY = 0;
    public static final int VIEW_CURRENT = 1;

    // define unit
    public static final int UNIT_KMH = 1;
    public static final int UNIT_MPH = 2;
    public static final int UNIT_MS = 3;
    public static final String UNIT_TYPE = "com.android.atheletracker.unittype";
    // Constant for identifying the dialog
    public static final int DIALOG_CONFIRM_EXIT = 1;
    public static final int DIALOG_GSP_SETTING_NOTFICATION = 2;

    public static final String THRESHOLD = "THRESHOLD";
    public static final String THRESHOLD_A = "THRESHOLD_A";
    public static final String THRESHOLD_B = "THRESHOLD_B";
    public static final String THRESHOLD_C = "THRESHOLD_C";
    public static final String THRESHOLD_D = "THRESHOLD_D";

    public static final String COMMAND_DOWNLOAD = "com.android.atheletracker.commanddownload";
    public static final String COMMAND_UPLOAD = "com.android.atheletracker.commandupload";
    public static final String COMMAND_LISTFILE = "com.android.atheletracker.commandlistfile";
    public static final String COMMAND_CONNECT = "com.android.atheletracker.commandconnect";

    public static final String LOGIN_STATUS = "com.android.atheletracker.loginstatus";
    public static final String FTP_SERVER = "com.android.atheletracker.server";
    public static final String FTP_USERNAME = "com.android.atheletracker.username";
    public static final String FTP_PASSWORD = "com.android.atheletracker.password";
    public static final String FTP_PORT = "com.android.atheletracker.port";
    public static final String MIN_UPDATE_TIME = "com.android.atheletracker.mintimeupdate";
    public static final String MIN_UPDATE_DISTANCE = "com.android.atheletracker.mindistanceupdate";

    public static final String APP_LOGO_PATH = "http://i70.photobucket.com/albums/i91/giaosucan/app_icon_zps9997fb19.png";
    public static final String APP_ID = "546502555422719";

    public static final String PREF_NAME = "com.android.atheletracker";

    public static final String TOP_LEFT_LAT = "com.android.atheletracker.topleftlat";
    public static final String TOP_LEFT_LON = "com.android.atheletracker.topleftlon";
    public static final String TOP_RIGHT_LAT = "com.android.atheletracker.toprightlat";
    public static final String TOP_RIGHT_LON = "com.android.atheletracker.toprightlon";
    public static final String BOTTOM_LEFT_LAT = "com.android.atheletracker.bottomleftlat";
    public static final String BOTTOM_LEFT_LON = "com.android.atheletracker.bottomleftlon";

}
