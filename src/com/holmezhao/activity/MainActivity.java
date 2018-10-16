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
	private String port = "8080";/* �����������û�����Ķ˿� */
	private String ip = "192.168.10.1";
	private String tempFilePath, videoFilePath;
	private MyApp myApp;
//	SoundPool snd = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);// ���ڲ������֣��Ȳ���
//	int videoSound;// soundID
	private static final String TEMP_INFO = "temp_info";
	private Intent intent = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		/* Android4.0���ϲ�������Ҫ�ӵĴ��� */
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
		
//		int state = wifi.getWifiState();// ���wifi��ǰ״̬
//		if (state != WifiManager.WIFI_STATE_ENABLED) {
//			Generic.showMsg(this, "���wifi", false);
//		}

		// ���ֻ��ⲿ�洢�д���BuEye/temp�ļ��С�BuEye/video�ļ���
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
				// ����about����
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
		// ��ñ༭��
		SharedPreferences.Editor editor = getSharedPreferences(TEMP_INFO,
				MODE_PRIVATE).edit();
		// ��EditText�е��ı�������ӵ��༭��
		editor.putBoolean("sound", myApp.getSoundFlag());
		// �ύ�༭������
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
	 * ����finish����ʱ���ⷽ����������
	 * ����������Ϊ�գ����ø����onDestroy������Դ
	 */
	protected void onDestroy() {
		is = null;
		super.onDestroy();
	}
	
	/**
	 * �����߳� ������������ں�̨�߳���ִ��http���ӣ����ӿ�ס����Ӱ��UI���У��ʺ�������ʱ��ϳ����ֲ���Ӱ��ǰ̨�̵߳����
	 * �첽������3������4��
	 * :onPreExecute()��doInBackground()��onProgressUpdate()��onPostExecute()
	 * onPreExecute()��������UI�̣߳�һ��Ϊ��̨�߳���׼���������û��ӿ���ʾ������
	 * doInBackground():��onPreExecuteִ�к����ϱ�������ִ�л��ѽϳ�ʱ��ĺ�̨���㣬������ֵ����onPostExecute
	 * onProgressUpdate():���û����� publishProgress()����������ִ�е�ʱ��δ���壬�������������������ʽ��ʾ����
	 * һ�����ڼ���һ��������������UI�ı�������ʾlogo onPostExecute():����̨����ִ�к���UI�̱߳��������Ѻ�ִ̨�еĽ��֪ͨ��UI
	 * ����һ:�����ں�̨��doInBackground�Ĳ�������
	 * ������:doInBackground�����֪ͨ��UI�̵߳ĵ�Ԫ���ͣ���������UI�߳�onProgressUpdate�Ĳ������ͣ�����û�õ�
	 * ������:doInBackground�ķ���ֵ��������onPostExecute������
	 * 
	 * @author Administrator
	 * 
	 */
	private class ConnectTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			for (int i = 0; i < params.length; i++) {
				String ip = params[i];/* ȡ��ÿһ��ip */

				if (ip.split("\\.").length == 4) {
					/**
					 * ��������۲컭��ʱ,Ҳ������������ַ�����ַ
					 */
					String action = "http://" + ip + ":" + port
							+ "/?action=stream";
					is = http(action);
					if (is != null) { /* ��һ�α�������IP���´ε�¼ʱ�ſ��ҵ�֮ǰ��¼�ɹ����IP */
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
				 * Intent��Android���еĶ�����������Intentָ������Ҫִ�еĶ���(����:view,edit,dial)
				 * ��׼���ó���ִ�иù�������Ҫ�Ĳ��Ϻ�
				 * ��ֻҪ����startActivity��Androidϵͳ���Զ�Ѱ���������ָ��Ҫ���Ӧ�ó��� ��ִ�иó���
				 */
				startActivity(new Intent(MainActivity.this, CtrlActivity.class));
				// finish();/*������Activity*/
			} else {
				startActivity(new Intent(MainActivity.this, CtrlActivity.class));
				Generic.showMsg(mContext, "����ģ������ʧ��", true);
			}

			super.onPostExecute(result);
		}

		/**
		 * ���ܣ�http���� Android�ṩ����http�ͻ��ˣ� HttpURLConnection �� Apache HTTP
		 * Client�����Ƕ�֧��HTTPS�����ϴ��������ļ� ���ó�ʱʱ�䣬����IPV6�� connection pooling�� Apache
		 * HTTP client��Android2.2��֮ǰ�汾�н���BUG
		 * ����Android2.2��֮��HttpURLConnection�Ǹ��õ�ѡ�������������õ��� Apache HTTP Client
		 * ���Ƕ�IO�Ĳ��������漰�쳣������Ҫtry��catch
		 * 
		 * @param url
		 * @return InputStream
		 */
		private InputStream http(String url) {
			HttpResponse res;
			DefaultHttpClient httpclient = new DefaultHttpClient();// ����http�ͻ��ˣ�
																	// ���ܵ������ĸ��ַ���

			httpclient.getParams().setParameter(
					CoreConnectionPNames.CONNECTION_TIMEOUT, 500);/* ���ó�ʱʱ�� */

			try {
				HttpGet hg = new HttpGet(url);// ����GET������httpAPI��GET������Ĭ�ϵ�HTTP���󷽷�
				res = httpclient.execute(hg);
				return res.getEntity().getContent(); // ����Ӧ�л�ȡ��Ϣʵ������
			} catch (IOException e) {
			}

			return null;
		}

	}

}
