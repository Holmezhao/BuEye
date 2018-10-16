/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，设置界面
 *     2，设置左右手油门，声音开关，wifi、蓝牙连接
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class SettingsActivity extends Activity {
	private ImageButton rightHandSwitch, soundSwitch, wifiSwitch, btSwitch;
	private boolean rightHandFlag = false, soundFlag = true;
	private static final String TEMP_INFO = "temp_info";
	private ImageButton saveSettings;
	private MyApp myApp;
	static final int WIFI_STATE_ENABLED = 3;
	static final int WIFI_STATE_ENABLING = 2;

	// 蓝牙连接用的量
	private BluetoothAdapter mBluetoothAdapter = null;
	private static final int REQUEST_CONNECT_DEVICE = 1;
	private static final int REQUEST_ENABLE_BT = 2;

	private static BluetoothService mBluetoothService = null;

	// Message types sent from the BluetoothRfcommClient Handler
	public static final int MESSAGE_STATE_CHANGE = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_DEVICE_NAME = 4;
	public static final int MESSAGE_TOAST = 5;

	// Key names received from the BluetoothRfcommClient Handler
	public static final String DEVICE_NAME = "device_name";
	public static final String TOAST = "toast";

	private TextView ConnState, ConnStateWifi, text1, text2, text3, text4,
			text5, wifiText, BluetoothText;// 用于显示蓝牙连接状态
	// Name of the connected device
	private String mConnectedDeviceName = null;
	private String wifiSSID = "0";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		// 设置字体
		initTypeface();

		// 获得SharedPreferences实例
		SharedPreferences sp = getSharedPreferences(TEMP_INFO, MODE_PRIVATE);
		// 从SharedPreferences获得备忘录的内容
		rightHandFlag = sp.getBoolean("right", false);
		rightHandSwitch = (ImageButton) findViewById(R.id.righthandswitch);
		soundSwitch = (ImageButton) findViewById(R.id.soundswitch);
		wifiSwitch = (ImageButton) findViewById(R.id.wifiswitch);
		btSwitch = (ImageButton) findViewById(R.id.btswitch);
		saveSettings = (ImageButton) findViewById(R.id.savesettings);
		myApp = (MyApp) getApplication();
		soundFlag = myApp.getSoundFlag();
		if (soundFlag == false) {
			soundSwitch.setImageDrawable(getResources().getDrawable(
					R.drawable.switch_off));
		} else {
			soundSwitch.setImageDrawable(getResources().getDrawable(
					R.drawable.switch_on));
		}
		if (rightHandFlag == false) {
			rightHandSwitch.setImageDrawable(getResources().getDrawable(
					R.drawable.switch_off));
		} else {
			rightHandSwitch.setImageDrawable(getResources().getDrawable(
					R.drawable.switch_on));
		}

		/*
		 * WifiManager wifiManager = (WifiManager)
		 * getSystemService(WIFI_SERVICE); WifiInfo wifiInfo =
		 * wifiManager.getConnectionInfo(); if (wifiManager.getWifiState() ==
		 * WIFI_STATE_ENABLED || wifiManager.getWifiState() ==
		 * WIFI_STATE_ENABLING) {
		 * ConnStateWifi.setText(R.string.title_connected_to +
		 * wifiInfo.getSSID()); }
		 */
		/*
		 * if (wifiManager.getWifiState() == WIFI_STATE_ENABLED ||
		 * wifiManager.getWifiState() == WIFI_STATE_ENABLING) { wifi_flag =
		 * true; } else { wifi_flag = false; }
		 */

		/*
		 * if (wifi_flag == false) {
		 * wifiswitch.setImageDrawable(getResources().getDrawable(
		 * R.drawable.switch_off)); } else {
		 * wifiswitch.setImageDrawable(getResources().getDrawable(
		 * R.drawable.switch_on)); }
		 */
		// audio = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		// Get local Bluetooth adapter
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		// If the adapter is null, then Bluetooth is not supported
		if (mBluetoothAdapter == null) {
			Toast.makeText(this, "Bluetooth is not available",
					Toast.LENGTH_LONG).show();
			// finish();
			return;
		}

		// Initialize the BluetoothService to perform bluetooth connections
		mBluetoothService = new BluetoothService(this, mHandler);

		rightHandSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (rightHandFlag == false) {
					rightHandFlag = true;
					rightHandSwitch.setImageDrawable(getResources()
							.getDrawable(R.drawable.switch_on));
				} else {
					rightHandFlag = false;
					rightHandSwitch.setImageDrawable(getResources()
							.getDrawable(R.drawable.switch_off));
				}
			}
		});
		soundSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				if (soundFlag == false) {
					soundFlag = true;
					myApp.setSoundFlag(true);
					soundSwitch.setImageDrawable(getResources().getDrawable(
							R.drawable.switch_on));
				} else {
					soundFlag = false;
					myApp.setSoundFlag(false);
					soundSwitch.setImageDrawable(getResources().getDrawable(
							R.drawable.switch_off));
				}
			}
		});
		wifiSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
				WifiInfo wifiInfo = wifiManager.getConnectionInfo();
				if (!wifiManager.isWifiEnabled()) {
					wifiManager.setWifiEnabled(true);
				}
				// 跳转到wifi设置界面
				startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)); // 直接进入手机中的wifi网络设置界面
			}
		});
		btSwitch.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				// 请求是否打开蓝牙
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
				} else {
					Intent serverIntent = new Intent();
					serverIntent.setClass(SettingsActivity.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					myApp.setSendFlag(true);
				}
				// if (mBluetoothAdapter.isEnabled()) {
				// Intent serverIntent = new Intent();
				// serverIntent.setClass(SettingsActivity.this,
				// DeviceListActivity.class);
				// startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
				// myApp.setSendFlag(true);
				// }

			}
		});
		saveSettings.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				myApp.setRightHandMode(rightHandFlag);
				finish();
			}
		});
	}

	private void initTypeface() {
		// TODO Auto-generated method stub
		// 设置字体
		ConnState = (TextView) findViewById(R.id.ConnState);
		ConnStateWifi = (TextView) findViewById(R.id.ConnStateWifi);
		text1 = (TextView) findViewById(R.id.text1);
		text2 = (TextView) findViewById(R.id.text2);
		text3 = (TextView) findViewById(R.id.text3);
		text4 = (TextView) findViewById(R.id.text4);
		text5 = (TextView) findViewById(R.id.text5);
		wifiText = (TextView) findViewById(R.id.wifiText);
		BluetoothText = (TextView) findViewById(R.id.BluetoothText);
		// 将字体文件保存在assets/fonts/目录下，创建Typeface对象
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/HOBOSTD.ttf");
		// 使用字体
		ConnState.setTypeface(typeFace);
		ConnStateWifi.setTypeface(typeFace);
		text1.setTypeface(typeFace);
		text2.setTypeface(typeFace);
		text3.setTypeface(typeFace);
		text4.setTypeface(typeFace);
		text5.setTypeface(typeFace);
		wifiText.setTypeface(typeFace);
		BluetoothText.setTypeface(typeFace);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		if ((connect.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState() == NetworkInfo.State.CONNECTED)
				&& wifiInfo.getSSID() != null) {
			wifiSSID = wifiInfo.getSSID();
			Message message = new Message();
			message.what = 1;
			wifiStateHandler.sendMessage(message);
		} else {
			Message message = new Message();
			message.what = 2;
			wifiStateHandler.sendMessage(message);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		// 获得编辑器
		SharedPreferences.Editor editor = getSharedPreferences(TEMP_INFO,
				MODE_PRIVATE).edit();
		// 将EditText中的文本内容添加到编辑器
		// editor.putBoolean("sound", sound_flag);
		editor.putBoolean("right", rightHandFlag);
		// 提交编辑器内容
		editor.commit();
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		myApp.setRightHandMode(rightHandFlag);
	}

	// 调整蓝牙可见性时间，目前用不上
	private void ensureDiscoverable() {
		if (mBluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
			Intent discoverableIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
			discoverableIntent.putExtra(
					BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
			startActivity(discoverableIntent);
		}
	}

	// 回调函数，startActivityForResult的回调函数
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CONNECT_DEVICE:
			if (resultCode == Activity.RESULT_OK) {
				// 获取远端蓝牙的MAC地址
				String address = data.getExtras().getString(
						DeviceListActivity.EXTRA_DEVICE_ADDRESS);
				// 得到远端蓝牙对象
				BluetoothDevice device = mBluetoothAdapter
						.getRemoteDevice(address);
				mBluetoothService.connect(device);
			}
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode != Activity.RESULT_OK) {
				Toast.makeText(this, R.string.bt_not_enabled_leaving,
						Toast.LENGTH_SHORT).show();
				//finish();
			} else {
				if (mBluetoothAdapter.isEnabled()) {
					Intent serverIntent = new Intent();
					serverIntent.setClass(SettingsActivity.this,
							DeviceListActivity.class);
					startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
					myApp.setSendFlag(true);
				}
			}
		}
	};

	private final Handler wifiStateHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:// 连接到
				ConnStateWifi.setText(R.string.title_connected_to);
				ConnStateWifi.append(" " + wifiSSID);
				break;
			case 2:// 未连接
				ConnStateWifi.setText(R.string.title_not_connected);
				break;
			}
		};
	};

	// The Handler that gets information back from the BluetoothService
	@SuppressLint("HandlerLeak")
	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case BluetoothService.STATE_CONNECTED:
					ConnState.setText(R.string.title_connected_to);
					ConnState.append(" " + mConnectedDeviceName);
					break;
				case BluetoothService.STATE_CONNECTING:
					ConnState.setText(R.string.title_connecting);
					break;
				case BluetoothService.STATE_NONE:
					ConnState.setText(R.string.title_not_connected);
					break;
				}
				break;
			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				DataAnl(readBuf, msg.arg1);
				break;
			case MESSAGE_DEVICE_NAME:
				// save the connected device's name
				mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
				Toast.makeText(getApplicationContext(),
						"Connected to " + mConnectedDeviceName,
						Toast.LENGTH_SHORT).show();
				break;
			case MESSAGE_TOAST:
				Toast.makeText(getApplicationContext(),
						msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};

	static void DataAnl(byte[] data, int len) {

	}

	/*
	 * @Override public synchronized void onResume() { super.onResume(); if
	 * (mBluetoothService != null) { // Only if the state is STATE_NONE, do we
	 * know that we haven't // started already if (mBluetoothService.getState()
	 * == mBluetoothService.STATE_NONE) { // Start the Bluetooth RFCOMM services
	 * mBluetoothService.start(); } } }
	 * 
	 * @Override public void onDestroy() { // Stop the Bluetooth RFCOMM services
	 * if (mBluetoothService != null) mBluetoothService.stop();
	 * super.onDestroy(); }
	 */

	// 发送string函数
	static void SendData(String message) {
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}
		// Check that there's actually something to send
		if (message.length() > 0) {
			// Get the message bytes and tell the BluetoothRfcommClient to write
			byte[] send = message.getBytes();
			mBluetoothService.write(send);
		}
	}

	// 发送Byte数据类型函数
	static void SendData_Byte(byte[] data) {
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}
		mBluetoothService.write(data);
	}

	// 发送控制指令函数
	static void Send_Command32(int i, int motor, int yaw, int pitch, int roll) {
		byte[] bytes = new byte[32];
		int sum = 0;
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		bytes[0] = (byte) 0xA5;// 包头
		bytes[1] = (byte) i;// 第i个数据包，i:0~100循环
		bytes[2] = (byte) (motor - motor / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// 油门低8位
		bytes[3] = (byte) (motor / (int) Math.pow(2, 8));// 油门高8位
		bytes[4] = (byte) (yaw - yaw / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// yaw低8位
		bytes[5] = (byte) (yaw / (int) Math.pow(2, 8));// yaw高8位
		bytes[6] = (byte) (pitch - pitch / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// pitch低8位
		bytes[7] = (byte) (pitch / (int) Math.pow(2, 8));
		;// pitch高8位
		bytes[8] = (byte) (roll - roll / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// roll低8位
		bytes[9] = (byte) (roll / (int) Math.pow(2, 8));
		;// roll高8位
		bytes[10] = (byte) 0x00;
		bytes[11] = (byte) 0x00;
		bytes[12] = (byte) 0x00;
		bytes[13] = (byte) 0x00;
		bytes[14] = (byte) 0x00;
		bytes[15] = (byte) 0x00;
		bytes[16] = (byte) 0x00;
		bytes[17] = (byte) 0x00;
		bytes[18] = (byte) 0x00;
		bytes[19] = (byte) 0x00;
		bytes[20] = (byte) 0x00;
		bytes[21] = (byte) 0x00;
		bytes[22] = (byte) 0x00;
		bytes[23] = (byte) 0x00;
		bytes[24] = (byte) 0x00;
		bytes[25] = (byte) 0x00;
		bytes[26] = (byte) 0x00;
		bytes[27] = (byte) 0xff;
		bytes[28] = (byte) 0xff;
		bytes[29] = (byte) 0xff;
		for (int j = 0; j < 30; j++)
			sum = sum + Byte.valueOf(bytes[j]).intValue();// 带测试
		// System.out.println(""+sum);

		bytes[30] = (byte) (sum - sum / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// sum低8位
		bytes[31] = (byte) (sum / (int) Math.pow(2, 8));// sum高8位

		SendData_Byte(bytes);

	}

	static void Send_Command16(int i, int motor, int yaw, int pitch, int roll,
			char left_right_bias, char forward_backward_bias) {
		byte[] bytes = new byte[16];
		int sum = 0;
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		bytes[0] = (byte) 0xA5;// 包头
		bytes[1] = (byte) i;// 第i个数据包，i:0~100循环
		bytes[2] = (byte) (motor - motor / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// 油门低8位
		bytes[3] = (byte) (motor / (int) Math.pow(2, 8));// 油门高8位
		bytes[4] = (byte) (yaw - yaw / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// yaw低8位
		bytes[5] = (byte) (yaw / (int) Math.pow(2, 8));// yaw高8位
		bytes[6] = (byte) (pitch - pitch / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// pitch低8位
		bytes[7] = (byte) (pitch / (int) Math.pow(2, 8));
		;// pitch高8位
		bytes[8] = (byte) (roll - roll / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// roll低8位
		bytes[9] = (byte) (roll / (int) Math.pow(2, 8));
		;// roll高8位
		bytes[10] = (byte) 0x03;// 灯灭
		bytes[11] = (byte) left_right_bias;// 飞机左右静偏，范伟：0~240
		bytes[12] = (byte) forward_backward_bias;// 飞机前后静偏，范伟：0~240
		bytes[13] = (byte) 0x00;
		for (int j = 0; j < 14; j++)
			sum = sum + Byte.valueOf(bytes[j]).intValue();
		bytes[14] = (byte) (sum - sum / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// sum低8位
		bytes[15] = (byte) (sum / (int) Math.pow(2, 8));// sum高8位

		SendData_Byte(bytes);

	}

	static void Send_Command16_LightOn(int i, int motor, int yaw, int pitch,
			int roll) {
		byte[] bytes = new byte[16];
		int sum = 0;
		// Check that we're actually connected before trying anything
		if (mBluetoothService.getState() != BluetoothService.STATE_CONNECTED) {
			// Toast.makeText(this, R.string.not_connected,
			// Toast.LENGTH_SHORT).show();
			return;
		}

		bytes[0] = (byte) 0xA5;// 包头
		bytes[1] = (byte) i;// 第i个数据包，i:0~100循环
		bytes[2] = (byte) (motor - motor / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// 油门低8位
		bytes[3] = (byte) (motor / (int) Math.pow(2, 8));// 油门高8位
		bytes[4] = (byte) (yaw - yaw / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// yaw低8位
		bytes[5] = (byte) (yaw / (int) Math.pow(2, 8));// yaw高8位
		bytes[6] = (byte) (pitch - pitch / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// pitch低8位
		bytes[7] = (byte) (pitch / (int) Math.pow(2, 8));
		;// pitch高8位
		bytes[8] = (byte) (roll - roll / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));
		;// roll低8位
		bytes[9] = (byte) (roll / (int) Math.pow(2, 8));
		;// roll高8位
		bytes[10] = (byte) 0x02;// 灯亮
		bytes[11] = (byte) 0x00;
		bytes[12] = (byte) 0x00;
		bytes[13] = (byte) 0x00;
		for (int j = 0; j < 14; j++)
			sum = sum + Byte.valueOf(bytes[j]).intValue();
		bytes[14] = (byte) (sum - sum / (int) Math.pow(2, 8)
				* (int) Math.pow(2, 8));// sum低8位
		bytes[15] = (byte) (sum / (int) Math.pow(2, 8));// sum高8位

		SendData_Byte(bytes);

	}

}
