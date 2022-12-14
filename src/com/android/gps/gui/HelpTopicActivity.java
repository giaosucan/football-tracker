package com.android.gps.gui;

/*
 * Display Help screen.
 */
import com.android.gps.main.App;
import com.android.gps.main.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

public class HelpTopicActivity extends Activity {
    int mTextResourceId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.activity_help_topic);
	// Read the arguments from the Intent object.
	Intent in = getIntent();
	mTextResourceId = in.getIntExtra(HelpActivity.ARG_TEXT_ID, 0);
	if (mTextResourceId <= 0)
	    mTextResourceId = R.string.no_help_available;

	TextView textView = (TextView) findViewById(R.id.topic_text);
	textView.setMovementMethod(LinkMovementMethod.getInstance());
	textView.setText(Html.fromHtml(getString(mTextResourceId)));
    }

    protected void onStart() {
	super.onStart();
	App.HelpTopicActivity = this;
    };

    @Override
    protected void onDestroy() {
	// TODO Auto-generated method stub
	super.onDestroy();
	App.HelpTopicActivity = null;
    }

}
