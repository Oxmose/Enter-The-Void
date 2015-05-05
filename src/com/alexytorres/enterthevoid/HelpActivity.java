package com.alexytorres.enterthevoid;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		Button goback = (Button) findViewById(R.id.activity_help_backbtn);
		goback.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				HelpActivity.this.finish();
			}
		});
	} // onCreate()
}
