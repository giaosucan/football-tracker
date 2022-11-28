package com.android.gps.heatmap;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.view.View;
import java.lang.Math;

import com.android.gps.util.Constant;
import com.android.gps.util.PreferentUtils;
import com.google.android.gms.maps.model.LatLng;

public class DrawHeatMap extends View {
    Bitmap mBitmap;

    // 21.031295&lon=105.781828
    private float topleftlat;
    private float topleftlon;
    // 21.030664&lon=105.785691
    private float toprightlat;
    private float toprightlon;
    // 21.029061&lon=105.781399
    private float bottomleftlat;
    private float bottomleftlon;

    public DrawHeatMap(Context context, int imageId) {
	super(context);
	BitmapFactory.Options myOptions = new BitmapFactory.Options();
	myOptions.inDither = true;
	myOptions.inScaled = false;
	myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
	myOptions.inPurgeable = true;
	mBitmap = BitmapFactory.decodeResource(getResources(), imageId,
		myOptions);

	PreferentUtils prefUtil = new PreferentUtils(context);
	topleftlat = prefUtil.getCoodinate(Constant.TOP_LEFT_LAT);
	topleftlon = prefUtil.getCoodinate(Constant.TOP_LEFT_LON);
	toprightlat = prefUtil.getCoodinate(Constant.TOP_RIGHT_LAT);
	toprightlon = prefUtil.getCoodinate(Constant.TOP_RIGHT_LON);
	bottomleftlat = prefUtil.getCoodinate(Constant.BOTTOM_LEFT_LAT);
	bottomleftlon = prefUtil.getCoodinate(Constant.BOTTOM_LEFT_LON);
    }

    public void setHeadMapLocation(float topleftlat, float topleftlon,
	    float toprightlat, float toprightlon, float bottomleftlat,
	    float bottomleftlon) {
	this.topleftlat = topleftlat;
	this.topleftlon = topleftlon;
	this.toprightlat = toprightlat;
	this.toprightlon = toprightlon;
	this.bottomleftlat = bottomleftlat;
	this.bottomleftlon = bottomleftlon;

    }

    public Bitmap drawHeatPoint(int Color, float xPos, float yPos,
	    float radius, int numPoint) {
	Paint paint = new Paint();
	paint.setAntiAlias(true);
	paint.setColor(Color);
	Bitmap workingBitmap = Bitmap.createBitmap(mBitmap);
	Bitmap mutableBitmap = workingBitmap
		.copy(Bitmap.Config.ARGB_8888, true);
	Canvas canvas = new Canvas(mutableBitmap);
	// Draw Point
	for (int i = 0; i < numPoint; i++) {
	    canvas.drawCircle(xPos, yPos, radius, paint);
	}
	return mutableBitmap;
    }

    public Bitmap drawHeatMap(ArrayList<HeatPoint> listPoint) {

	Bitmap workingBitmap = Bitmap.createBitmap(mBitmap);
	Bitmap mutableBitmap = workingBitmap
		.copy(Bitmap.Config.ARGB_8888, true);

	// Draw Point
	for (int i = 0; i < listPoint.size(); i++) {
	    // Init Paint Object
	    Paint paint = new Paint();
	    paint.setAntiAlias(true);
	    paint.setColor(listPoint.get(i).color);
	    // Init Bitmap

	    Canvas canvas = new Canvas(mutableBitmap);
	    canvas.drawCircle(listPoint.get(i).xPos, listPoint.get(i).yPos,
		    listPoint.get(i).radius, paint);
	    // canvas.drawPoint(listPoint.get(i).xPos, listPoint.get(i).yPos,
	    // paint);
	}

	return mutableBitmap;
    }

