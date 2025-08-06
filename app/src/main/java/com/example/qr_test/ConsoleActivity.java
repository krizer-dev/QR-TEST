package com.example.qr_test;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ConsoleActivity extends SerialPortActivity {
	TextView mymsg;
	ScanActivity myScan;
	static int DecodeCnt = 0;
	private static final String TAG = "ConsoleActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan);

		mymsg = (TextView) findViewById(R.id.device_msg);

		if (mOutputStream == null || mInputStream == null) {
			onDestroy();
			return;
		}

		myScan = new ScanActivity(this, myHandler, mOutputStream, mInputStream);

	}


	//메세지 출력
	Handler myHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

				//바코드 스캔
			case ScanActivity.HANDLER_SCAN_ONE_DEC:
				Log.w(TAG, "HANDLER_SCAN_ONE_DEC");
				mymsg.setText("[" + (++DecodeCnt) + "]" + "" + (String) msg.obj);
				Log.d(TAG, "(String) msg.obj = " + msg.obj );

				break;

				//QR 스캔
			case ScanActivity.HANDLER_SCAN_TWO_DEC:
				Log.w(TAG, "HANDLER_SCAN_TWO_DEC");
				mymsg.setText("[" + (++DecodeCnt) + "]" + "" + (String) msg.obj);
				Log.d(TAG, "(String) msg.obj = " + msg.obj );

				break;
			case ScanActivity.HANDLER_SCAN_TEXT:

				break;
			}
		}
	};

	@Override
	protected void onDestroy() {
		myScan.exit();
		super.onDestroy();
	}
}
