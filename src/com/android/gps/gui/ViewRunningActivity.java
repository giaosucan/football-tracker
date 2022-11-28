package com.android.gps.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import com.android.gps.calc.Sprints;
import com.android.gps.facebook.ShareFacebookActivity;
import com.android.gps.heatmap.DrawHeatMap;
import com.android.gps.main.App;
import com.android.gps.main.MainActivity;
import com.android.gps.main.R;
import com.android.gps.server.FileManager;
import com.android.gps.server.ServerTask;
import com.android.gps.server.ServerTask.FTPRequestResult;
import com.android.gps.util.Constant;
import com.android.gps.util.PreferentUtils;
import com.android.gps.util.RunnerTrack;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;

public class ViewRunningActivity extends FragmentActivity implements
	OnClickListener, FTPRequestResult {

    static public final String ARG_TEXT_ID = "text_id";
    RunnerTrack runTrack;
    ArrayList<Sprints> mSprintList = new ArrayList<Sprints>();
    ServerTask ftpServerTask;
    String mTrackName;
    String mFileList[] = null;
    TextView durationText;
    TextView distanceText;
    TextView avSpeedText;
    GoogleMap mGoogleMap = null;
    TextView mDateText, mTimeText;
    int mMapType;
    TextView sprintText;
    RelativeLayout headmaplayout;
    RelativeLayout googlemaplayout;
    RelativeLayout mapandheadmaplayout;

    private ProgressDialog mProgressDialog = null;
    PreferentUtils prefUtil;
    ViewFlipper mViewFlipper;
    int mUnit;
    boolean openfile;
    Button openBtn;
    Button saveBtn;
    int state;

    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_statistics_view);
	prefUtil = new PreferentUtils(this);

	headmaplayout = (RelativeLayout) findViewById(R.id.heatmapLayout);
	googlemaplayout = (RelativeLayout) findViewById(R.id.googlemap);
	mapandheadmaplayout = (RelativeLayout) findViewById(R.id.mapandheadmaplayout);
	sprintText = (TextView) findViewById(R.id.sprintText);
	sprintText.setOnClickListener(sprintClick);
	// googlemaplayout.setOnTouchListener(touchlistener);
	// mUnit = ((RunApplication)getApplication()).mUnitType;
	runTrack = ((RunApplication) getApplication()).runnerTrack;

	mViewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
	durationText = (TextView) findViewById(R.id.durationtext);
	distanceText = (TextView) findViewById(R.id.distancetext);
	avSpeedText = (TextView) findViewById(R.id.avspeedtext);

	String DateActivity = null;
	String TimeActivity = null;
	Bundle extras = getIntent().getExtras();
	if (extras != null) {
	    DateActivity = extras.getString("mDateActivity");
	    TimeActivity = extras.getString("mTimeActivity");
	    mMapType = extras.getInt("mMapType");
	    state = extras.getInt("state");
	}

	// durationText.setText(String.valueOf(runTrack.getDuration()));
	if (runTrack != null) {
	    mUnit = runTrack.unitType;
	    durationText.setText(runTrack.timeText);

	    String time[] = runTrack.timeText.split(":");
	    long timeDuration = getSecond(time);
	    double avSpeed = runTrack.getDistance() / timeDuration;
	    if (mUnit == Constant.UNIT_KMH) {
		avSpeed = avSpeed * 3600;
		distanceText.setText(String.format("%.2f",
			runTrack.getDistance())
			+ "km");
		avSpeedText.setText(String.format("%.2f", avSpeed) + "km/h");
	    } else if (mUnit == Constant.UNIT_MS) {
		distanceText.setText(String.format("%.2f",
			runTrack.getDistance())
			+ "m");
		avSpeedText.setText(String.format("%.2f", avSpeed) + "m/sec");
	    } else if (mUnit == Constant.UNIT_MPH) {
		// avSpeed = avSpeed * 3600;
		avSpeed = avSpeed * 3600 / 1604;
		distanceText.setText(String.format("%.2f",
			runTrack.getDistance())
			+ "mi");
		avSpeedText.setText(String.format("%.2f", avSpeed) + "Mph");
	    }

	    mSprintList = runTrack.getListSprint();

	}

	ExpandableListView listView = (ExpandableListView) findViewById(R.id.listsprint);
	try {
	    SprintExpandedAdapter adapter = new SprintExpandedAdapter(this,
		    mSprintList, runTrack.thresh, state);
	    listView.setAdapter(adapter);
	    listView.setOnTouchListener(lisener);
	} catch (Exception e) {
	    e.printStackTrace();
	}

	saveBtn = (Button) findViewById(R.id.savebtn);
	saveBtn.setOnClickListener(this);
	openBtn = (Button) findViewById(R.id.openbtn);
	openBtn.setOnClickListener(this);
	Button cancel = (Button) findViewById(R.id.cancelbtn);
	cancel.setOnClickListener(this);
	Button share = (Button) findViewById(R.id.sharebtn);
	share.setOnClickListener(this);

	mTrackName = getFileNameByTime();
	String currentTime[] = getCurrentTime();
	mDateText = (TextView) findViewById(R.id.dateitemtext);
	if (DateActivity == null) {
	    mDateText.setText(currentTime[0]);
	} else {
	    mDateText.setText(DateActivity);
	}

	mTimeText = (TextView) findViewById(R.id.timeitemtext);

	if (TimeActivity == null) {
	    mTimeText.setText(currentTime[1]);
	} else {
	    mTimeText.setText(TimeActivity);
	}
	displayLocation();
    }

    private ExpandableListView.OnTouchListener lisener = new ExpandableListView.OnTouchListener() {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    int action = event.getAction();
	    switch (action) {
	    case MotionEvent.ACTION_DOWN:
		// Disallow ScrollView to intercept touch events.
		v.getParent().requestDisallowInterceptTouchEvent(true);
		break;

	    case MotionEvent.ACTION_UP:
		// Allow ScrollView to intercept touch events.
		v.getParent().requestDisallowInterceptTouchEvent(false);
		break;
	    }

	    // Handle ListView touch events.
	    v.onTouchEvent(event);
	    return true;
	}

    };

    @Override
    public void onBackPressed() {
	// your code.
	finish();
	Intent intent = new Intent(this, MainActivity.class);
	startActivity(intent);
    }

    @Override
    protected void onStart() {
	super.onStart();
	App.ViewRunningActivity = this;
    };

    @Override
    protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	App.ViewRunningActivity = null;
    }

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
		String dateActivity = splitName[0].replace("_", "/");
		String timeActivity = splitName[1].replace("_", ":");
		String[] timeAct = timeActivity.split(".athe");
		mDateText.setText(dateActivity);
		mTimeText.setText(timeAct[0]);
	    }
	});

	AlertDialog alert = builder.create();
	alert.show();

    }

    private long getSecond(String[] times) {
	if (times.length == 1)
	    return Integer.valueOf(times[0]);
	else if (times.length == 2)
	    return Integer.valueOf(times[0]) * 60 + Integer.valueOf(times[1]);
	else if (times.length == 2)
	    return Integer.valueOf(times[0]) * 60 * 60
		    + Integer.valueOf(times[1]) * 60
		    + Integer.valueOf(times[2]) * 60;
	else
	    return 0;
    }

    private String writeJsonObject() {
	Gson gson = new Gson();
	String json = gson.toJson(runTrack);
	// fromJsontoRunTrack(json);
	Log.d("PRDCV jSon", json);
	return json;
    }

    @Override
    public void onClick(View v) {
	// TODO Auto-generated method stub
	switch (v.getId()) {
	case R.id.savebtn:
	    // saveFileServer();
	    new AlertDialog.Builder(this)
		    .setTitle("Please select ")
		    .setMessage("Save run track to local or server !")
		    .setPositiveButton("Server",
			    new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
					int which) {
				    // TODO Auto-generated method stub
				    saveFileServer();
				}
			    })
		    .setNeutralButton("Local",
			    new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog,
					int which) {
				    // TODO Auto-generated method stub
				    saveFileLocal();
				}
			    }).setNegativeButton("Cancel", null).show();

	    break;

	case R.id.openbtn:
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

	    break;

	case R.id.cancelbtn:
	    finish();
	    Intent intent = new Intent(this, MainActivity.class);
	    startActivity(intent);
	    break;
	case R.id.sharebtn:
	    // do sharing here
	    intent = new Intent(this, ShareFacebookActivity.class);
	    intent.putExtra("distance", distanceText.getText());
	    intent.putExtra("duration", durationText.getText());
	    intent.putExtra("speed", avSpeedText.getText());
	    intent.putExtra("totalSprint", mSprintList.size());
	    // intent.putExtra("distance", runTrack.getDistance());
	    startActivity(intent);
	    break;
	}
    }

    private RunnerTrack fromJsontoRunTrack(String json) {
	Gson gson = new Gson();
	RunnerTrack runTrack = gson.fromJson(json, RunnerTrack.class);
	return runTrack;
    }

    @Override
    public void onRequestResult(String command, String[] array) {
	// TODO Auto-generated method stub
	if (command.equals(Constant.COMMAND_UPLOAD)) {
	    if (mProgressDialog != null)
		mProgressDialog.dismiss();

	    if (array[0].equals("0")) {
		Toast.makeText(this, "Upload file fail !", Toast.LENGTH_LONG)
			.show();

	    } else {
		Toast.makeText(this, "Upload file successfully !",
			Toast.LENGTH_LONG).show();
	    }

	} else if (command.equals(Constant.COMMAND_LISTFILE)) {
	    if (mProgressDialog != null)
		mProgressDialog.dismiss();
	    if (array == null) {
		new AlertDialog.Builder(this)
			.setMessage("Can not get list file")
			.setPositiveButton("OK", null).show();
	    } else
		showListFileDialog(array);
	}

	else if (command.equals(Constant.COMMAND_DOWNLOAD)) {
	    if (mProgressDialog != null)
		mProgressDialog.dismiss();
	    updateRuntrackUI(array[0]);
	}

	else if (command.equals(Constant.COMMAND_CONNECT)) {
	    if (array[0].equalsIgnoreCase("1")) {
		prefUtil.saveLoginStatus(true);
		// login successfull
		if (openfile)
		    openBtn.performClick();
		else
		    saveBtn.performClick();
	    } else {
		// login fail
		Toast.makeText(this, "Login fail !", Toast.LENGTH_LONG).show();
		prefUtil.saveLoginStatus(false);

	    }
	}
    }

    private String getFileNameByTime() {
	SimpleDateFormat dfDate = new SimpleDateFormat("yyyy_MMM_dd HH_mm_ss");
	String data = "";
	Calendar c = Calendar.getInstance();
	data = dfDate.format(c.getTime());
	Log.d("prdcv ", "name file: " + data);
	return data + ".athe";

    }

    private String[] getCurrentTime() {
	String time[] = new String[2];
	SimpleDateFormat dfDate = new SimpleDateFormat("yyyy/MMM/dd");
	SimpleDateFormat timehour = new SimpleDateFormat("HH:mm:ss");
	Calendar c = Calendar.getInstance();
	time[0] = dfDate.format(c.getTime());
	time[1] = timehour.format(c.getTime());
	return time;

    }

    private void loadRuntrack(int id) {
	String commandParam[] = new String[3];
	commandParam[0] = Constant.COMMAND_DOWNLOAD;
	commandParam[1] = mFileList[id];
	ftpServerTask = new ServerTask(this, this);
	ftpServerTask.execute(commandParam);

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

    private void updateRuntrackUI(String json) {
	runTrack = fromJsontoRunTrack(json);
	((RunApplication) getApplication()).runnerTrack = runTrack;
	if (runTrack == null) {
	    Toast.makeText(this, "Your track is empty", Toast.LENGTH_LONG)
		    .show();
	    return;
	}
	durationText.setText(runTrack.timeText);
	// distanceText.setText(String.format("%.2f",runTrack.getDistance()));

	String time[] = runTrack.timeText.split(":");
	long timeDuration = getSecond(time);

	double avSpeed = runTrack.getDistance() / timeDuration;
	//
	// avSpeedText.setText(String.format("%.2f",avSpeed));
	if (mUnit == Constant.UNIT_KMH) {
	    avSpeed = avSpeed * 3600;
	    distanceText.setText(String.format("%.3f", runTrack.getDistance())
		    + "km");
	    avSpeedText.setText(String.format("%.3f", avSpeed) + "km/h");
	} else if (mUnit == Constant.UNIT_MS) {
	    distanceText.setText(String.format("%.2f", runTrack.getDistance())
		    + "m");
	    avSpeedText.setText(String.format("%.2f", avSpeed) + "m/sec");
	} else if (mUnit == Constant.UNIT_MPH) {
	    // avSpeed = avSpeed * 3600;
	    avSpeed = avSpeed * 3600 / 1604;
	    distanceText.setText(String.format("%.3f", runTrack.getDistance())
		    + "mi");
	    avSpeedText.setText(String.format("%.3f", avSpeed) + "Mph");
	}

	displayLocation();
	mSprintList = runTrack.getListSprint();
	ExpandableListView listView = (ExpandableListView) findViewById(R.id.listsprint);
	SprintExpandedAdapter adapter = new SprintExpandedAdapter(this,
		mSprintList, runTrack.thresh, state);
	listView.setAdapter(adapter);
	listView.invalidate();

    }

    DrawHeatMap mDraw;
    ImageView mImgHeatMap;
    boolean change_map = false;

    private void displayLocation() {
	if (change_map) {
	    showMapView();
	    return;
	}

	mDraw = new DrawHeatMap(this, R.drawable.img_football_field);

	mImgHeatMap = (ImageView) findViewById(R.id.heat_map_view);
	mImgHeatMap.setAdjustViewBounds(true);
	if (runTrack != null) {
	    mDraw.setHeadMapLocation(runTrack.htopleftlat,
		    runTrack.htopleftlon, runTrack.htoprightlat,
		    runTrack.htoprightlon, runTrack.hbottomleftlat,
		    runTrack.hbottomleftlon);

	    Bitmap mutableBitmap = mDraw.drawHeadMapPos(
		    runTrack.getmListPoint(), Color.BLUE, Color.RED);
	    mImgHeatMap.setImageBitmap(mutableBitmap);
	} else
	    mImgHeatMap.setImageResource(R.drawable.img_football_field);

	mImgHeatMap.invalidate();

    }

    private void showMapView() {
	try {
	    runTrack = ((RunApplication) getApplication()).runnerTrack;
	    if (runTrack == null)
		return;
	    // if map has not already loaded
	    // if (mGoogleMap == null) {
	    Log.d("displayLocation", "Load map");
	    FragmentManager fmanager = getSupportFragmentManager();
	    Fragment fragment = fmanager.findFragmentById(R.id.headmap);
	    mGoogleMap = ((SupportMapFragment) fragment).getMap();

	    mGoogleMap.getUiSettings().setZoomControlsEnabled(true);
	    mGoogleMap.getUiSettings().setCompassEnabled(true);
	    mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
	    mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
	    mGoogleMap.setTrafficEnabled(true);
	    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(runTrack
		    .getmListPoint().get(0), Constant.DEFAULT_ZOOM_LEVEL));
	    mGoogleMap.animateCamera(
		    CameraUpdateFactory.zoomTo(Constant.DEFAULT_ZOOM_LEVEL),
		    2000, null);
	    mGoogleMap.setMapType(mMapType);
	    // }

	    mGoogleMap.clear();
	    MarkerOptions markPoint;
	    if (runTrack.getmListPoint().size() > 1) {

		// add the location point
		for (int i = 0; i < runTrack.getmListPoint().size(); i++) {

		    if (i != runTrack.getmListPoint().size() - 1) {
			markPoint = new MarkerOptions().position(
				runTrack.getmListPoint().get(i)).icon(
				BitmapDescriptorFactory
					.fromResource(R.drawable.ic_marker));
		    } else {
			markPoint = new MarkerOptions()
				.position(runTrack.getmListPoint().get(i))
				.icon(BitmapDescriptorFactory
					.fromResource(R.drawable.ic_destination));
		    }
		    mGoogleMap.addMarker(markPoint);
		}

	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public void setListViewHeightBasedOnChildren(ExpandableListView listView) {
	ListAdapter listAdapter = listView.getAdapter();
	if (listAdapter == null) {
	    // pre-condition
	    return;
	}

	int totalHeight = 0;
	for (int i = 0; i < listAdapter.getCount(); i++) {
	    View listItem = listAdapter.getView(i, null, listView);
	    listItem.measure(0, 0);
	    totalHeight += listItem.getMeasuredHeight();
	}

	ViewGroup.LayoutParams params = listView.getLayoutParams();
	params.height = totalHeight
		+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
	listView.setLayoutParams(params);
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
		prefUtil.saveFTPPass(editFTPPass.getText().toString());
		prefUtil.saveFTPUserName(editFTPUser.getText().toString());

		ftpServerTask = new ServerTask(ViewRunningActivity.this,
			ViewRunningActivity.this);
		String commandstr[] = new String[3];
		commandstr[0] = Constant.COMMAND_CONNECT;
		ftpServerTask.execute(commandstr);
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
	return prefUtil.getLoginStatus();

    }

    private void openFileServer() {
	if (checkLoginStatus()) {
	    String commandstr[] = new String[3];
	    commandstr[0] = Constant.COMMAND_LISTFILE;
	    ftpServerTask = new ServerTask(this, this);
	    ftpServerTask.execute(commandstr);

	    mProgressDialog = new ProgressDialog(this);
	    // mProgressDialog.setCancelable(false);
	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    mProgressDialog.setMessage("Please wait list file!");
	    mProgressDialog.show();
	} else {
	    openfile = true;
	    showUserLogin();
	}
    }

    private void saveFileServer() {
	if (checkLoginStatus()) {
	    String json = writeJsonObject();
	    String commandParam[] = new String[3];
	    commandParam[0] = Constant.COMMAND_UPLOAD;
	    commandParam[1] = mTrackName;
	    commandParam[2] = json;
	    ftpServerTask = new ServerTask(this, this);
	    ftpServerTask.execute(commandParam);

	    mProgressDialog = new ProgressDialog(this);
	    // mProgressDialog.setCancelable(false);
	    mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	    mProgressDialog.setMessage("Please wait !");
	    mProgressDialog.show();
	} else {
	    showUserLogin();
	    openfile = false;
	}
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
		String dateActivity = splitName[0].replace("_", "/");
		String timeActivity = splitName[1].replace("_", ":");
		String[] timeAct = timeActivity.split(".athe");
		mDateText.setText(dateActivity);
		mTimeText.setText(timeAct[0]);
	    }
	});

	AlertDialog alert = builder.create();
	alert.show();

    }

    private void saveFileLocal() {
	FileManager filemng = new FileManager(this);

	String content = writeJsonObject();
	filemng.saveToSDCard(mTrackName, content);
    }

    private void loadRunTrackLocal(String fileName) {
	FileManager filemng = new FileManager(this);
	String data = filemng.readFromFile(fileName);
	updateRuntrackUI(data);
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
	menu.add(0, 2, 2, "Switch Map").setIcon(R.drawable.ic_map);
	return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

	if (change_map) {
	    headmaplayout.setVisibility(View.VISIBLE);
	    googlemaplayout.setVisibility(View.GONE);
	    change_map = false;
	    displayLocation();
	} else {
	    headmaplayout.setVisibility(View.GONE);
	    googlemaplayout.setVisibility(View.VISIBLE);
	    change_map = true;
	    displayLocation();

	}

	return true;
    }

    OnTouchListener touchlistener = new OnTouchListener() {

	@Override
	public boolean onTouch(View v, MotionEvent event) {
	    // TODO Auto-generated method stub
	    return false;
	}
    };

    boolean bsprintClick = false;
    OnClickListener sprintClick = new OnClickListener() {

	@Override
	public void onClick(View v) {
	    // TODO Auto-generated method stub
	    if (bsprintClick) {
		sprintText.setBackgroundResource(R.drawable.sprint_border);
		mapandheadmaplayout.setVisibility(View.VISIBLE);
		bsprintClick = false;
	    } else {
		mapandheadmaplayout.setVisibility(View.GONE);
		sprintText
			.setBackgroundResource(R.drawable.sprint_border_click);
		bsprintClick = true;
	    }
	}
    };
}
