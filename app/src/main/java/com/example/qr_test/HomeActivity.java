package com.example.qr_test;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class HomeActivity extends Activity {


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);

		//port, Baud rate 설정
		final Button buttonSetup = (Button) findViewById(R.id.ButtonSetup);
		buttonSetup.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						SerialPortPreferences.class));
			}
		});

		//QR 테스트
		final Button buttonInto = (Button) findViewById(R.id.ButtonInto);
		buttonInto.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						ConsoleActivity.class));
			}
		});

	}

}
