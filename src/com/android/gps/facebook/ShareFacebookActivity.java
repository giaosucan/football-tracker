/*
 * Copyright (C) 2013
 * ShareFacebookActivity class
 */

package com.android.gps.facebook;

import org.json.JSONObject;
import org.json.JSONTokener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.android.gps.util.Constant;
import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.BaseRequestListener;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.facebook.android.SessionStore;

/**
 * This class provides Facebook features.
 */
public class ShareFacebookActivity extends Activity {
    /**
     * instance Facebook variable.
     */
    private Facebook mFacebook;

    /**
     * Control the progress Dialog.
     */
    private ProgressDialog mProgress;
    private Handler mRunOnUi = new Handler();
    String mDistance;
    String mDuration;
    String mSpeed;
    int totalSprint;
    private static final String[] PERMISSIONS = new String[] {
	    "publish_stream", "read_stream", "offline_access" };

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	mProgress = new ProgressDialog(this);
	mFacebook = new Facebook(Constant.APP_ID);

	Bundle extras = getIntent().getExtras();
	if (extras != null) {
	    mDistance = extras.getString("distance");
	    mDuration = extras.getString("duration");
	    mSpeed = extras.getString("speed");
	    totalSprint = extras.getInt("totalSprint");
	}

	SessionStore.restore(mFacebook, this);
	if (mFacebook.isSessionValid()) {
	    String name = SessionStore.getName(this);
	    name = (name.equals("")) ? "Unknown" : name;
	}

	connectFacebook();
    }

    /**
     * Authorize user login to facebook Post status to facebook.
     * 
     * @param Nil
     */
    private void connectFacebook() {
	if (mFacebook.isSessionValid()) {
	    // final AlertDialog.Builder builder = new
	    // AlertDialog.Builder(this);
	    //
	    // builder.setMessage("Delete current Facebook connection?")
	    // .setCancelable(false)
	    // .setPositiveButton("Yes",
	    // new DialogInterface.OnClickListener() {
	    // public void onClick(DialogInterface dialog, int id) {
	    // fbLogout();
	    // }
	    // })
	    // .setNegativeButton("No", new DialogInterface.OnClickListener() {
	    // public void onClick(DialogInterface dialog, int id) {
	    // dialog.cancel();
	    //
	    //
	    // }
	    // });
	    //
	    // final AlertDialog alert = builder.create();
	    //
	    // alert.show();
	    postToFacebook("I have complete a football match",
		    "Athele tracker", "Here is my result", "www.elance.com",
		    "Distance :" + mDistance + "\n" + "Time :" + mDuration
			    + "\n" + "Average Speed: " + mSpeed + "\n"
			    + "Total Sprints: " + totalSprint,
		    Constant.APP_LOGO_PATH);
	} else {
	    mFacebook.authorize(this, PERMISSIONS, -1,
		    new FbLoginDialogListener());
	}
    }

    /*
     * 
     * Post status to facebook
     * 
     * @param message : Message post to Facebook name : Name of status Caption :
     * Caption of status link : Hyperlink of status desc : General description
     * imagePath: Image path
     * 
     * @return null
     */

    private void postToFacebook(String message, String name, String caption,
	    String link, String desc, String imagePath) {
	mProgress.setMessage("Posting ...");
	mProgress.show();
	AsyncFacebookRunner mAsyncFbRunner = new AsyncFacebookRunner(mFacebook);
	Bundle params = new Bundle();
	params.putString("message", message);
	params.putString("name", name);
	params.putString("caption", caption);
	params.putString("link", link);
	params.putString("description", desc);
	params.putString("picture", imagePath);
	mAsyncFbRunner.request("me/feed", params, "POST",
		new WallPostListener());
    }

    private final class WallPostListener extends BaseRequestListener {
	public void onComplete(final String response) {
	    mRunOnUi.post(new Runnable() {
		@Override
		public void run() {
		    mProgress.cancel();
		    Toast.makeText(ShareFacebookActivity.this,
			    "Posted to Facebook", Toast.LENGTH_SHORT).show();
		    finish();
		}
	    });
	}

    }

    private final class FbLoginDialogListener implements DialogListener {
	public void onComplete(Bundle values) {
	    SessionStore.save(mFacebook, ShareFacebookActivity.this);
	    getFbName();
	    postToFacebook("I have complete a football match",
		    "Athele tracker", "Here is my result", "www.elance.com",
		    "Distance :" + mDistance + "\n" + "Time :" + mDuration
			    + "\n" + "Average Speed: " + mSpeed + "\n"
			    + "Total Sprints: " + totalSprint,
		    Constant.APP_LOGO_PATH);
	}

	public void onFacebookError(FacebookError error) {
	    Toast.makeText(ShareFacebookActivity.this,
		    "Facebook connection failed", Toast.LENGTH_SHORT).show();

	}

	public void onError(DialogError error) {
	    Toast.makeText(ShareFacebookActivity.this,
		    "Facebook connection failed", Toast.LENGTH_SHORT).show();
	}

	public void onCancel() {

	}
    }

    private void getFbName() {
	mProgress.setMessage("Finalizing ...");
	mProgress.show();

	new Thread() {
	    @Override
	    public void run() {
		String name = "";
		int what = 1;

		try {
		    String me = mFacebook.request("me");

		    JSONObject jsonObj = (JSONObject) new JSONTokener(me)
			    .nextValue();
		    name = jsonObj.getString("name");
		    what = 0;
		} catch (Exception ex) {
		    ex.printStackTrace();
		}

		mFbHandler.sendMessage(mFbHandler.obtainMessage(what, name));
	    }
	}.start();
    }

    private void fbLogout() {
	mProgress.setMessage("Disconnecting from Facebook");
	mProgress.show();

	new Thread() {
	    @Override
	    public void run() {
		SessionStore.clear(ShareFacebookActivity.this);

		int what = 1;

		try {
		    mFacebook.logout(ShareFacebookActivity.this);

		    what = 0;
		} catch (Exception ex) {
		    ex.printStackTrace();
		}

		mHandler.sendMessage(mHandler.obtainMessage(what));
	    }
	}.start();
    }

    private Handler mFbHandler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
	    mProgress.dismiss();

	    if (msg.what == 0) {
		String username = (String) msg.obj;
		username = (username.equals("")) ? "No Name" : username;
		SessionStore.saveName(username, ShareFacebookActivity.this);
		Toast.makeText(ShareFacebookActivity.this,
			"Connected to Facebook as " + username,
			Toast.LENGTH_SHORT).show();
	    } else {
		Toast.makeText(ShareFacebookActivity.this,
			"Connected to Facebook", Toast.LENGTH_SHORT).show();
	    }
	}
    };

    private Handler mHandler = new Handler() {
	@Override
	public void handleMessage(Message msg) {
	    mProgress.dismiss();

	    if (msg.what == 1) {
		Toast.makeText(ShareFacebookActivity.this,
			"Facebook logout failed", Toast.LENGTH_SHORT).show();
	    } else {
		Toast.makeText(ShareFacebookActivity.this,
			"Disconnected from Facebook", Toast.LENGTH_SHORT)
			.show();
	    }
	}
    };
}
