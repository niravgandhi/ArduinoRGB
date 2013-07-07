package com.example.arduinotest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private static final String TAG = "com.example.androidtest.MainActivity";
	protected static final int RESULT_SPEECH = 1;
	private UsbSerialDriver driver;
	private UsbManager manager;
	private Context context;
	private TextView tempTextView;
	private Button listenButton;
	private HashMap<String,Integer[]> colorMap;
	private EditText tempEditText;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		colorMap = new HashMap<String,Integer[]>();
		colorMap.put("blue", new Integer[]{1,0});
		colorMap.put("Green", new Integer[]{0,255});
		colorMap.put("red", new Integer[]{0,0});
		colorMap.put("purple", new Integer[]{1,129});
		colorMap.put("yellow", new Integer[]{0,65});
		tempTextView = (TextView) findViewById(R.id.textView1);
		tempEditText = (EditText) findViewById(R.id.editText1);
		context = this;
		listenButton = (Button) findViewById(R.id.button1);
		manager = (UsbManager) getSystemService(Context.USB_SERVICE);
		driver = UsbSerialProber.acquire(manager);
		if(driver!=null){
			try{
				driver.open();
				driver.setBaudRate(57600);
			}
			catch(IOException e){
				Log.d(TAG,"Exception");
				Toast.makeText(context, "Connection unsuccessful", Toast.LENGTH_SHORT).show();
			}
		}
		else{
			Toast.makeText(context, "driver is null", Toast.LENGTH_SHORT).show();
		}
		
		listenButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
				try{
					startActivityForResult(intent, RESULT_SPEECH);
				}
				catch(ActivityNotFoundException e){
					Toast.makeText(context, "Not supported", Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch(requestCode){
			case RESULT_SPEECH:
				if(resultCode == RESULT_OK && data !=null){
					ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
					tempEditText.setText(text.get(0));
					if(colorMap.containsKey(text.get(0))){
						tempTextView.setText("" + text.get(0));
						byte[] byteArray = new byte[]{(byte)(colorMap.get(text.get(0))[0] & 0xff),(byte)(colorMap.get(text.get(0))[1] & 0xff)};
						try {
							driver.write(byteArray, 120);
							Toast.makeText(context, "Written successfully", Toast.LENGTH_SHORT).show();
						} catch (IOException e) {
							Toast.makeText(context, "Unable to write", Toast.LENGTH_SHORT).show();
							e.printStackTrace();
						}
					}
				}
			}
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
