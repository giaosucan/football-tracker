/*
 * Copyright (C) 2013
 * HelpActivity class.
 */

package com.android.gps.gui;

/*
 * Display Help screen guide user how to use this application
 *
 */

import com.android.gps.main.App;
import com.android.gps.main.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * @author 4000105.
 * 
 */
public class HelpActivity extends Activity {

    static public final String ARG_TEXT_ID = "text_id";

    /**
     * onCreate - called when the activity is first created. Called when the
     * activity is first created. This is where you should do all of your normal
     * static set up: create views, bind data to lists, etc. This method also
     * provides you with a Bundle containing the activity's previously frozen
     * state, if there was one.
     * 
     * Always followed by onStart().
     * 
     */

    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_help);

	// Set up so that formatted text can be in the help_page_intro text and
	// so that html links are handled.
	TextView textView = (TextView) findViewById(R.id.help_page_intro);
	if (textView != null) {
	    textView.setMovementMethod(LinkMovementMethod.getInstance());
	    textView.setText(Html
		    .fromHtml(getString(R.string.help_page_intro_html)));
	}
    }

    @Override
    protected void onStart() {
	super.onStart();
	App.HelpActivity = this;
    };

    @Override
    protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	App.HelpActivity = null;
    }

    /**
     * Handle the click of one of the help buttons on the page. Start an
     * activity to display the help text for the topic selected.
     * 
     * @param v
     *            View
     * @return void
     */

    public void onClickHelp(View v) {
	int id = v.getId();
	int textId = -1;
	switch (id) {
	case R.id.icon_help_about:
	    textId = R.string.help_topic_about;
	    break;
	case R.id.icon_help_require:
	    textId = R.string.help_topic_require;
	    break;
	case R.id.icon_help_guides:
	    textId = R.string.help_topic_guide;
	    break;
	case R.id.icon_help_contact:
	    textId = R.string.help_topic_contact;
	    break;
	default:
	    break;
	}

	if (textId >= 0)
	    startInfoActivity(textId);
	else
	    toast("Detailed Help for that topic is not available.", true);
    }

    /**
     * Start a TopicActivity and show the text indicated by argument 1.
     * 
     * @param textId
     *            int - resource id of the text to show
     * @return void
     */

    public void startInfoActivity(int textId) {
	if (textId >= 0) {
	    Intent intent = (new Intent(this, HelpTopicActivity.class));
	    intent.putExtra(ARG_TEXT_ID, textId);
	    startActivity(intent);
	} else {
	    toast("No information is available for topic: " + textId, true);
	}
    } // end startInfoActivity

    /**
     * Show a string on the screen via Toast.
     * 
     * @param msg
     *            String
     * @param longLength
     *            boolean - show message a long time
     * @return void
     */

    public void toast(String msg, boolean longLength) {
	Toast.makeText(getApplicationContext(), msg,
		(longLength ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT)).show();
    }

}