    public Bitmap drawHeadMapPos(ArrayList<LatLng> mListPoint, int prevColor,
	    int curColor) {
	Bitmap workingBitmap = Bitmap.createBitmap(mBitmap);
	Bitmap mutableBitmap = workingBitmap
		.copy(Bitmap.Config.ARGB_8888, true);
	Canvas canvas = new Canvas(mutableBitmap);

	Paint paint = new Paint();
	paint.setAntiAlias(true);
	paint.setColor(prevColor);

	for (LatLng point : mListPoint) {
	    // XYCohesion xyCohesion =
	    // convertXpos(point.latitude,point.longitude);
	    XYCohesion xyCohesion = convertToPos(point.latitude,
		    point.longitude);
	    // just point inside stadium
	    if (xyCohesion.XPos <= 1 && xyCohesion.YPos <= 1) {
		double xPos = mBitmap.getWidth() * xyCohesion.XPos;
		double yPos = mBitmap.getHeight() * xyCohesion.YPos;
		if (mListPoint.lastIndexOf(point) == (mListPoint.size() - 1)) {
		    paint.setColor(curColor);
		    canvas.drawCircle((float) xPos, (float) yPos, 10, paint);
		} else {
		    canvas.drawCircle((float) xPos, (float) yPos, 10, paint);
		}
	    }

	}

	return mutableBitmap;
    }

    public Bitmap clearHeatMap() {
	Bitmap workingBitmap = Bitmap.createBitmap(mBitmap);
	Bitmap mutableBitmap = workingBitmap
		.copy(Bitmap.Config.ARGB_8888, true);
	Canvas canvas = new Canvas(mutableBitmap);
	// canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
	canvas.drawColor(0, Mode.CLEAR);

	return mutableBitmap;
    }

    public XYCohesion convertXpos(double pointlat, double pointLon) {
	XYCohesion xyCohesion = new XYCohesion();

	double XvectorOX = pointlat - topleftlat;
	double YvectorOX = pointLon - topleftlon;

	double XvectorOA = toprightlat - topleftlat;
	double YvectorOA = toprightlon - topleftlon;

	double XvectorOC = bottomleftlat - topleftlat;
	double YvectorOC = bottomleftlon - topleftlon;

	double absOX = Math.sqrt(XvectorOX * XvectorOX + YvectorOX * YvectorOX);
	double absOA = Math.sqrt(XvectorOA * XvectorOA + XvectorOA * XvectorOA);
	double absOC = Math.sqrt(XvectorOC * XvectorOC + YvectorOC * YvectorOC);

	double OXnew = absOX
		* ((XvectorOX * XvectorOA + YvectorOX * YvectorOA))
		/ (absOX * absOA);

	xyCohesion.XPos = OXnew / absOA;
	xyCohesion.YPos = Math.sqrt(absOX * absOX - OXnew * OXnew) / absOC;

	return xyCohesion;

    }

    public XYCohesion convertToPos(double pointlat, double pointLon) {
	XYCohesion xyCohesion = new XYCohesion();

	double AB = calcDistance(topleftlon, topleftlat, toprightlon,
		toprightlat);
	double XA = calcDistance(topleftlon, topleftlat, pointLon, pointlat);
	double XB = calcDistance(toprightlon, toprightlat, pointLon, pointlat);
	// double XC = calcDistance(bottomleftlon, bottomleftlat, pointLon,
	// pointlat);
	double AC = calcDistance(topleftlon, topleftlat, bottomleftlon,
		bottomleftlat);

	double P = (AB + XA + XB) / 2;
	// double P1 = (XA+ XC +AC)/2;
	double ha = 2 * Math.sqrt(P * (P - AB) * (P - XA) * (P - XB)) / AB;
	// double hb = 2*Math.sqrt(P1*(P1-XA)*(P1-XC)*(P1-AC))/AC;
	double hb = Math.sqrt(XA * XA - ha * ha);

	xyCohesion.XPos = hb / AB;
	xyCohesion.YPos = ha / AC;

	return xyCohesion;

    }

    /**
     * Function to calculate the mDistance which player run using Haversine
     * formula
     * 
     * @return the mDistance
     * */
    private double calcDistance(double lngStart, double latStart,
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

    public class XYCohesion {
	public double XPos;
	public double YPos;
    }

}
