/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，显示蓝牙设备列表
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class DeviceListActivity extends Activity{
	
	private static final String TAG = "DeviceLiatActivity";
	private static final boolean D = true;
	
	public static String EXTRA_DEVICE_ADDRESS = "device_address";
	
	private BluetoothAdapter mBtAdapter;
	private ArrayAdapter<String> mPairedDeviceArrayAdapter;
	private ArrayAdapter<String> mNewDeviceArrayAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.device_list);
		
		setResult(Activity.RESULT_CANCELED);
		
		Button scanButton = (Button)findViewById(R.id.button_scan);
		scanButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				doDiscovery();
				v.setVisibility(View.GONE);
			}
		});
		
		mPairedDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		mNewDeviceArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
		
		ListView pairedListView = (ListView)findViewById(R.id.paired_devices);
		pairedListView.setAdapter(mPairedDeviceArrayAdapter);
		pairedListView.setOnItemClickListener(mDeviceClickListener);
		
		ListView newListView = (ListView)findViewById(R.id.new_devices);
		newListView.setAdapter(mNewDeviceArrayAdapter);
		newListView.setOnItemClickListener(mDeviceClickListener);
		
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		this.registerReceiver(mReceiver, filter);
		
		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		this.registerReceiver(mReceiver, filter);
		
		mBtAdapter = BluetoothAdapter.getDefaultAdapter();
		
		Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
		
		if(pairedDevices.size() > 0)
		{
			findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
			for(BluetoothDevice device :  pairedDevices){
				mPairedDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}else{
			String noDevices = getResources().getText(R.string.none_paired).toString();
			mPairedDeviceArrayAdapter.add(noDevices);
		}
		
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		if(mBtAdapter != null)
		{
			mBtAdapter.cancelDiscovery();
		}
		
		this.unregisterReceiver(mReceiver);
	}
	
	private void doDiscovery(){
		if(D) Log.d(TAG, "doDiecovery()");
		
		setProgressBarVisibility(true);
		setTitle(R.string.scannning);
		
		findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);
		
		if(mBtAdapter.isDiscovering()){
			mBtAdapter.cancelDiscovery();
		}
		
		mBtAdapter.startDiscovery();
	}
	
	private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
		public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3){
			mBtAdapter.cancelDiscovery();
			
			String info = ((TextView) v).getText().toString();
			String address = info.substring(info.length() - 17);
			
			Intent intent = new Intent();
			intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
			
			setResult(Activity.RESULT_OK, intent);
			finish();
		}
	
	};
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver(){
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			
			if(BluetoothDevice.ACTION_FOUND.equals(action)){
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				if(device.getBondState() != BluetoothDevice.BOND_BONDED){
					mNewDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
				}
			}else if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
				setProgressBarVisibility(false);
				setTitle(R.string.select_device);
				if(mNewDeviceArrayAdapter.getCount() == 0){
					String noDevices = getResources().getText(R.string.none_found).toString();
					mNewDeviceArrayAdapter.add(noDevices);
				}
			}
		}
	};

}
