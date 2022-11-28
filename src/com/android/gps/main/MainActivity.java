package com.android.gps.main;

/**
 * Main screen of application
 * 
 */

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import com.android.gps.calc.Sprints;
import com.android.gps.gui.HelpActivity;
import com.android.gps.gui.RunApplication;
import com.android.gps.gui.ViewRunningActivity;
import com.android.gps.heatmap.DrawHeatMap;
import com.android.gps.main.R;

import com.android.gps.server.FileManager;
import com.android.gps.server.ServerTask;
import com.android.gps.server.ServerTask.FTPRequestResult;
import com.android.gps.util.PreferentUtils;
import com.android.gps.util.RunData;
import com.android.gps.util.RunnerTrack;
import com.android.gps.util.TimeTick;
import com.android.gps.util.Constant;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.ViewFlipper;
import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

public class MainActivity extends FragmentActivity implements OnClickListener,
	FTPRequestResult, OnMapLongClickListener {
    // Google Map
    GoogleMap mGoogleMap;
    GPSTracker mGPSTrack;
    Marker mMarkPos;
    int mPointType;
    int mMapType;
    boolean mHeatMapView;
    ImageView mImgHeatMap;
    DrawHeatMap mDraw;
    // GUI elements
    Button mStop;
    ToggleButton mStart;
    ToggleButton mShowHideBtn;
    ImageButton mSetThreshold;
    ActionBar mActionBar;
    TextView mTxtDistance;
    TextView mTxtSpeed;
    TextView txtClockFace;
    Chronometer mChron;
    ViewFlipper mViewFlipper;
    ProgressDialog mProgressDialog = null;

    // Threshold
    float mThreshold[] = new float[4];
    boolean mTrackingStatus = false;
    boolean mZoomMapStatus = false;
    LatLng mFirstLocate;
    LatLng mPrevLocate;
    LatLng mCurLocate;
    String mDateActivity;
    String mTimeActivity;
    float lastX;
    int mUnit;
    // the run which athlete made
    double mDistance;
    double mSpeed;

    ServerTask mFtpServerTask;

    private final ArrayList<LatLng> mListPoint = new ArrayList<LatLng>();
    ArrayList<RunData> mRundata = new ArrayList<RunData>();
    private long mPrevTimeStamp = 0;
    PreferentUtils mPrefUtil;
    NotificationManager mNotifyManager;
    final static int NOTIFY_ID = 1;
    boolean mGpsStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	// if(savedInstanceState)
	setContentView(R.layout.activity_location);

	// create action bar when Android version >3.0
	if (isAndroid2x() == false) {
	    createActionBar();
	} else {

	}

	initParameter();
	mPrefUtil = new PreferentUtils(this);
	// Start time clock
	mChron = (Chronometer) this.findViewById(R.id.chronometer);
	txtClockFace = (TextView) this.findViewById(R.id.txtTimeValue);
	TimeTick chronInterface = new TimeTick(txtClockFace, "HH:mm:ss");
	mChron.setOnChronometerTickListener(chronInterface);

	// Initialize Google Map
	FragmentManager fmanager = getSupportFragmentManager();
	Fragment fragment = fmanager.findFragmentById(R.id.map);
	// boolean test = GooglePlayServicesUtil.i
	int resultCode = GooglePlayServicesUtil
		.isGooglePlayServicesAvailable(this);
	if (resultCode != ConnectionResult.SUCCESS) {
	    Builder builder = new AlertDialog.Builder(this);
	    builder.setMessage("Google Play Service is out of date !");
	    builder.setCancelable(true);
	    builder.setPositiveButton("OK", null);
	    AlertDialog dialog = builder.create();
	    dialog.show();
	} else {
	    mGoogleMap = ((SupportMapFragment) fragment).getMap();
	    mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
	    mGoogleMap.getUiSettings().setCompassEnabled(true);
	    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
	    mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
	    mGoogleMap.setTrafficEnabled(true);
	    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
	    mGoogleMap.setOnMapLongClickListener(this);
	}

	// default Unit
	mUnit = mPrefUtil.getUnitType();
	mMapType = GoogleMap.MAP_TYPE_SATELLITE;

	// Initialize GUI elements
	mStop = (Button) findViewById(R.id.btnStopTracking);
	mStart = (ToggleButton) findViewById(R.id.toggleBtnStart);
	mShowHideBtn = (ToggleButton) findViewById(R.id.toogleShowHide);
	mSetThreshold = (ImageButton) findViewById(R.id.imgBtnSetting);
	mTxtDistance = (TextView) findViewById(R.id.txtDistValue);
	mTxtSpeed = (TextView) findViewById(R.id.txtSprintValue);
	mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
	mDraw = new DrawHeatMap(this, R.drawable.img_football_field);

	if (isAndroid2x() == false) {
	    mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	}

	if (mUnit == Constant.UNIT_KMH) {
	    mTxtDistance.setText("" + 0.0f + " km");
	    mTxtSpeed.setText("" + 0.0f + "km/h");
	} else if (mUnit == Constant.UNIT_MPH) {
	    mTxtDistance.setText("" + 0.0f + " mi");
	    mTxtSpeed.setText("" + 0.0f + "mph");
	} else {
	    mTxtDistance.setText("" + 0.0f + " m");
	    mTxtSpeed.setText("" + 0.0f + "m/sec");
	}

	mShowHideBtn.setOnClickListener(this);
	mSetThreshold.setOnClickListener(this);
	mStop.setOnClickListener(this);
	mStart.setOnClickListener(this);

	// display Stop button at starting state
	mStop.setEnabled(false);

	// setting default Threshold

	mThreshold[0] = mPrefUtil.getThresholdValue(Constant.THRESHOLD_A,
		Constant.THRESH_TYPE_A);
	mThreshold[1] = mPrefUtil.getThresholdValue(Constant.THRESHOLD_B,
		Constant.THRESH_TYPE_B);
	mThreshold[2] = mPrefUtil.getThresholdValue(Constant.THRESHOLD_C,
		Constant.THRESH_TYPE_C);
	mThreshold[3] = mPrefUtil.getThresholdValue(Constant.THRESHOLD_D,
		Constant.THRESH_TYPE_D);

    }

    @Override
    public void onMapLongClick(LatLng point) {
	mPointType++;
	if (mPointType <= 3) {
	    mMarkPos = mGoogleMap.addMarker(new MarkerOptions()
		    .position(point)
		    .title("The Field here")
		    .icon(BitmapDescriptorFactory
			    .fromResource(R.drawable.ic_pin)).draggable(true));
	    Toast.makeText(
		    this,
		    "Lat :" + point.latitude + "\n" + "Long:" + point.longitude,
		    Toast.LENGTH_SHORT).show();
	}
	if (mPointType == 1) {
	    mPrefUtil.saveCoodinateValue(Constant.TOP_LEFT_LAT,
		    (float) point.latitude);
	    mPrefUtil.saveCoodinateValue(Constant.TOP_LEFT_LON,
		    (float) point.longitude);
	} else if (mPointType == 2) {
	    mPrefUtil.saveCoodinateValue(Constant.TOP_RIGHT_LAT,
		    (float) point.latitude);
	    mPrefUtil.saveCoodinateValue(Constant.TOP_RIGHT_LON,
		    (float) point.longitude);
	} else if (mPointType == 3) {
	    mPrefUtil.saveCoodinateValue(Constant.BOTTOM_LEFT_LAT,
		    (float) point.latitude);
	    mPrefUtil.saveCoodinateValue(Constant.BOTTOM_LEFT_LON,
		    (float) point.longitude);
	}

	if (mPointType == 3) {
	    setFieldConfirm();
	}
    }

    private void setFieldConfirm() {
	// Create out AlterDialog
	Builder builder = new AlertDialog.Builder(this);
	builder.setMessage("Do you confirm the field setting");
	builder.setCancelable(true);
	builder.setPositiveButton("Confirm", new ConfirmOnClickListner());
	builder.setNegativeButton("Re-setting", new ResetlOnClickListener());
	AlertDialog dialog = builder.create();
	dialog.show();
    }

    private final class ConfirmOnClickListner implements
	    DialogInterface.OnClickListener {
	public void onClick(DialogInterface dialog, int which) {
	    mMarkPos = null;
	}
    }

    private final class ResetlOnClickListener implements
	    DialogInterface.OnClickListener {
	public void onClick(DialogInterface dialog, int which) {
	    mPointType = 0;
	    mGoogleMap.clear();
	    MarkerOptions markPoint = new MarkerOptions()
		    .position(mFirstLocate).icon(
			    BitmapDescriptorFactory
				    .fromResource(R.drawable.ic_marker_first));
	    mMarkPos = mGoogleMap.addMarker(markPoint);
	    mMarkPos = null;
	}
    }

    @Override
    protected void onStart() {
	super.onStart();
	initGPS((long) mPrefUtil.getDistanceUpdate(),
		(long) mPrefUtil.getMinTimeUpdate(), Constant.TRACKING_START);

	App.MainActivity = this;
    };

    @Override
    protected void onPause() {
	// TODO Auto-generated method stub
	super.onPause();
    }

    @Override
    protected void onResume() {
	// TODO Auto-generated method stub
	if (!CheckEnableGPS()) {
	    new AlertDialog.Builder(this)
		    .setTitle("Enable GPS")
		    .setMessage("Please enable GPS for this feature")
		    .setPositiveButton("OK",
			    new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog,
					int which) {
				    // TODO Auto-generated method stub
				    enableGPS();
				}
			    })
		    .setNegativeButton("Cancel",
			    new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
					int which) {
				    // TODO Auto-generated method stub
				    MainActivity.this.finish();
				}
			    }).show();
	} else {
	}

	super.onResume();
    }

    @Override
    protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	App.MainActivity = null;
    }

    @Override
    // Method to handle touch event like left to right swap and right to left
    // swap
    public boolean onTouchEvent(MotionEvent touchevent) {

	switch (touchevent.getAction()) {
	// when user first touches the screen to swap
	case MotionEvent.ACTION_DOWN: {
	    lastX = touchevent.getX();
	    break;
	}
	case MotionEvent.ACTION_UP: {
	    float currentX = touchevent.getX();

	    // if left to right swipe on screen
	    if (lastX < currentX) {

		// If no more View/Child to flip
		if (mViewFlipper.getDisplayedChild() == 0)
		    break;

		// set the required Animation type to ViewFlipper
		// The Next screen will come in form Left and current Screen
		// will go OUT from Right
		mViewFlipper.setInAnimation(this, R.anim.in_from_left);
		mViewFlipper.setOutAnimation(this, R.anim.out_to_right);
		// Show the next Screen
		mViewFlipper.showNext();
		mHeatMapView = false;
	    }

	    // if right to left swipe on screen
	    if (lastX > currentX) {

		if (mViewFlipper.getDisplayedChild() == 1)
		    break;
		// set the required Animation type to ViewFlipper
		// The Next screen will come in form Right and current Screen
		// will go OUT from Left
		mViewFlipper.setInAnimation(this, R.anim.in_from_right);
		mViewFlipper.setOutAnimation(this, R.anim.out_to_left);
		// Show The Previous Screen
		mViewFlipper.showPrevious();
		mHeatMapView = true;

	    }
	    break;
	}
	}

	return false;
    }

    @SuppressLint("NewApi")
    public void createActionBar() {
	mActionBar = getActionBar();
	mActionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
		| ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);
	mActionBar.setDisplayShowTitleEnabled(true);
	// actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
	mActionBar.show();
	mActionBar.setLogo(R.drawable.app_icon);

    }

    private boolean isAndroid2x() {
	boolean isAndroid2x = true;
	int currentapiVersion = android.os.Build.VERSION.SDK_INT;
	if (currentapiVersion >= android.os.Build.VERSION_CODES.HONEYCOMB) {
	    // Do something for HONEYCOMB and above versions
	    isAndroid2x = false;
	} else {
	    // do something for phones running an SDK before HONEYCOMB
	    isAndroid2x = true;
	}
	return isAndroid2x;
    }

    public void initParameter() {
	mTrackingStatus = false;
	mZoomMapStatus = false;
	mListPoint.clear();
	mDistance = 0;
	mSpeed = 0;
	mPointType = 0;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
	//
	// if (isAndroid2x() == false) {
	// SubMenu subMenu = menu.addSubMenu(0, 0, 1,
	// "").setIcon(R.drawable.ic_menu);
	// MenuItem subMenuItem = subMenu.getItem();
	// subMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS |
	// MenuItem.SHOW_AS_ACTION_WITH_TEXT);
	// }
	menu.add(0, 2, 2, "Setting").setIcon(R.drawable.ic_setting);

	menu.add(0, 3, 3, "History").setIcon(R.drawable.ic_history);

	menu.add(0, 4, 4, "Change Unit").setIcon(R.drawable.ic_unit);

	menu.add(0, 5, 5, "Map View").setIcon(R.drawable.ic_map);

	menu.add(0, 6, 6, "Help").setIcon(R.drawable.ic_help);

	menu.add(0, 7, 7, "Exit").setIcon(R.drawable.ic_exit);

	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
	// if(mStop.isEnabled())
	// {
	// Toast.makeText(this,"Please stop tracking before using !",
	// Toast.LENGTH_LONG)
	// .show();
	// return true;
	// }
	Intent intent;
	switch (item.getItemId()) {
	case Constant.MENU_SETTING:
	    showSettingDialog();
	    break;
	case Constant.MENU_HISTORY:
	    showHistoryListTrack();
	    break;
	case Constant.MENU_UNIT:
	    // intent = new Intent(MediaStore.INTENT_ACTION_MUSIC_PLAYER);
	    // startActivity(intent);
	    showSettingUnitDialog();
	    break;
	case Constant.MENU_MAP_VIEW:
	    showSetViewMapDialog();
	    break;
	case Constant.MENU_HELP:
	    intent = new Intent(this, HelpActivity.class);
	    startActivity(intent);
	    break;
	case Constant.MENU_EXIT:
	    showDialog(Constant.DIALOG_CONFIRM_EXIT);
	    break;
	} // for items handled
	return true;
    }

    private void showHistoryListTrack() {
	new AlertDialog.Builder(this)
		.setTitle("Please select")
		.setMessage("Open run track from")
		.setPositiveButton("Server",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// TODO Auto-generated method stub
				openFileServer();
			    }
			})
		.setNeutralButton("Local",
			new DialogInterface.OnClickListener() {

			    @Override
			    public void onClick(DialogInterface dialog,
				    int which) {
				// TODO Auto-generated method stub
				openFileLocal();
			    }
			}).setNegativeButton("Cancel", null).show();
    }

    private void showSettingUnitDialog() {
	final CharSequence[] items = { "Km/h", "Mph", "m/sec" };
	int choice = 0;
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Select Unit");
	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

	    @Override
	    public void onClick(DialogInterface dialog, int which) {
		updateUIUnitchange();
		mPrefUtil.saveUnitType(mUnit);
	    }
	});

	if (mUnit == Constant.UNIT_KMH) {
	    choice = 0;
	} else if (mUnit == Constant.UNIT_MPH) {
	    choice = 1;
	} else {
	    choice = 2;
	}

	builder.setSingleChoiceItems(items, choice,
		new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub

			if ("Km/h".equals(items[which])) {

			    if (mUnit == Constant.UNIT_MS) {
				mDistance = mDistance / 1000;
			    } else if (mUnit == Constant.UNIT_MPH) {
				mDistance = mDistance * 1.604;
			    }
			    mUnit = Constant.UNIT_KMH;
			    // mThreshold[0] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_A,Constant.THRESH_TYPE_A);
			    // mThreshold[1] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_B,Constant.THRESH_TYPE_B);
			    // mThreshold[2] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_C,Constant.THRESH_TYPE_C);
			    // mThreshold[3] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_D,Constant.THRESH_TYPE_D);

			} else if ("Mph".equals(items[which])) {

			    if (mUnit == Constant.UNIT_KMH) {
				mDistance = mDistance * 1000 / 1604;
			    } else if (mUnit == Constant.UNIT_MS) {
				mDistance = mDistance / 1604;
			    }
			    mUnit = Constant.UNIT_MPH;
			    // mThreshold[0] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_A,Constant.THRESH_TYPE_A)*
			    // 1000/ 60;
			    // mThreshold[0] = (float) (Math.round(mThreshold[0]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[1] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_B,Constant.THRESH_TYPE_B)*
			    // 1000/ 60;
			    // mThreshold[1] = (float) (Math.round(mThreshold[1]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[2] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_C,Constant.THRESH_TYPE_C)*
			    // 1000/ 60;
			    // mThreshold[2] = (float) (Math.round(mThreshold[2]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[3] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_D,Constant.THRESH_TYPE_D)*
			    // 1000/ 60;
			    // mThreshold[3] = (float) (Math.round(mThreshold[3]
			    // * 100.0) / 100.0);

			} else if ("m/sec".equals(items[which])) {

			    if (mUnit == Constant.UNIT_KMH) {
				mDistance = mDistance * 1000;
			    } else if (mUnit == Constant.UNIT_MPH) {
				mDistance = mDistance * 1604;
			    }
			    mUnit = Constant.UNIT_MS;
			    // mThreshold[0] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_A,Constant.THRESH_TYPE_A)*
			    // 1000/3600;
			    // mThreshold[0] = (float) (Math.round(mThreshold[0]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[1] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_B,Constant.THRESH_TYPE_B)*
			    // 1000/3600;
			    // mThreshold[1] = (float) (Math.round(mThreshold[1]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[2] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_C,Constant.THRESH_TYPE_C)*
			    // 1000/3600;
			    // mThreshold[2] = (float) (Math.round(mThreshold[2]
			    // * 100.0) / 100.0);
			    //
			    // mThreshold[3] =
			    // mPrefUtil.getThresholdValue(Constant.THRESHOLD_D,Constant.THRESH_TYPE_D)*
			    // 1000/3600;
			    // mThreshold[3] = (float) (Math.round(mThreshold[3]
			    // * 100.0) / 100.0);
			} else {

			}
			// ((RunApplication)getApplication()).mUnitType = mUnit;
		    }
		});
	builder.show();
    }

    @SuppressLint("NewApi")
    private void updateUIUnitchange() {
	DecimalFormat df;
	if (mUnit == Constant.UNIT_KMH) {
	    df = new DecimalFormat("##.####");
	} else if (mUnit == Constant.UNIT_MPH) {
	    df = new DecimalFormat("##.###");
	} else {
	    df = new DecimalFormat("##.##");
	}
	df.setRoundingMode(RoundingMode.HALF_DOWN);
	df.format(mDistance);

	if (mUnit == Constant.UNIT_KMH) {
	    mTxtDistance.setText("" + df.format(mDistance) + " km");
	    mTxtSpeed.setText("" + Math.round(3.6 * mSpeed * 100.0) / 100.0
		    + "km/h");
	} else if (mUnit == Constant.UNIT_MPH) {
	    mTxtDistance.setText("" + df.format(mDistance) + " mi");
	    mTxtSpeed.setText("" + Math.round(mSpeed * 2.23694 * 100) / 100.0
		    + " mph");
	} else {
	    mTxtDistance.setText("" + df.format(mDistance) + " m");
	    mTxtSpeed.setText("" + Math.round(mSpeed * 100.0) / 100.0
		    + " m/sec");
	}
    }

    private void showSetViewMapDialog() {
	final CharSequence[] items = { "Normal", "Hybrid", "Satellite",
		"Terrain" };
	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Select Map View");
	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

	    @Override
	    public void onClick(DialogInterface dialog, int which) {
	    }
	});

	builder.setSingleChoiceItems(items, -1,
		new DialogInterface.OnClickListener() {

		    @Override
		    public void onClick(DialogInterface dialog, int which) {
			// TODO Auto-generated method stub

			if ("Normal".equals(items[which])) {
			    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
			    mMapType = GoogleMap.MAP_TYPE_NORMAL;
			} else if ("Hybrid".equals(items[which])) {
			    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
			    mMapType = GoogleMap.MAP_TYPE_HYBRID;
			} else if ("Satellite".equals(items[which])) {
			    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
			    mMapType = GoogleMap.MAP_TYPE_SATELLITE;
			} else if ("Satellite".equals(items[which])) {
			    mGoogleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
			    mMapType = GoogleMap.MAP_TYPE_TERRAIN;
			} else {

			}
		    }
		});
	builder.show();
    }

    private void showSettingDialog() {
	final Dialog dialog = new Dialog(MainActivity.this,
		android.R.style.Theme_Translucent);
	dialog.setContentView(R.layout.full_setting_dialog_box);
	dialog.show();

	Button btnSaveSetting = (Button) dialog
		.findViewById(R.id.btn_savesetting);
	Button btnCancelSetting = (Button) dialog
		.findViewById(R.id.btncancelsetting);
	dialog.setCancelable(true);

	final EditText editFTPServer = (EditText) dialog
		.findViewById(R.id.servertext);
	final EditText editFTPUser = (EditText) dialog
		.findViewById(R.id.usernametext);
	final EditText editFTPPass = (EditText) dialog
		.findViewById(R.id.passwordtext);
	final EditText editFTPPort = (EditText) dialog
		.findViewById(R.id.ftpporttext);
	final EditText editMinTimeUpdate = (EditText) dialog
		.findViewById(R.id.gpsupdatetime);
	final EditText editMinDistanceUpdate = (EditText) dialog
		.findViewById(R.id.mindistanceupdate);

	editFTPServer.setText(mPrefUtil.getFTPServer());
	editFTPUser.setText(mPrefUtil.getFTPUserName());
	editFTPPass.setText(mPrefUtil.getFTPPass());
	editFTPPort.setText(String.valueOf(mPrefUtil.getFTPPort()));

	editMinTimeUpdate.setText(String.valueOf(mPrefUtil.getMinTimeUpdate()));
	editMinDistanceUpdate.setText(String.valueOf(mPrefUtil
		.getDistanceUpdate()));

	btnSaveSetting.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		mPrefUtil.saveFTPPass(editFTPPass.getText().toString());
		mPrefUtil.saveFTPServer(editFTPServer.getText().toString());
		mPrefUtil.saveFTPUserName(editFTPUser.getText().toString());
		mPrefUtil.saveFTPPort(Long.valueOf(editFTPPort.getText()
			.toString()));

		mPrefUtil.saveMinDistanceUpdate(Float
			.valueOf(editMinDistanceUpdate.getText().toString()));
		mPrefUtil.saveMinTimeUpdate(Float.valueOf(editMinTimeUpdate
			.getText().toString()));
		dialog.dismiss();
	    }
	});

	btnCancelSetting.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		dialog.dismiss();
	    }
	});

    }

    @SuppressWarnings("deprecation")
    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case Constant.DIALOG_CONFIRM_EXIT:
	    closeAppConfirm();
	    break;
	}
	return super.onCreateDialog(id);
    }

    private void closeAppConfirm() {
	// Create out AlterDialog
	Builder builder = new AlertDialog.Builder(this);
	builder.setMessage("Do you want to close this app");
	builder.setCancelable(true);
	builder.setPositiveButton("Yes", new OkOnClickListener());
	builder.setNegativeButton("No", new CancelOnClickListener());
	AlertDialog dialog = builder.create();
	dialog.show();
    }

    private final class CancelOnClickListener implements
	    DialogInterface.OnClickListener {
	public void onClick(DialogInterface dialog, int which) {
	    Toast.makeText(getApplicationContext(), "Enjoy tracking",
		    Toast.LENGTH_LONG).show();
	}
    }

    private final class OkOnClickListener implements
	    DialogInterface.OnClickListener {
	public void onClick(DialogInterface dialog, int which) {
	    if (isAndroid2x() == false) {
		mNotifyManager.cancel(NOTIFY_ID);
	    }
	    App.close();
	}
    }

    @SuppressLint("NewApi")
    @Override
    public void onClick(View v) {
	String elapsedTime;
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.btnStopTracking:
	    if (isAndroid2x() == false) {
		mNotifyManager.cancel(NOTIFY_ID);
	    }
	    Toast.makeText(getApplicationContext(), "Stop tracking",
		    Toast.LENGTH_LONG).show();
	    // make vibration
	    Vibrator vibraStop = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    // Vibrate for 500 milliseconds
	    vibraStop.vibrate(100);
	    // stop using GPS
	    if (mGPSTrack != null)
		mGPSTrack.stopUsingGPS(true);
	    // get duration Time
	    elapsedTime = (String) mChron.getText();
	    mChron.stop();
	    // save coordinate
	    if (((RunApplication) getApplication()).runnerTrack != null) {
		((RunApplication) getApplication()).runnerTrack.htopleftlat = mPrefUtil
			.getCoodinate(Constant.TOP_LEFT_LAT);
		((RunApplication) getApplication()).runnerTrack.htopleftlon = mPrefUtil
			.getCoodinate(Constant.TOP_LEFT_LON);
		((RunApplication) getApplication()).runnerTrack.htoprightlat = mPrefUtil
			.getCoodinate(Constant.TOP_RIGHT_LAT);
		((RunApplication) getApplication()).runnerTrack.htoprightlon = mPrefUtil
			.getCoodinate(Constant.TOP_RIGHT_LON);
		((RunApplication) getApplication()).runnerTrack.hbottomleftlat = mPrefUtil
			.getCoodinate(Constant.BOTTOM_LEFT_LAT);
		((RunApplication) getApplication()).runnerTrack.hbottomleftlon = mPrefUtil
			.getCoodinate(Constant.BOTTOM_LEFT_LON);
		((RunApplication) getApplication()).runnerTrack.unitType = mUnit;
		((RunApplication) getApplication()).runnerTrack.thresh[0] = mPrefUtil
			.getThresholdValue(Constant.THRESHOLD_A,
				Constant.THRESH_TYPE_A);
		((RunApplication) getApplication()).runnerTrack.thresh[1] = mPrefUtil
			.getThresholdValue(Constant.THRESHOLD_B,
				Constant.THRESH_TYPE_B);
		((RunApplication) getApplication()).runnerTrack.thresh[2] = mPrefUtil
			.getThresholdValue(Constant.THRESHOLD_C,
				Constant.THRESH_TYPE_C);
		((RunApplication) getApplication()).runnerTrack.thresh[3] = mPrefUtil
			.getThresholdValue(Constant.THRESHOLD_D,
				Constant.THRESH_TYPE_D);
	    }

	    // //
	    finish();
	    // launch the statistic screen
	    Intent intent = new Intent(this, ViewRunningActivity.class);
	    intent.putExtra("mMapType", mMapType);
	    intent.putExtra("state", Constant.VIEW_CURRENT);
	    startActivity(intent);
	    mDistance = 0;
	    mStop.setEnabled(false);
	    mStart.setChecked(false);

	    break;
	case R.id.toogleShowHide:
	    if (isAndroid2x() == false) {
		if (mShowHideBtn.isChecked()) {
		    mActionBar.hide();
		} else {
		    mActionBar.show();
		}
	    } else {

	    }
	    break;
	case R.id.imgBtnSetting:
	    final Dialog dialog = new Dialog(MainActivity.this,
		    android.R.style.Theme_Translucent);
	    dialog.setContentView(R.layout.setting_dialog_box);
	    // dialog.setTitle("Adjust Threshold " + getUnitName(mUnit));
	    dialog.show();
	    Button btnSaveThresh = (Button) dialog
		    .findViewById(R.id.btn_saveThreshold);
	    Button btnCancelThresh = (Button) dialog
		    .findViewById(R.id.btnCancelThresh);
	    dialog.setCancelable(true);
	    EditText editThresh1 = (EditText) dialog
		    .findViewById(R.id.editThreshold1);
	    EditText editThresh2 = (EditText) dialog
		    .findViewById(R.id.editThreshold2);
	    EditText editThresh3 = (EditText) dialog
		    .findViewById(R.id.editThreshold3);
	    EditText editThresh4 = (EditText) dialog
		    .findViewById(R.id.editThreshold4);
	    TextView title = (TextView) dialog.findViewById(R.id.txtTitle);

	    title.setText("set sprnt threshold " + getUnitName(mUnit));
	    editThresh1.setText(String.valueOf(mThreshold[0]));
	    editThresh2.setText(String.valueOf(mThreshold[1]));
	    editThresh3.setText(String.valueOf(mThreshold[2]));
	    editThresh4.setText(String.valueOf(mThreshold[3]));

	    btnSaveThresh.setOnClickListener(new OnClickListener() {
		@Override
		public void onClick(View v) {
		    EditText editThresh1 = (EditText) dialog
			    .findViewById(R.id.editThreshold1);
		    EditText editThresh2 = (EditText) dialog
			    .findViewById(R.id.editThreshold2);
		    EditText editThresh3 = (EditText) dialog
			    .findViewById(R.id.editThreshold3);
		    EditText editThresh4 = (EditText) dialog
			    .findViewById(R.id.editThreshold4);

		    float tempThresold[] = new float[4];
		    // TODO Auto-generated method stub
		    try {
			if (editThresh1.getText().toString() != null) {
			    tempThresold[0] = Float.parseFloat(editThresh1
				    .getText().toString());
			} else {
			    tempThresold[0] = getThresholdDefault(
				    Constant.THRESH_TYPE_A, mUnit);
			}
			if (editThresh2.getText().toString() != null) {
			    tempThresold[1] = Float.parseFloat(editThresh2
				    .getText().toString());
			} else {
			    tempThresold[1] = getThresholdDefault(
				    Constant.THRESH_TYPE_B, mUnit);
			}

			if (editThresh3.getText().toString() != null) {
			    tempThresold[2] = Float.parseFloat(editThresh3
				    .getText().toString());
			} else {
			    tempThresold[2] = getThresholdDefault(
				    Constant.THRESH_TYPE_C, mUnit);
			}
			if (editThresh4.getText().toString() != null) {
			    tempThresold[3] = Float.parseFloat(editThresh4
				    .getText().toString());
			} else {
			    tempThresold[3] = getThresholdDefault(
				    Constant.THRESH_TYPE_D, mUnit);
			}

			PreferentUtils prefUtils = new PreferentUtils(
				MainActivity.this);
			if (tempThresold[0] < tempThresold[1]
				&& tempThresold[1] < tempThresold[2]
				&& tempThresold[2] < tempThresold[3]) {
			    mThreshold[0] = tempThresold[0];
			    mThreshold[1] = tempThresold[1];
			    mThreshold[2] = tempThresold[2];
			    mThreshold[3] = tempThresold[3];

			    prefUtils.saveThresholdValue(Constant.THRESHOLD_A,
				    mThreshold[0]);
			    prefUtils.saveThresholdValue(Constant.THRESHOLD_B,
				    mThreshold[1]);
			    prefUtils.saveThresholdValue(Constant.THRESHOLD_C,
				    mThreshold[2]);
			    prefUtils.saveThresholdValue(Constant.THRESHOLD_D,
				    mThreshold[3]);
			    dialog.dismiss();
			} else {
			    new AlertDialog.Builder(MainActivity.this)
				    .setTitle("Invalid Thresold Value")
				    .setMessage(
					    "Please enter correct value: Thresold A < Thresold B < Thresold C < Thresold D")
				    .setPositiveButton("OK", null).show();
			}
		    } catch (Exception e) {
			e.printStackTrace();
		    }

		}
	    });
	    btnCancelThresh.setOnClickListener(new OnClickListener() {

		@Override
		public void onClick(View v) {
		    // TODO Auto-generated method stub
		    dialog.dismiss();
		}
	    });
	    break;
	case R.id.toggleBtnStart:
	    if (isAndroid2x() == false) {
		displayNotification(NOTIFY_ID, R.drawable.app_notify,
			"Athlete Tracker is tracking your running");
	    }
	    // Enable Stop button
	    mStop.setEnabled(true);
	    // make vibration
	    Vibrator vibraStart = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
	    // Vibrate for q00 milliseconds
	    vibraStart.vibrate(100);

	    if (mStart.isChecked()) {
		int stoppedMilSeconds = 0;
		// elapsedTime =(String) mChron.getText();
		elapsedTime = (String) txtClockFace.getText();
		String array[] = elapsedTime.split(":");
		stoppedMilSeconds = Integer.parseInt(array[0]) * 60 * 60 * 1000
			+ Integer.parseInt(array[1]) * 60 * 1000
			+ Integer.parseInt(array[2]) * 1000;
		mChron.setBase(SystemClock.elapsedRealtime()
			- stoppedMilSeconds);
		mChron.start();
		mGpsStart = true;
		Toast.makeText(getApplicationContext(), "Start tracking",
			Toast.LENGTH_SHORT).show();
		if (mTrackingStatus == false) {
		    Log.d("Location Activity", "Time start");
		    // initGPS((long) mPrefUtil.getDistanceUpdate(),(long)
		    // mPrefUtil.getMinTimeUpdate(),Constant.TRACKING_START);
		} else {
		    Log.d("Location Activity", "Time pause");
		    initGPS((long) mPrefUtil.getDistanceUpdate(),
			    (long) mPrefUtil.getMinTimeUpdate(),
			    Constant.TRACKING_PAUSE);
		}
		mTrackingStatus = true;
	    } else {
		Toast.makeText(getApplicationContext(), "Pause tracking",
			Toast.LENGTH_SHORT).show();
		if (mGPSTrack != null)
		    mGPSTrack.stopUsingGPS(false);
		elapsedTime = (String) mChron.getText();
		mChron.stop();
		mGpsStart = false;
	    }
	    break;

	}
    }

    private float getThresholdDefault(int thresholdType, int unit) {
	float threshValue = 0.0f;
	switch (thresholdType) {
	case Constant.THRESH_TYPE_A:
	    if (unit == Constant.UNIT_KMH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_A;
	    } else if (unit == Constant.UNIT_MPH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MPH_A;
	    } else {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MS_A;
	    }
	    break;
	case Constant.THRESH_TYPE_B:
	    if (unit == Constant.UNIT_KMH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_B;
	    } else if (unit == Constant.UNIT_MPH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MPH_B;
	    } else {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MS_B;
	    }
	    break;
	case Constant.THRESH_TYPE_C:
	    if (unit == Constant.UNIT_KMH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_C;
	    } else if (unit == Constant.UNIT_MPH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MPH_C;
	    } else {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MS_C;
	    }
	    break;
	case Constant.THRESH_TYPE_D:
	    if (unit == Constant.UNIT_KMH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_KMH_D;
	    } else if (unit == Constant.UNIT_MPH) {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MPH_D;
	    } else {
		threshValue = Constant.DEFAULT_THRESHOLD_VALUE_MS_D;
	    }
	    break;
	}
	return threshValue;
    }

    @SuppressLint("NewApi")
    private void displayNotification(int notifyID, int icon, String msg) {

	// Crate Notification when app run on background
	// intent triggered, you can add other intent for other actions
	Intent intent = new Intent(this, this.getClass());
	intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
		PendingIntent.FLAG_CANCEL_CURRENT);

	// Build the notification!
	Notification mNotification = new Notification.Builder(this)
		.setContentTitle("Athelte Tracker").setContentText(msg)
		.setSmallIcon(icon).setContentIntent(pIntent).build();

	// Hide the notification after it was selected
	mNotification.flags |= Notification.FLAG_AUTO_CANCEL;
	mNotification.flags = Notification.FLAG_ONGOING_EVENT
		| Notification.FLAG_NO_CLEAR;
	mNotifyManager.notify(notifyID, mNotification);
    }

    public void initGPS(long minDistance, long minTime, int status) {
	try {
	    // setup GPS
	    mGPSTrack = new GPSTracker(MainActivity.this, minDistance, minTime,
		    status);
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void displayLocation(LatLng locate) {

	try {

	    // if map has not already loaded
	    if (mZoomMapStatus == false) {
		Log.d("displayLocation", "Load map");
		mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locate,
			Constant.DEFAULT_ZOOM_LEVEL));
		mGoogleMap
			.animateCamera(CameraUpdateFactory
				.zoomTo(Constant.DEFAULT_ZOOM_LEVEL), 2000,
				null);
		mZoomMapStatus = true;
	    }
	    mGoogleMap.setInfoWindowAdapter(new InfoWindowAdapter() {

		@Override
		public View getInfoWindow(Marker arg0) {
		    // TODO Auto-generated method stub
		    return null;
		}

		@SuppressLint("NewApi")
		@Override
		public View getInfoContents(Marker arg0) {
		    // TODO Auto-generated method stub
		    DecimalFormat df;
		    if (mUnit == Constant.UNIT_KMH) {
			df = new DecimalFormat("##.####");
		    } else if (mUnit == Constant.UNIT_MPH) {
			df = new DecimalFormat("##.###");
		    } else {
			df = new DecimalFormat("##.##");
		    }
		    df.setRoundingMode(RoundingMode.HALF_DOWN);
		    double distance = 0.0f;
		    distance = mGPSTrack.getDistance();
		    df.format(distance);

		    Log.d("displayLocation", "the distance which athele made: "
			    + distance);
		    if (mUnit == Constant.UNIT_KMH) {
			mTxtDistance.setText("" + df.format(distance) + " km");
		    } else if (mUnit == Constant.UNIT_MPH) {
			mTxtDistance.setText("" + df.format(distance) + " mi");
		    } else {
			mTxtDistance.setText("" + df.format(distance) + " m");
		    }

		    // double dSprints = sprints.calcSprints(mThreshold,
		    // distance);
		    Log.d("displayLocation",
			    "the current speed which athele made: " + mSpeed);
		    if (mUnit == Constant.UNIT_KMH) {
			mTxtSpeed.setText("" + Math.round(3.6 * mSpeed * 100.0)
				/ 100.0 + "km/h");
		    } else if (mUnit == Constant.UNIT_MS) {
			mTxtSpeed.setText("" + Math.round(mSpeed * 100.0)
				/ 100.0 + " m/sec");
		    } else {
			mTxtSpeed.setText(""
				+ Math.round(mSpeed * 2.23694 * 100) / 100.0
				+ " mph");
		    }

		    View v = getLayoutInflater().inflate(R.layout.windowlayout,
			    null);

		    TextView tvDistance = (TextView) v
			    .findViewById(R.id.txtDistance);

		    TextView tvSprints = (TextView) v
			    .findViewById(R.id.txt_sprints);
		    if (mUnit == Constant.UNIT_KMH) {
			tvDistance.setText("Distance: " + df.format(distance)
				+ " km");
		    } else if (mUnit == Constant.UNIT_MPH) {
			tvDistance.setText("Distance: " + df.format(distance)
				+ " mi");
		    } else {
			tvDistance.setText("Distance: " + df.format(distance)
				+ " m");
		    }

		    // display total sprint
		    RunnerTrack runTrack = processRunData();
		    Log.d("PRDCV", "sprint size "
			    + runTrack.getListSprint().size());
		    ((RunApplication) getApplication()).runnerTrack = runTrack;
		    tvSprints.setText("Total Sprints: "
			    + runTrack.getListSprint().size());

		    return v;
		}
	    });

	    // mGoogleMap.clear();
	    if (mMarkPos != null) {
		mMarkPos.remove();
	    }
	    for (int i = 0; i < mListPoint.size(); i++) {
		if (i != (mListPoint.size() - 1)) {
		    MarkerOptions markPoint = new MarkerOptions().position(
			    mListPoint.get(i)).icon(
			    BitmapDescriptorFactory
				    .fromResource(R.drawable.ic_marker));
		    mMarkPos = mGoogleMap.addMarker(markPoint);
		} else {
		    // add current marker
		    MarkerOptions maker = new MarkerOptions()
			    .position(locate)
			    .icon(BitmapDescriptorFactory
				    .defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
		    mMarkPos = mGoogleMap.addMarker(maker);
		    mMarkPos.showInfoWindow();
		}

	    }

	    // User view Heat Map
	    if (mHeatMapView == true) {

		// Load Football field
		mImgHeatMap = (ImageView) findViewById(R.id.heat_map_view);
		mImgHeatMap.setAdjustViewBounds(true);
		Bitmap mutableBitmap = mDraw.drawHeadMapPos(mListPoint,
			Color.BLUE, Color.RED);
		mImgHeatMap.setImageBitmap(mutableBitmap);
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    private class GPSTracker extends Service implements LocationListener {

	private final Context mContext;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	Location location; // location

	// Declaring a Location Manager
	protected LocationManager locationManager;

	public GPSTracker(Context context, long minDistance, long minTime,
		int status) {
	    this.mContext = context;
	    setupGPS(minDistance, minTime, status);
	}

	public Location setupGPS(long minDistance, long minTime, int status) {
	    try {
		locationManager = (LocationManager) mContext
			.getSystemService(LOCATION_SERVICE);

		final Criteria criteria = new Criteria();

		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setSpeedRequired(true);
		criteria.setAltitudeRequired(false);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);

		// getting GPS status
		isGPSEnabled = locationManager
			.isProviderEnabled(LocationManager.GPS_PROVIDER);

		// getting network status
		isNetworkEnabled = locationManager
			.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (!isGPSEnabled && !isNetworkEnabled) {
		    // no network provider is enabled
		    Toast.makeText(mContext,
			    "There's no network provider is enabled",
			    Toast.LENGTH_LONG).show();
		} else {
		    this.canGetLocation = true;
		    // if GPS Enabled get lat/long using GPS Services
		    if (isGPSEnabled) {
			if (location == null) {
			    locationManager.requestLocationUpdates(
				    LocationManager.GPS_PROVIDER, minTime,
				    minDistance, this);
			    Log.d("setupGPS", "GPS enabled");
			    if (status == Constant.TRACKING_START) {
				if (locationManager != null) {
				    location = locationManager
					    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
				    if (location != null) {
					// get start Location of athlete
					// if (!mGpsStart) {
					// mPrevLocate = new
					// LatLng(location.getLatitude(),
					// location.getLongitude());
					// mCurLocate = new
					// LatLng(location.getLatitude(),
					// location.getLongitude());
					// }
				    }
				}
			    }
			}
		    } else if (isNetworkEnabled) {
			locationManager.requestLocationUpdates(
				LocationManager.NETWORK_PROVIDER, minTime,
				minDistance, this);
			Log.d("setupGPS", "Network enabled");
			if (status == Constant.TRACKING_START) {
			    if (locationManager != null) {
				location = locationManager
					.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (location != null) {
				    // get start Location of athlete
				    // mPrevLocate = new
				    // LatLng(location.getLatitude(),
				    // location.getLongitude());
				    // mCurLocate = new
				    // LatLng(location.getLatitude(),
				    // location.getLongitude());
				}

			    }
			}
		    }

		}

	    } catch (Exception e) {
		e.printStackTrace();
	    }

	    return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in
	 * your app
	 * */
	public void stopUsingGPS(boolean isClear) {
	    if (locationManager != null) {
		locationManager.removeUpdates(this);
		if (isClear == true) {
		    // reset all
		    resetGUI(mUnit);
		} else {

		}
	    }
	}

	/**
	 * Reset GUI and all parameter
	 * 
	 * */
	private void resetGUI(int unit) {
	    if (mGoogleMap != null)
		mGoogleMap.clear();
	    // mListPoint.clear();
	    mTrackingStatus = false;
	    mZoomMapStatus = false;
	    // mDistance = 0;
	    // mSpeed = 0;
	    if (unit == Constant.UNIT_KMH) {
		mTxtDistance.setText("" + 0 + " km");
		mTxtSpeed.setText("" + 0 + " km/h");
	    } else if (unit == Constant.UNIT_MPH) {
		mTxtDistance.setText("" + 0 + " mi");
		mTxtSpeed.setText("" + 0 + " mph");
	    } else {
		mTxtDistance.setText("" + 0 + " m");
		mTxtSpeed.setText("" + 0 + " m/sec");
	    }
	    txtClockFace.setText("00:00:00");

	    // Load Football field
	    Bitmap mutableBitmap = mDraw.drawHeadMapPos(mListPoint,
		    Color.TRANSPARENT, Color.TRANSPARENT);
	    if (mImgHeatMap != null)
		mImgHeatMap.setImageBitmap(mutableBitmap);
	}

	/**
	 * Function to calculate the mDistance which player run using Haversine
	 * formula
	 * 
	 * @return the mDistance
	 * */
	public double calcDistance(double lngStart, double latStart,
		double lngCur, double latCur) {
	    double dist = 0.0f;
	    double deltaLat, deltaLon;

	    double earthRadius = 6371; // Radius of Earth
	    deltaLat = Math.toRadians(latCur - latStart);
	    deltaLon = Math.toRadians(lngCur - lngStart);
	    double sindLat = Math.sin(deltaLat / 2);
	    double sindLng = Math.sin(deltaLon / 2);
	    double a = Math.pow(sindLat, 2) + Math.pow(sindLng, 2)
		    * Math.cos(Math.toRadians(latStart))
		    * Math.cos(Math.toRadians(latCur));
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
	    dist = earthRadius * c * 1000;
	    return dist;
	}

	/**
	 * Function to return mDistance between 2 points in Google map
	 * 
	 */
	public double getDistance() {

	    return mDistance;
	}

	@Override
	public void onLocationChanged(Location location) {

	    if (!mGpsStart) {
		updateMapViewLocation(location);
		mFirstLocate = new LatLng(location.getLatitude(),
			location.getLongitude());
		return;
	    }
	    Log.d("onLocationChanged", "Location is changed");
	    mCurLocate = new LatLng(location.getLatitude(),
		    location.getLongitude());
	    mListPoint.add(mCurLocate);

	    // accumulate the runs which player made
	    float[] results = new float[3];
	    // mDistance = mGPSTrack.calcDistance(mPrevLocate.latitude,
	    // mPrevLocate.longitude, mCurLocate.latitude,
	    // mCurLocate.longitude);
	    // Location.distanceBetween(mPrevLocate.latitude,
	    // mPrevLocate.longitude, mCurLocate.latitude, mCurLocate.longitude,
	    // results);
	    if (mPrevLocate != null) {
		results[0] = (float) mGPSTrack.calcDistance(
			mPrevLocate.latitude, mPrevLocate.longitude,
			mCurLocate.latitude, mCurLocate.longitude);
	    }
	    if (mUnit == Constant.UNIT_KMH) {
		mDistance = mDistance + results[0] / 1000;

	    } else if (mUnit == Constant.UNIT_MPH) {
		mDistance = mDistance + results[0] / 1609.34;
		// mDistance = mDistance/1609.34 + results[0]/1609.34;
	    } else {
		mDistance = mDistance + results[0];
	    }

	    // current location become prev location in next update
	    mPrevLocate = new LatLng(location.getLatitude(),
		    location.getLongitude());
	    String Text = "My start location is: " + "Latitud = "
		    + mPrevLocate.latitude + "\nLongitud = "
		    + mPrevLocate.longitude;
	    Log.d("onLocationChanged", Text);

	    Text = "My current location is: " + "Latitud = "
		    + mCurLocate.latitude + "\nLongitud = "
		    + mCurLocate.longitude;
	    Log.d("onLocationChanged", Text);

	    // displayLocation(mPrevLocate);
	    RunData rdata = new RunData();

	    if (mPrevTimeStamp == 0) {
		rdata.timeStamp = location.getTime();
		mPrevTimeStamp = rdata.timeStamp;
	    } else {
		long curTimest = location.getTime();
		rdata.detaDistance = results[0];
		rdata.detaSpeed = 1000 * results[0]
			/ (curTimest - mPrevTimeStamp);
		// display current speed;
		mSpeed = rdata.detaSpeed;
		if (mUnit == Constant.UNIT_KMH) {
		    rdata.detaSpeed = (float) (rdata.detaSpeed * 3.6);
		    rdata.detaDistance = rdata.detaDistance / 1000;
		} else if (mUnit == Constant.UNIT_MPH) {
		    rdata.detaSpeed = (float) (rdata.detaSpeed * 2.23694);
		    rdata.detaDistance = rdata.detaDistance / 1604;
		} else {

		}
		Log.d("PRDCV", "rundata speed1: " + rdata.detaSpeed
			+ " distance = " + rdata.detaDistance);

		int lenght = mRundata.size();
		if (lenght >= 3) {
		    rdata.detaSpeed = (rdata.detaSpeed
			    + mRundata.get(lenght - 1).detaSpeed
			    + mRundata.get(lenght - 2).detaSpeed + mRundata
			    .get(lenght - 3).detaSpeed) / 3;
		}

		if (rdata.detaSpeed > mThreshold[3]) {
		    rdata.thresoldType = Constant.THRESH_TYPE_D;
		} else if (rdata.detaSpeed > mThreshold[2]) {
		    rdata.thresoldType = Constant.THRESH_TYPE_C;
		} else if (rdata.detaSpeed > mThreshold[1]) {
		    rdata.thresoldType = Constant.THRESH_TYPE_B;
		} else if (rdata.detaSpeed > mThreshold[0]) {
		    rdata.thresoldType = Constant.THRESH_TYPE_A;
		} else
		    rdata.thresoldType = 0;

		rdata.timeStamp = curTimest;
		mPrevTimeStamp = curTimest;
		Log.d("PRDCV", "rundata speed: " + rdata.detaSpeed
			+ " distance = " + rdata.detaDistance);
		mRundata.add(rdata);

		displayLocation(mCurLocate);
	    }

	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	@Override
	public IBinder onBind(Intent arg0) {
	    return null;
	}
    }

    private RunnerTrack processRunData() {
	RunnerTrack runTrack = new RunnerTrack();

	runTrack.setDistance(mDistance);
	runTrack.timeText = (String) mChron.getText();
	runTrack.setmListPoint(mListPoint);

	int preInSprint = 0;
	Log.d("PRDCV sprint", "Size = " + mRundata.size() + " distance: "
		+ mDistance);
	// Sprint sprint = new Sprint();
	for (int i = 1; i < mRundata.size(); i++) {
	    if (mRundata.get(i).thresoldType != 0) {
		if (preInSprint != mRundata.get(i).thresoldType) {
		    Log.d("PRDCV sprint", "Start sprint at: " + i + " type: "
			    + mRundata.get(i).thresoldType + " time: "
			    + mRundata.get(i).timeStamp);
		    // add new sprint
		    preInSprint = mRundata.get(i).thresoldType;
		    long starttime = mRundata.get(i - 1).timeStamp;
		    double distance = 0;
		    int j;
		    for (j = i; j < mRundata.size() - 1; j++) {
			Log.d("PRDCV sprint content", "sprint " + j + " time: "
				+ mRundata.get(i).timeStamp);

			if (mRundata.get(j).thresoldType == mRundata.get(j + 1).thresoldType)
			    distance = distance + mRundata.get(j).detaDistance;
			else {
			    distance = distance + mRundata.get(j).detaDistance;
			    i = j;
			    break;
			}
		    }
		    Log.d("PRDCV sprint", "End sprint at: " + j + " type: "
			    + mRundata.get(j).thresoldType + " time: "
			    + mRundata.get(j).timeStamp);

		    i = j;

		    Sprints sprint = new Sprints();
		    sprint.interval = mRundata.get(j).timeStamp - starttime;
		    sprint.mDistance = distance;
		    sprint.mSprintType = preInSprint;
		    sprint.thresh = mThreshold[mRundata.get(i).thresoldType];
		    runTrack.addSprint(sprint);

		}
	    } else {
		preInSprint = 0;
	    }
	}

	return runTrack;
    }

    @Override
    public void onRequestResult(String command, String[] array) {
	// TODO Auto-generated method stub
	if (command.equals(Constant.COMMAND_LISTFILE)) {
	    if (mProgressDialog != null)
		mProgressDialog.dismiss();
	    if (array == null) {
		new AlertDialog.Builder(this)
			.setMessage("Can not get list file")
			.setPositiveButton("OK", null).show();
	    } else
		showListFileDialog(array);
	} else if (command.equals(Constant.COMMAND_DOWNLOAD)) {
	    if (mProgressDialog != null)
		mProgressDialog.dismiss();
	    updateRuntrackUI(array[0]);
	} else if (command.equals(Constant.COMMAND_CONNECT)) {
	    if (array[0].equalsIgnoreCase("1")) {
		mPrefUtil.saveLoginStatus(true);
		showHistoryListTrack();
	    } else {
		mPrefUtil.saveLoginStatus(false);
		Toast.makeText(this, "Login fail !", Toast.LENGTH_LONG).show();
	    }
	}

    }

    private void updateRuntrackUI(String json) {

	RunnerTrack runtrack = fromJsontoRunTrack(json);
	if (runtrack != null) {
	    ((RunApplication) getApplication()).runnerTrack = runtrack;
	    // start acitivity
	    Intent intent = new Intent(this, ViewRunningActivity.class);
	    intent.putExtra("mMapType", mMapType);
	    intent.putExtra("mDateActivity", mDateActivity);
	    intent.putExtra("mTimeActivity", mTimeActivity);
	    intent.putExtra("state", Constant.VIEW_HISTORY);
	    // intent.putExtra("mThreshA",runtrack.thresh[Constant.THRESH_TYPE_A]);
	    // intent.putExtra("mThreshB",runtrack.thresh[Constant.THRESH_TYPE_B]);
	    // intent.putExtra("mThreshC",runtrack.thresh[Constant.THRESH_TYPE_C]);
	    // intent.putExtra("mThreshD",runtrack.thresh[Constant.THRESH_TYPE_D]);
	    startActivity(intent);
	} else {
	    // handle error here
	    Toast.makeText(this, "Can not load the track", Toast.LENGTH_LONG)
		    .show();
	}
    }

    private RunnerTrack fromJsontoRunTrack(String json) {
	Gson gson = new Gson();
	RunnerTrack runTrack = gson.fromJson(json, RunnerTrack.class);
	return runTrack;
    }

    String mFileList[];

    private void showListFileDialog(String[] filelist) {
	mFileList = filterFile(filelist);

	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Select your running activity");
	builder.setPositiveButton("OK", null);
	builder.setItems(mFileList, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int item) {
		loadRuntrack(item);
		// parser File name
		String hisFile = mFileList[item];
		String[] splitName = hisFile.split(" ");
		mDateActivity = splitName[0].replace("_", "/");
		String[] timeAct = splitName[1].replace("_", ":")
			.split(".athe");
		mTimeActivity = timeAct[0];

	    }
	});

	AlertDialog alert = builder.create();
	alert.show();

    }

    private void loadRuntrack(int id) {
	String commandParam[] = new String[3];
	commandParam[0] = Constant.COMMAND_DOWNLOAD;
	commandParam[1] = mFileList[id];
	mFtpServerTask = new ServerTask(this, this);
	mFtpServerTask.execute(commandParam);

	mProgressDialog = new ProgressDialog(this);
	mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	mProgressDialog.setMessage("Loading file !");
	mProgressDialog.show();

    }

    private String[] filterFile(String[] listfile) {
	ArrayList<String> filterList = new ArrayList<String>();

	for (int i = 0; i < listfile.length; i++) {
	    if (listfile[i].length() > 4) {
		if (listfile[i].substring(listfile[i].length() - 4,
			listfile[i].length()).equals("athe")) {
		    filterList.add(listfile[i]);
		}

	    }
	}

	return filterList.toArray(new String[filterList.size()]);
    }

    private void enableGPS() {
	Intent intent = new Intent(
		android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
	startActivity(intent);
    }

    private boolean CheckEnableGPS() {
	String provider = Settings.Secure.getString(getContentResolver(),
		Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
	LocationManager locationManager = (LocationManager) this
		.getSystemService(LOCATION_SERVICE);
	boolean isGPSEnabled = locationManager
		.isProviderEnabled(LocationManager.GPS_PROVIDER);

	// getting network status
	boolean isNetworkEnabled = locationManager
		.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

	if (isGPSEnabled || isNetworkEnabled) {
	    // GPS Enabled
	    // Toast.makeText(this, "GPS Enabled: " + provider,
	    // Toast.LENGTH_LONG).show();
	    return true;
	} else {
	    return false;
	    // Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
	    // startActivity(intent);
	}

    }

    private String getUnitName(int unittype) {
	if (unittype == Constant.UNIT_KMH) {
	    return "(km/h)";
	} else if (unittype == Constant.UNIT_MPH)
	    return "(Mph)";
	else
	    return "(m/sec)";

    }

    private boolean showUserLogin() {
	final Dialog dialog = new Dialog(this,
		android.R.style.Theme_Translucent);
	dialog.setContentView(R.layout.userlogin);
	dialog.show();

	Button btnSaveSetting = (Button) dialog
		.findViewById(R.id.btn_savesetting);
	Button btnCancelSetting = (Button) dialog
		.findViewById(R.id.btncancelsetting);
	dialog.setCancelable(true);

	final EditText editFTPUser = (EditText) dialog
		.findViewById(R.id.usernametext);
	final EditText editFTPPass = (EditText) dialog
		.findViewById(R.id.passwordtext);

	btnSaveSetting.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {
		mPrefUtil.saveFTPPass(editFTPPass.getText().toString());
		mPrefUtil.saveFTPUserName(editFTPUser.getText().toString());

		mFtpServerTask = new ServerTask(MainActivity.this,
			MainActivity.this);
		String commandstr[] = new String[3];
		commandstr[0] = Constant.COMMAND_CONNECT;
		mFtpServerTask.execute(commandstr);
		dialog.dismiss();
	    }
	});

	btnCancelSetting.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View v) {

		dialog.dismiss();
	    }
	});

	return false;
    }

    private boolean checkLoginStatus() {
	return mPrefUtil.getLoginStatus();

    }

    private void openFileLocal() {
	FileManager filemng = new FileManager(this);
	String listFile[] = filemng.getListFile();

	final String mFileListlocal[] = filterFile(listFile);

	AlertDialog.Builder builder = new AlertDialog.Builder(this);
	builder.setTitle("Select your running activity");
	builder.setPositiveButton("OK", null);
	builder.setItems(mFileListlocal, new DialogInterface.OnClickListener() {
	    public void onClick(DialogInterface dialog, int item) {
		loadRunTrackLocal(mFileListlocal[item]);
		// parser File name
		String hisFile = mFileListlocal[item];
		String[] splitName = hisFile.split(" ");
		mDateActivity = splitName[0].replace("_", "/");
		String[] timeAct = splitName[1].replace("_", ":")
			.split(".athe");
		mTimeActivity = timeAct[0];
	    }
	});

	AlertDialog alert = builder.create();
	alert.show();
    }

    private void loadRunTrackLocal(String fileName) {
	FileManager filemng = new FileManager(this);
	String data = filemng.readFromFile(fileName);
	updateRuntrackUI(data);
    }

    private void openFileServer() {
	if (checkLoginStatus()) {
	    String commandstr[] = new String[3];
	    commandstr[0] = Constant.COMMAND_LISTFILE;
	    mFtpServerTask = new ServerTask(this, this);
	    mFtpServerTask.execute(commandstr);

	    mProgressDialog = new ProgressDialog(this);
	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    mProgressDialog.setMessage("Please wait list file!");
	    mProgressDialog.show();
	} else {
	    showUserLogin();
	}
    }

    public void updateMapViewLocation(Location location) {
	if (mZoomMapStatus == false && mGoogleMap != null) {
	    Log.d("displayLocation", "Load map");
	    LatLng locate = new LatLng(location.getLatitude(),
		    location.getLongitude());
	    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locate,
		    Constant.DEFAULT_ZOOM_LEVEL));
	    mGoogleMap.animateCamera(
		    CameraUpdateFactory.zoomTo(Constant.DEFAULT_ZOOM_LEVEL),
		    2000, null);
	    mZoomMapStatus = true;

	    // MarkerOptions maker = new MarkerOptions().position(locate)
	    // .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
	    // mMarkPos = mGoogleMap.addMarker(maker);
	    MarkerOptions markPoint = new MarkerOptions().position(locate)
		    .icon(BitmapDescriptorFactory
			    .fromResource(R.drawable.ic_marker_first));
	    mMarkPos = mGoogleMap.addMarker(markPoint);
	    mMarkPos.showInfoWindow();

	}
    }
}
