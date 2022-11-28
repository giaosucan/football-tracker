package com.android.gps.gui;

/**
 * Display list of sprints which player made after football match
 */

import java.util.ArrayList;
import java.util.List;

import com.android.gps.calc.Sprints;
import com.android.gps.main.R;
import com.android.gps.util.Constant;
import com.android.gps.util.PreferentUtils;

import android.app.Activity;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class SprintExpandedAdapter extends BaseExpandableListAdapter {

    private SparseArray<Group> groups = new SparseArray<Group>();
    private ArrayList<Sprints> mSprintList;
    public LayoutInflater inflater;
    public Activity activity;
    double threshA, threshB, threshC, threshD;
    PreferentUtils mPrefUtil;
    int mUnit;

    public SprintExpandedAdapter(Activity act, SparseArray<Group> groups) {
	activity = act;
	// this.groups = groups;
	inflater = act.getLayoutInflater();

    }

    public SprintExpandedAdapter(Activity act, ArrayList<Sprints> sprintlist,
	    double thresh[], int state) {

	activity = act;
	// this.groups = groups;
	mSprintList = sprintlist;
	inflater = act.getLayoutInflater();
	mPrefUtil = new PreferentUtils(act);

	if (state == Constant.VIEW_CURRENT) {
	    threshA = mPrefUtil.getThresholdValue(Constant.THRESHOLD_A,
		    Constant.THRESH_TYPE_A);
	    threshB = mPrefUtil.getThresholdValue(Constant.THRESHOLD_B,
		    Constant.THRESH_TYPE_B);
	    threshC = mPrefUtil.getThresholdValue(Constant.THRESHOLD_C,
		    Constant.THRESH_TYPE_C);
	    threshD = mPrefUtil.getThresholdValue(Constant.THRESHOLD_D,
		    Constant.THRESH_TYPE_D);
	} else if (state == Constant.VIEW_HISTORY) {
	    threshA = thresh[Constant.THRESH_TYPE_A];
	    threshB = thresh[Constant.THRESH_TYPE_B];
	    threshC = thresh[Constant.THRESH_TYPE_C];
	    threshD = thresh[Constant.THRESH_TYPE_D];

	}
	threshA = Math.round(threshA * 100.0) / 100.0;
	threshB = Math.round(threshB * 100.0) / 100.0;
	threshC = Math.round(threshC * 100.0) / 100.0;
	threshD = Math.round(threshD * 100.0) / 100.0;

	groups.append(0, new Group("Sprint type A -- Threshold: " + threshA));
	groups.append(1, new Group("Sprint type B -- Threshold: " + threshB));
	groups.append(2, new Group("Sprint type C -- Threshold: " + threshC));
	groups.append(3, new Group("Sprint type D -- Threshold: " + threshD));

	for (Sprints sprint : sprintlist) {
	    if (sprint.mSprintType == Constant.THRESH_TYPE_A)
		groups.get(0).children.add(sprint);
	    else if (sprint.mSprintType == Constant.THRESH_TYPE_B)
		groups.get(1).children.add(sprint);
	    if (sprint.mSprintType == Constant.THRESH_TYPE_C)
		groups.get(2).children.add(sprint);
	    if (sprint.mSprintType == Constant.THRESH_TYPE_D)
		groups.get(3).children.add(sprint);
	}
	if (((RunApplication) act.getApplication()).runnerTrack != null)
	    mUnit = ((RunApplication) act.getApplication()).runnerTrack.unitType;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
	return groups.get(groupPosition).children.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
	return 0;
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition,
	    boolean isLastChild, View convertView, ViewGroup parent) {
	final Sprints children = (Sprints) getChild(groupPosition,
		childPosition);
	if (convertView == null) {
	    convertView = inflater.inflate(R.layout.sprintitem, null);
	}

	TextView sprintDistancetext = (TextView) convertView
		.findViewById(R.id.sprintDistancetext);
	if (mUnit == Constant.UNIT_KMH)
	    sprintDistancetext.setText(String
		    .format("%.3f", children.mDistance) + "km");
	else if (mUnit == Constant.UNIT_MPH)
	    sprintDistancetext.setText(String
		    .format("%.3f", children.mDistance) + "mi");
	else
	    sprintDistancetext.setText(String
		    .format("%.2f", children.mDistance) + "m");

	TextView sprintTime = (TextView) convertView
		.findViewById(R.id.durationitemtext);

	sprintTime.setText(formatTime((children.interval)));

	// text.setText(children);
	convertView.setOnClickListener(new OnClickListener() {
	    @Override
	    public void onClick(View v) {
		Toast.makeText(activity, "you click on id " + childPosition,
			Toast.LENGTH_SHORT).show();
	    }
	});
	return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
	return groups.get(groupPosition).children.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
	return groups.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
	return groups.size();
    }

    @Override
    public void onGroupCollapsed(int groupPosition) {
	super.onGroupCollapsed(groupPosition);
    }

    @Override
    public void onGroupExpanded(int groupPosition) {
	super.onGroupExpanded(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
	return 0;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
	    View convertView, ViewGroup parent) {
	if (convertView == null) {
	    convertView = inflater.inflate(R.layout.sprintcategory, null);
	}
	Group group = (Group) getGroup(groupPosition);
	TextView text = (TextView) convertView
		.findViewById(R.id.textSprintType);

	text.setText(group.string);
	// ((CheckedTextView) convertView).setChecked(isExpanded);
	return convertView;
    }

    @Override
    public boolean hasStableIds() {
	return false;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
	return false;
    }

    private String formatTime(long time) {
	String duration;
	long minutes = (time / 1000) / 60;
	long second = (time / 1000) % 60;
	duration = minutes + ":" + second;
	return duration;

    }

    public class Group {

	public String string;
	// public final List<String> children = new ArrayList<String>();
	public final List<Sprints> children = new ArrayList<Sprints>();

	public Group(String string) {
	    this.string = string;
	}

    }
}
