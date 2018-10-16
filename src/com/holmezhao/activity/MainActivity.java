package com.holmezhao.activity;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import tools.FileUtils;
import tools.FileUtils.NoSdcardException;
import tools.Generic;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.holmezhao.io.MjpegInputStream;
import com.holmezhao.view.QPopupWindow;

public class MainActivity extends Activity {
	private Context mContext = this;
	/*
	 * private AutoCompleteTextView ipEdt = null; private EditText portEdt =
	 * null; private TextView hintTv = null; private DhcpInfo dpInfo = null;
	 */
//	private WifiManager wifi = null;
	private InputStream is = null;
	/*
	 * private SharedPreferences sp = null; private Editor editor = null;
	 */
	private String port = "8080";/* 用来保存获得用户输入的端口 */
	private String ip = "192.168.10.1";
	private String tempFilePath, videoFilePath;
	private MyApp myApp;
//	SoundPool snd = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);// 用于播放音乐，先不管
//	int videoSound;// soundID
	private static final String TEMP_INFO = "temp_info";
	private Intent intent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Android4.0以上操作网络要加的代码 */
		if (Build.VERSION.SDK_INT >= 11) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
					.detectDiskReads().detectDiskWrites().detectNetwork()
					.penaltyLog().build());
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
					.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
					.penaltyLog()/* .penaltyDeath() */.build());
		}

		myApp = (MyApp) getApplication();
//		wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		intent = new Intent();
		
//		int state = wifi.getWifiState();// 获得wifi当前状态
//		if (state != WifiManager.WIFI_STATE_ENABLED) {
//			Generic.showMsg(this, "请打开wifi", false);
//		}

		// 在手机外部存储中创建BuEye/temp文件夹、BuEye/video文件夹
		tempFilePath = "BuEye/temp";
		videoFilePath = "BuEye/video";
		try {
			new FileUtils().getSDCardRoot();
			new FileUtils().creatSDDir("BuEye");
			new FileUtils().creatSDDir(tempFilePath);
			new FileUtils().creatSDDir(videoFilePath);
		} catch (NoSdcardException e) {
			e.printStackTrace();
		}
	}
	
	public void onClickMain(View v){
		switch (v.getId()){
			case R.id.aboutus:
				// 弹出about窗口
				AboutPopShow();
				break;
			case R.id.help:
				intent.setClass(MainActivity.this, HelpActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.settings:
				intent.setClass(MainActivity.this, SettingsActivity.class);
				MainActivity.this.startActivity(intent);
				break;
			case R.id.jump:
				new ConnectTask().execute(ip);
				break;
			default:
				break;
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		// 获得编辑器
		SharedPreferences.Editor editor = getSharedPreferences(TEMP_INFO,
				MODE_PRIVATE).edit();
		// 将EditText中的文本内容添加到编辑器
		editor.putBoolean("sound", myApp.getSoundFlag());
		// 提交编辑器内容
		editor.commit();
	}

	private void AboutPopShow() {
		// TODO Auto-generated method stub
		View view = (RelativeLayout) findViewById(R.id.mainLayout);
		RelativeLayout parentView = (RelativeLayout) LayoutInflater.from(
				MainActivity.this).inflate(R.layout.about, null);
		QPopupWindow popupWindow = new QPopupWindow(parentView,
				(int) (view.getWidth() * 0.9), (int) (view.getHeight() * 0.9),
				true);

		popupWindow.setOutsideTouchable(true);
		popupWindow.setTouchable(true);
		popupWindow.setBackgroundDrawable(new ColorDrawable());
		popupWindow.setAnimationStyle(R.style.PopupAnimation);
		popupWindow.update();
		popupWindow.showAtLocation(view, Gravity.CENTER_VERTICAL, 0, 0);
	}

	@Override
	/**
	 * 调用finish方法时，这方法将被激发
	 * 设置输入流为空，调用父类的onDestroy销毁资源
	 */
	protected void onDestroy() {
		is = null;
		super.onDestroy();
	}
	
	/**
	 * 连接线程 此类的作用是在后台线程里执行http连接，连接卡住不会影响UI运行，适合于运行时间较长但又不能影响前台线程的情况
	 * 异步任务，有3参数和4步
	 * :onPreExecute()，doInBackground()，onProgressUpdate()，onPostExecute()
	 * onPreExecute()：运行于UI线程，一般为后台线程做准备，如在用户接口显示进度条
	 * doInBackground():当onPreExecute执行后，马上被触发，执行花费较长时间的后台运算，将返回值传给onPostExecute
	 * onProgressUpdate():当用户调用 publishProgress()将被激发，执行的时间未定义，这个方法可以用任意形式显示进度
	 * 一般用于激活一个进度条或者在UI文本领域显示logo onPostExecute():当后台进程执行后在UI线程被激发，把后台执行的结果通知给UI
	 * 参数一:运行于后台的doInBackground的参数类型
	 * 参数二:doInBackground计算的通知给UI线程的单元类型，即运行于UI线程onProgressUpdate的参数类型，这里没用到
	 * 参数三:doInBackground的返回值，将传给onPostExecute作参数
	 * 
	 * @author Administrator
	 * 
	 */
	private class ConnectTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			for (int i = 0; i < params.length; i++) {
				String ip = params[i];/* 取出每一个ip */

				if (ip.split("\\.").length == 4) {
					/**
					 * 在浏览器观察画面时,也是输入下面的字符串网址
					 */
					String action = "http://" + ip + ":" + port
							+ "/?action=stream";
					is = http(action);
					if (is != null) { /* 第一次必须输入IP，下次登录时才可找到之前登录成功后的IP */
						// writeSp(ip);
						MjpegInputStream.initInstance(is);
						break;
					}
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			if (is != null) {
				/**
				 * Intent是Android特有的东西，可以在Intent指定程序要执行的动作(比如:view,edit,dial)
				 * 都准备好程序执行该工作所需要的材料后
				 * ，只要调用startActivity，Android系统会自动寻找最符合你指定要求的应用程序 并执行该程序
				 */
				startActivity(new Intent(MainActivity.this, CtrlActivity.class));
				// finish();/*结束本Activity*/
			} else {
				startActivity(new Intent(MainActivity.this, CtrlActivity.class));
				Generic.showMsg(mContext, "航拍模块连接失败", true);
			}

			super.onPostExecute(result);
		}

		/**
		 * 功能：http连接 Android提供两种http客户端， HttpURLConnection 和 Apache HTTP
		 * Client，它们都支持HTTPS，能上传和下载文件 配置超时时间，用于IPV6和 connection pooling， Apache
		 * HTTP client在Android2.2或之前版本有较少BUG
		 * 但在Android2.2或之后，HttpURLConnection是更好的选择，在这里我们用的是 Apache HTTP Client
		 * 凡是对IO的操作都会涉及异常，所以要try和catch
		 * 
		 * @param url
		 * @return InputStream
		 */
		private InputStream http(String url) {
			HttpResponse res;
			DefaultHttpClient httpclient = new DefaultHttpClient();// 创建http客户端，
																	// 才能调用它的各种方法

			httpclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 500);/* 设置超时时间 */

			try {
				HttpGet hg = new HttpGet(url);// 这是GET方法的httpAPI，GET方法是默认的HTTP请求方法
				res = httpclient.execute(hg);
				return res.getEntity().getContent(); // 从响应中获取消息实体内容
			} catch (IOException e) {
			}

			return null;
		}

	}

}
