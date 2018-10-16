package com.holmezhao.activity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import tools.DensityUtil;
import tools.Generic;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.holmezhao.io.MjpegInputStream;
import com.holmezhao.view.MjpegView;

/**
 * 登录成功后，将执行这里，此类继承了Activity实现了OnCheckedChangeListener监听器
 * OnCheckedChangeListener是当在RadioGroup的RadioButton被选中或改变时，一个回调接口被触发后执行
 * 主要有1个RadioGroup和5个RadioButton，实现了5个控件的跳转，但没都实现具体的操作，只是调到某个类去执行
 */
public class CtrlActivity extends Activity {
	public static CtrlActivity instance = null;
	private MjpegInputStream mis = null;
	private MjpegView mjpegView = null;
	// private RadioGroup mainTab = null;
	private File sdCardFile = null;
	private String picturePath = "";
	private ImageButton back, takephoto, viewphoto, /*light, */openeye, ctrl_open,
			biasButton/*
					 * , takevideo
					 */;
	private ImageView ctrlbg;
	private MyApp myApp;
	private boolean light_flag = false, eye_flag = true, ctrl_flag = false,
			video_flag = false;
	private int i = 0;// 包序号
	private static int yaw = 512, motor = 0, roll = 512, pitch = 512;
	private ImageView motorCircle, directionCircle, motorCircle2,
			directionCircle2;
	int LstartX;
	int LstartY;
	int Loldl, Loldr, Loldt, Loldb;

	int RstartX;
	int RstartY;
	int Roldl, Roldr, Roldt, Roldb;

	private int Ldx, Ldy, Rdx, Rdy;

	private int H, W;
	private RelativeLayout DirectionLayout, DirectionLayout2, CtrlLayout1,
			CtrlLayout2, BiasLayout;
	private boolean gyro_flag = false;
	private SensorManager sensorManager;
	private Sensor accelerometerSensor;
	private float gravity[] = new float[3];// 重力加速度在三个方向上的投影
	private Boolean right_hand_flag = false, send_flag = false,
			sound_flag = true;
	private Chronometer timer;
	Timer send_timer = new Timer();
	SoundPool snd = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);// 用于播放音乐，先不管
	int photo_sound, video_sound;// soundID
	private TextView leftText, rightText, forwardText, backwardText;
	private SeekBar left_right_seekbar = null, forward_backward_seekbar = null;
	private static final String TEMP_INFO = "temp_info";
	private char left_right_bias_progress, forward_backward_bias_progress;

	/**
	 * 当该Activit第一次创建后，该方法即被触发 1.设置布局 2.初始化视频输入流 3.根据在R.java的id找到控件
	 * 4.为RadioGroup设置监听器，当RadioButton被按下或改变时触发下面的onCheckedChanged方法
	 * 5.检查SD卡，初始化mjpegview视图，这样就可看到监控画面了
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ctrl);/* 构造RadioGroup的5的RadioButton */

		instance = this;
		mis = MjpegInputStream.getInstance();
		mjpegView = (MjpegView) findViewById(R.id.mjpegview);
		// mainTab = (RadioGroup) findViewById(R.id.main_tab);
		// mainTab.setOnCheckedChangeListener(this);
		back = (ImageButton) findViewById(R.id.back);
		takephoto = (ImageButton) findViewById(R.id.takephoto);
		viewphoto = (ImageButton) findViewById(R.id.ToGalary);
//		light = (ImageButton) findViewById(R.id.light);
		openeye = (ImageButton) findViewById(R.id.openeye);
		// takevideo = (ImageButton) findViewById(R.id.takevideo);
		ctrl_open = (ImageButton) findViewById(R.id.ctrl_open);
		myApp = (MyApp) getApplication();
		// socket = myApp.getSocket();
		right_hand_flag = myApp.getRightHandMode();
		// 获得SharedPreferences实例
		SharedPreferences sp = getSharedPreferences(TEMP_INFO,
				MODE_WORLD_READABLE);
		// 从SharedPreferences获得备忘录的内容
		right_hand_flag = sp.getBoolean("right", false);
		send_flag = myApp.getSendFlag();
		sound_flag = myApp.getSoundFlag();
		motorCircle = (ImageView) findViewById(R.id.MotorCtrl);
		directionCircle = (ImageView) findViewById(R.id.DirectionCtrl);
		motorCircle2 = (ImageView) findViewById(R.id.MotorCtrl2);
		directionCircle2 = (ImageView) findViewById(R.id.DirectionCtrl2);
		ctrlbg = (ImageView) findViewById(R.id.ctrlbg);
		DirectionLayout = (RelativeLayout) findViewById(R.id.DirectionLayout);
		DirectionLayout2 = (RelativeLayout) findViewById(R.id.DirectionLayout2);
		CtrlLayout1 = (RelativeLayout) findViewById(R.id.CtrlLayout);
		CtrlLayout2 = (RelativeLayout) findViewById(R.id.CtrlLayout2);
		video_sound = snd.load(CtrlActivity.this, R.raw.ping_short, 0);// soundID赋值
		photo_sound = snd.load(CtrlActivity.this, R.raw.take_photo, 0);// soundID赋值
		BiasLayout = (RelativeLayout) findViewById(R.id.BiasLayout);
		biasButton = (ImageButton) findViewById(R.id.biasButton);
		leftText = (TextView) findViewById(R.id.leftText);
		rightText = (TextView) findViewById(R.id.rightText);
		forwardText = (TextView) findViewById(R.id.forwardText);
		backwardText = (TextView) findViewById(R.id.backwardText);
		// 设置字体
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/HOBOSTD.ttf");
		// 使用字体
		leftText.setTypeface(typeFace);
		rightText.setTypeface(typeFace);
		forwardText.setTypeface(typeFace);
		backwardText.setTypeface(typeFace);
		// 取到设置调静偏的seekbar
		left_right_seekbar = (SeekBar) findViewById(R.id.left_right_bias);
		forward_backward_seekbar = (SeekBar) findViewById(R.id.forward_backward_bias);
		// 从SharedPreferences获得备忘录的内容,即是seekbar的初始值
		left_right_bias_progress = (char) sp.getInt("left_right_bias_progress",
				120);
		forward_backward_bias_progress = (char) sp.getInt(
				"forward_backward_bias_progress", 120);
		// 设置seekbar的初始值
		left_right_seekbar.setProgress(left_right_bias_progress);
		forward_backward_seekbar.setProgress(forward_backward_bias_progress);
		// 获得计时器对象
		timer = (Chronometer) findViewById(R.id.chronometer);
		// 获得屏幕分辨率
		Display mDisplay = getWindowManager().getDefaultDisplay();
		W = mDisplay.getWidth();
		H = mDisplay.getHeight();
		if (right_hand_flag) {
			CtrlLayout1.setVisibility(View.GONE);
			CtrlLayout2.setVisibility(View.VISIBLE);
		}
		// 获取管理所有传感器的sensorManager对象
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// 获取加速度传感器对象
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		checkSdcard();
		initMjpegView();

		// 调整静偏的窗口
		biasButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				if (BiasLayout.getVisibility() == View.VISIBLE) {
					BiasLayout.setVisibility(View.INVISIBLE);
					biasButton.setImageDrawable(getResources().getDrawable(
							R.drawable.bias_angle));
				} else {
					BiasLayout.setVisibility(View.VISIBLE);
					biasButton.setImageDrawable(getResources().getDrawable(
							R.drawable.ok_1));
				}
			}
		});

		// 获取图像
		openeye.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				if (eye_flag == false) {
					// mySurfaceView.setVisibility(View.VISIBLE);
					ctrlbg.setVisibility(View.INVISIBLE);
					// mySurfaceView.GetMySocket(socket);
					openeye.setImageDrawable(getResources().getDrawable(
							R.drawable.eye_on));
					eye_flag = true;
				} else {
					// mySurfaceView.setVisibility(View.INVISIBLE);
					ctrlbg.setVisibility(View.VISIBLE);
					openeye.setImageDrawable(getResources().getDrawable(
							R.drawable.open_eye));
					eye_flag = false;
				}
			}
		});

		// 照明
//		light.setOnClickListener(new OnClickListener() {
//
//			public void onClick(View v) {
//				// TODO Auto-generated method stub
////				if (sound_flag) {
////					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
////				}
//				if (light_flag == false) {
//					light.setImageDrawable(getResources().getDrawable(
//							R.drawable.light_on));
//					light_flag = true;
//				} else {
//					light.setImageDrawable(getResources().getDrawable(
//							R.drawable.light_off));
//					light_flag = false;
//				}
//			}
//		});

		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				// finish();
				// System.exit(0);//断开蓝牙
				onBack();
			}
		});

		takephoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				shotSnap();
			}
		});
		/*
		 * takevideo.setOnClickListener(new OnClickListener() {
		 * 
		 * @Override public void onClick(View v) { // TODO Auto-generated method
		 * stub if (sound_flag) { snd.play(video_sound, (float) 0.5, (float)
		 * 0.5, 0, 0, 1); } if (video_flag == false) { // 将计时器清零
		 * timer.setBase(SystemClock.elapsedRealtime()); // 开始计时 timer.start();
		 * video_flag = true; } else {// 停止计时 timer.stop(); // 将计时器清零
		 * timer.setBase(SystemClock.elapsedRealtime()); video_flag = false; } }
		 * });
		 */
		viewphoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				Intent intent = new Intent();
				// intent.setClass(MainActivity.this, MediaActivity.class);
				intent.setClass(CtrlActivity.this, PhotoListActivity.class);// 直接进入照片的文件浏览器
				CtrlActivity.this.startActivity(intent);
				// scanPic();
			}
		});
		// 控制面板
		ctrl_open.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				if (ctrl_flag == false) {
					ctrl_open.setImageDrawable(getResources().getDrawable(
							R.drawable.gyro_icon));
					Toast.makeText(CtrlActivity.this, "体感模式", Toast.LENGTH_SHORT).show();
					gyro_flag = true;
					DirectionLayout.setVisibility(View.INVISIBLE);
					if (right_hand_flag) {
						DirectionLayout2.setVisibility(View.INVISIBLE);
					}
					// DirectionLayout.setEnabled(false);
					ctrl_flag = true;
				} else {
					ctrl_open.setImageDrawable(getResources().getDrawable(
							R.drawable.control_view));
					Toast.makeText(CtrlActivity.this, "摇杆模式", Toast.LENGTH_SHORT).show();
					gyro_flag = false;
					DirectionLayout.setVisibility(0);
					if (right_hand_flag) {
						DirectionLayout2.setVisibility(0);
					}
					// DirectionLayout.setEnabled(true);
					ctrl_flag = false;
				}
			}
		});

		// 重力控制工作
		// 为accelerometerSensor注册监听器
		sensorManager.registerListener(new SensorEventListener() {
			// 传感器参数一变化就执行的代码
			public void onSensorChanged(SensorEvent event) {

				// 计算重力加速度在三轴上的投影值
				gravity[0] = event.values[0];
				gravity[1] = event.values[1];
				gravity[2] = event.values[2];

				// 调试时打印

				/*
				 * System.out.println("x/////" + gravity[0]);
				 * System.out.println("y/////" + gravity[1]);
				 * System.out.println("z/////" + gravity[2]);
				 */

				if (gyro_flag == true) {
					if (gravity[0] < -0.5)// 往前
					{
						pitch = (int) (512 - (gravity[0] + 0.5) * 220);
						if (pitch > 1024)
							pitch = 1024;
					} else if (gravity[0] > 0.5) // 往后
					{
						pitch = (int) (512 - (gravity[0] - 0.5) * 220);
						if (pitch < 0)
							pitch = 0;
					} else {
						pitch = 512;
					}

					if (gravity[1] > 0.5)// 往右
					{
						roll = (int) (512 + (gravity[1] - 0.5) * 180);
						if (roll > 1024)
							roll = 1024;
					} else if (gravity[1] < -0.5)// 往左
					{
						roll = (int) (512 + (gravity[1] + 0.5) * 180);
						if (roll < 0)
							roll = 0;
					} else {
						roll = 512;
					}
				}
			}

			// 传感器精度一变化就执行的代码
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		}, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

		// 左杆
		motorCircle.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 按下，得到当前的坐标位置
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();
					// 得到图像的位置
					Loldl = CtrlActivity.this.motorCircle.getLeft();
					Loldr = CtrlActivity.this.motorCircle.getRight();
					Loldt = CtrlActivity.this.motorCircle.getTop();
					Loldb = CtrlActivity.this.motorCircle.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// 滑动
					int newX = (int) event.getRawX();// 得到的是像素值
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// 得到图像的位置
					int l = CtrlActivity.this.motorCircle.getLeft();
					int r = CtrlActivity.this.motorCircle.getRight();
					int t = CtrlActivity.this.motorCircle.getTop();
					int b = CtrlActivity.this.motorCircle.getBottom();

					if (newX < DensityUtil.dip2px(CtrlActivity.this, 16)) {
						Ldx = 0;
						newl = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newX > DensityUtil.dip2px(CtrlActivity.this,
							16 + 256)) {
						Ldx = 0;
						newl = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Ldx = newX - LstartX;
						newl = l + Ldx;
						newr = r + Ldx;
						if ((newl + newr) / 2 < 0) {
							newl = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newr = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newl + newr) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newl = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newr = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					if (newY < (H - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Ldy = 0;
						newt = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newY > (H - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Ldy = 0;
						newt = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Ldy = newY - LstartY;
						newt = t + Ldy;
						newb = b + Ldy;
						if ((newt + newb) / 2 < 0) {
							newt = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newb = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newt + newb) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newt = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newb = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					// 画出移动到新位置的图像
					CtrlActivity.this.motorCircle
							.layout(newl, newt, newr, newb);

					yaw = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					motor = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);
					// motor = (int)((1024 -
					// 4*DensityUtil.px2dip(CtrlActivity.this, (newt+newb)/2) -
					// 180)/(844.0-180.0)*1024);
					// 打印路径
					// System.out.println("motorCircle"+":"+ yaw +" "+ motor );
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();

					Loldt = newt;
					Loldb = newb;
					break;
				case MotionEvent.ACTION_UP: // 抬起，记录位置
					yaw = 512;
					int lastx = CtrlActivity.this.motorCircle.getLeft();
					int lasty = CtrlActivity.this.motorCircle.getTop();
					CtrlActivity.this.motorCircle.layout(Loldl, Loldt, Loldr,
							Loldb);

					break;
				}
				return true;
			}

		});

		// 右杆
		directionCircle.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 按下，得到当前的坐标位置
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					// 得到图像的位置
					Roldl = CtrlActivity.this.directionCircle.getLeft();
					Roldr = CtrlActivity.this.directionCircle.getRight();
					Roldt = CtrlActivity.this.directionCircle.getTop();
					Roldb = CtrlActivity.this.directionCircle.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// 滑动
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// 得到图像的位置
					int l = CtrlActivity.this.directionCircle.getLeft();
					int r = CtrlActivity.this.directionCircle.getRight();
					int t = CtrlActivity.this.directionCircle.getTop();
					int b = CtrlActivity.this.directionCircle.getBottom();

					if (newX < (W - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Rdx = 0;
						newl = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newX > (W - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Rdx = 0;
						newl = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Rdx = newX - RstartX;
						newl = l + Rdx;
						newr = r + Rdx;
						if ((newl + newr) / 2 < 0) {
							newl = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newr = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newl + newr) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newl = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newr = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					if (newY < (H - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Rdy = 0;
						newt = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newY > (H - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Rdy = 0;
						newt = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Rdy = newY - RstartY;
						newt = t + Rdy;
						newb = b + Rdy;
						if ((newt + newb) / 2 < 0) {
							newt = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newb = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newt + newb) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newt = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newb = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					// 画出移动到新位置的图像
					CtrlActivity.this.directionCircle.layout(newl, newt, newr,
							newb);

					roll = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					pitch = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);

					// 打印路径
					// System.out.println("directionCircle"+":"+ roll +" "+
					// pitch );
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP: // 抬起，记录位置
					roll = 512;
					pitch = 512;
					int lastx = CtrlActivity.this.directionCircle.getLeft();
					int lasty = CtrlActivity.this.directionCircle.getTop();
					CtrlActivity.this.directionCircle.layout(Roldl, Roldt,
							Roldr, Roldb);
					break;
				}
				return true;
			}

		});

		// 右杆
		motorCircle2.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 按下，得到当前的坐标位置
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();
					// 得到图像的位置
					Loldl = CtrlActivity.this.motorCircle2.getLeft();
					Loldr = CtrlActivity.this.motorCircle2.getRight();
					Loldt = CtrlActivity.this.motorCircle2.getTop();
					Loldb = CtrlActivity.this.motorCircle2.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// 滑动
					int newX = (int) event.getRawX();// 得到的是像素值
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// 得到图像的位置
					int l = CtrlActivity.this.motorCircle2.getLeft();
					int r = CtrlActivity.this.motorCircle2.getRight();
					int t = CtrlActivity.this.motorCircle2.getTop();
					int b = CtrlActivity.this.motorCircle2.getBottom();

					if (newX < (W - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Ldx = 0;
						newl = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newX > (W - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Ldx = 0;
						newl = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Ldx = newX - LstartX;
						newl = l + Ldx;
						newr = r + Ldx;
						if ((newl + newr) / 2 < 0) {
							newl = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newr = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newl + newr) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newl = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newr = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					if (newY < (H - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Ldy = 0;
						newt = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newY > (H - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Ldy = 0;
						newt = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Ldy = newY - LstartY;
						newt = t + Ldy;
						newb = b + Ldy;
						if ((newt + newb) / 2 < 0) {
							newt = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newb = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newt + newb) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newt = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newb = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					// 画出移动到新位置的图像
					CtrlActivity.this.motorCircle2.layout(newl, newt, newr,
							newb);

					yaw = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					motor = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);
					// motor = (int)((1024 -
					// 4*DensityUtil.px2dip(CtrlActivity.this, (newt+newb)/2) -
					// 180)/(844.0-180.0)*1024);
					// 打印路径
					// System.out.println("motorCircle"+":"+ yaw +" "+ motor );
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();

					Loldt = newt;
					Loldb = newb;
					break;
				case MotionEvent.ACTION_UP: // 抬起，记录位置
					yaw = 512;
					int lastx = CtrlActivity.this.motorCircle2.getLeft();
					int lasty = CtrlActivity.this.motorCircle2.getTop();
					CtrlActivity.this.motorCircle2.layout(Loldl, Loldt, Loldr,
							Loldb);

					break;
				}
				return true;
			}

		});

		// 左杆
		directionCircle2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// 按下，得到当前的坐标位置
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					// 得到图像的位置
					Roldl = CtrlActivity.this.directionCircle2.getLeft();
					Roldr = CtrlActivity.this.directionCircle2.getRight();
					Roldt = CtrlActivity.this.directionCircle2.getTop();
					Roldb = CtrlActivity.this.directionCircle2.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// 滑动
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// 得到图像的位置
					int l = CtrlActivity.this.directionCircle2.getLeft();
					int r = CtrlActivity.this.directionCircle2.getRight();
					int t = CtrlActivity.this.directionCircle2.getTop();
					int b = CtrlActivity.this.directionCircle2.getBottom();

					if (newX < DensityUtil.dip2px(CtrlActivity.this, 16)) {
						Rdx = 0;
						newl = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newX > DensityUtil.dip2px(CtrlActivity.this,
							16 + 256)) {
						Rdx = 0;
						newl = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newr = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Rdx = newX - RstartX;
						newl = l + Rdx;
						newr = r + Rdx;
						if ((newl + newr) / 2 < 0) {
							newl = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newr = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newl + newr) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newl = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newr = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					if (newY < (H - DensityUtil.dip2px(CtrlActivity.this,
							16 + 256))) {
						Rdy = 0;
						newt = -1 * DensityUtil.dip2px(CtrlActivity.this, 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 45);
					} else if (newY > (H - DensityUtil.dip2px(
							CtrlActivity.this, 16))) {
						Rdy = 0;
						newt = DensityUtil.dip2px(CtrlActivity.this, 256 - 45);
						newb = DensityUtil.dip2px(CtrlActivity.this, 256 + 45);
					} else {
						Rdy = newY - RstartY;
						newt = t + Rdy;
						newb = b + Rdy;
						if ((newt + newb) / 2 < 0) {
							newt = -1
									* DensityUtil.dip2px(CtrlActivity.this, 45);
							newb = DensityUtil.dip2px(CtrlActivity.this, 45);
						} else if ((newt + newb) / 2 > DensityUtil.dip2px(
								CtrlActivity.this, 256)) {
							newt = DensityUtil.dip2px(CtrlActivity.this,
									256 - 45);
							newb = DensityUtil.dip2px(CtrlActivity.this,
									256 + 45);
						}
					}

					// 画出移动到新位置的图像
					CtrlActivity.this.directionCircle2.layout(newl, newt, newr,
							newb);

					roll = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					pitch = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);

					// 打印路径
					// System.out.println("directionCircle"+":"+ roll +" "+
					// pitch );
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP: // 抬起，记录位置
					roll = 512;
					pitch = 512;
					int lastx = CtrlActivity.this.directionCircle2.getLeft();
					int lasty = CtrlActivity.this.directionCircle2.getTop();
					CtrlActivity.this.directionCircle2.layout(Roldl, Roldt,
							Roldr, Roldb);
					break;
				}
				return true;
			}

		});

		if (send_flag == true) {
			send_timer.schedule(send_task, 0, 20);// 第二个参数是等待多长时间才能执行run（），第三个参数是之后每隔多长时间调用一次run（），单位都是毫秒。
			// myApp.setSendFlag(false);
		}
		// send_timer.schedule(send_task2,0,200);//200ms检测一次socket连接
	}

	public void onBack() {//模拟一个返回键
		new Thread() {
			public void run() {
				try {
					Instrumentation inst = new Instrumentation();
					inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
				} catch (Exception e) {
					Log.e("Exception when onBack", e.toString());
				}
			}
		}.start();

	}

	@Override
	protected void onPause() {
		super.onPause();
		// 获得编辑器
		SharedPreferences.Editor editor = getSharedPreferences(TEMP_INFO,
				MODE_WORLD_WRITEABLE).edit();
		// 将EditText中的文本内容添加到编辑器
		editor.putInt("left_right_bias_progress",
				left_right_seekbar.getProgress());
		editor.putInt("forward_backward_bias_progress",
				forward_backward_seekbar.getProgress());
		editor.putBoolean("right", right_hand_flag);
		// 提交编辑器内容
		editor.commit();
	}

	// 控制界面销毁后，停掉timer
	protected void onDestroy() {
		if (send_timer != null) {
			send_timer.cancel();
			send_timer = null;
		}
		super.onDestroy();
	}

	// 建立新线程，每隔200ms，发送一帧数据
	TimerTask send_task = new TimerTask() {

		public void run() {
			if (i > 99)
				i = 0;// i在0到99之间循环
			/*
			 * if (light_flag == false) { SettingsActivity.Send_Command16(i,
			 * motor, yaw, pitch, roll); } else {
			 * SettingsActivity.Send_Command16_LightOn(i, motor, yaw, pitch,
			 * roll); }
			 */
			SettingsActivity.Send_Command16(i, motor, yaw, pitch, roll,
					(char) left_right_seekbar.getProgress(),
					(char) forward_backward_seekbar.getProgress());// 120代表没有静偏
			/*
			 * System.out.println(left_right_seekbar.getProgress());
			 * System.out.println(forward_backward_seekbar.getProgress());
			 */
			i++;
			System.out.println("i" + ":" + i);
			System.out.println(motor + ":" + yaw + ":" + pitch + ":" + roll);
		}
	};

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (right_hand_flag) {
			CtrlLayout1.setVisibility(View.GONE);
			CtrlLayout2.setVisibility(View.VISIBLE);
		} else {
			CtrlLayout1.setVisibility(View.VISIBLE);
			CtrlLayout2.setVisibility(View.GONE);
		}
	}

	/**
	 * 功能:获得SD路径 如果有SD卡，则创建存放图片的picturePath目录
	 */
	private void checkSdcard() {
		sdCardFile = Generic.getSdCardFile();
		if (sdCardFile == null)
			Generic.showMsg(this, "请插入SD卡", true);
		else {
			picturePath = sdCardFile.getAbsolutePath() + "/BuEye/";
			File f = new File(picturePath);
			if (!(f.exists() && f.isDirectory()))
				f.mkdir();
		}
	}

	/**
	 * 调用com/mjpeg/view的mjpegView.java类中mjpegView的众多方法来初始化自定义控件com.mjpeg.view.
	 * MjpegView MjpegView类是重头戏
	 */
	private void initMjpegView() {
		if (mis != null) {
			mjpegView.setSource(mis);// 设置数据来源
			// mjpegView.setDisplayMode(mjpegView.getDisplayMode());/*设置mjpegview的显示模式*/
			mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);/* 全屏 */
			/**
			 * setFps和getFps方法是为了在屏幕的右上角动态显示当前的帧率 如果我们只需观看画面，下面这句完全可以省去
			 */
			mjpegView.setFps(mjpegView.getFps());
			/**
			 * 调用mjpegView中的线程的run方法，开始显示画面
			 */
			mjpegView.startPlay();
		}
	}

	// 不注释掉的话从MainActivity退出到FlashActivity会出错
	/*
	 * @Override
	 *//**
	 * 当本Activity被finish时，该方法被激发 先调用mjpegview的stopplay方法，然后调用父类的onDestroy方法
	 */
	/*
	 * protected void onDestroy() { if (mjpegView != null) mjpegView.stopPlay();
	 * super.onDestroy(); }
	 */

	/*
	 * @Override
	 *//**
	 * 当RadioGroup的成员改变时，该方法被调用
	 * 
	 * @parm group ：RadioButton所在的组
	 * @parm checkedId：可以根据这个值来判断是哪个Button 1.先不Checked RadioButton控件
	 *       2.根据RadioGroup找到里面的Button成员的id 3.根据ID获得RadioBotton控件
	 *       4.根据点击不同的RadioBotton，执行相应的操作
	 */
	/*
	 * public void onCheckedChanged(RadioGroup group, int checkedId) { int
	 * radioButtonId = group.getCheckedRadioButtonId(); // 根据ID获取RadioButton的实例
	 * RadioButton rb = ((RadioButton) this.findViewById(radioButtonId));
	 * rb.setChecked(false);
	 * 
	 * switch (checkedId) { case R.id.radiobtn0: //shotSnap(rb); break; case
	 * R.id.radiobtn1:
	 *//**
	 * 这里没实现录像功能，只弹出一个"录像"Toast
	 */
	/*
	 * Toast.makeText(this, "录像", Toast.LENGTH_SHORT).show(); break; case
	 * R.id.radiobtn2: //scanPic(); /*最复杂的浏览
	 */
	/*
	 * break; case R.id.radiobtn3: //setFullScreen(rb); break; case
	 * R.id.radiobtn4:
	 *//**
	 * 跳转到settingActivi.java
	 */
	/*
	 * startActivity(new Intent(this, SettingActivity.class)); break; }
	 * 
	 * }
	 */

	@Override
	/**
	 * 回调上一个Activity的结果处理函数
	 * 当Activity调用resume()之前,正在重启Activity时调用该方法
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * 显示模式设置 1.获得当前显示模式 2.如果当前是全屏显示，点击"set"按钮，Button的text将为"标准",点击将切换到标准模式
	 * 3.如果当前是标准显示，点同一按钮，按钮变为"全屏",并切换到全屏显示模式
	 * 
	 * @param rb
	 */
	/*
	 * private void setFullScreen(RadioButton rb) { int mode =
	 * mjpegView.getDisplayMode();
	 * 
	 * if (mode == MjpegView.FULLSCREEN_MODE) {
	 *//**
	 * 可以在xml文件设置RadioButton的text，也可以调用控件的的setText方法设置其text
	 */
	/*
	 * //rb.setText(R.string.fullscreen);
	 * mainTab.setBackgroundResource(R.drawable.maintab_toolbar_bg);黑条背景
	 * mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);标准 } else {
	 * //rb.setText(R.string.standard);/*"标准"
	 */
	/*
	 * mainTab.setBackgroundColor(Color.TRANSPARENT);透明背景
	 * mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);全屏 } }
	 */

	/**
	 * 功能:拍照
	 * 
	 * @parm RadioButton rb 1.先disable RadioButton,再使能它
	 *       2.如果有SD卡，先在picturePath新建以当前系统时间为前缀的图片文件
	 *       3.调用mjpegview的getbitmap方法获得位图 4.位图 不为空，根据图片文件获得缓冲输出流
	 *       5.调用位图的压缩方法将图片压缩为JPEG格式，刷新缓存，关闭流
	 */
	private void shotSnap(/* RadioButton rb */) {
		Bitmap curBitmap = null;

		// rb.setEnabled(false);
		if (sdCardFile != null) {
			BufferedOutputStream bos;
			File captureFile = new File(picturePath + Generic.getSysNowTime()
					+ ".jpg");

			try {
				curBitmap = mjpegView.getBitmap();
				if (curBitmap != null) {
					bos = new BufferedOutputStream(new FileOutputStream(
							captureFile));/* File-->输出流 */
					curBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);/* 压缩 */
					bos.flush();
					bos.close();
					snd.play(photo_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
					Generic.showMsg(this, "拍照成功", true);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Generic.showMsg(this, "请检查SD卡", true);
		}
		// rb.setEnabled(true);
	}

	/**
	 * 功能:浏览、删除图片 先判断是否有SD卡
	 * 有SD卡就设置将之前创建好的picturePath传递给ScanPicActivity，下面就关心ScanPicActivity
	 * 
	 */
	/*
	 * private void scanPic() { if (sdCardFile != null) {
	 * 可以设置Intent的putExtra，来传递数据 startActivity(new Intent(this,
	 * ScanPicActivity.class).putExtra( "picturePath", picturePath)); } else {
	 * Generic.showMsg(this, "请检查SD卡", true); } }
	 */

	/*
	 * @Override
	 *//**
	 * 当点击手机的返回键，将调用此方法 跳到ExitActivity执行
	 * 
	 * @parm keyCode:键值
	 * @parm event:按键动作 如果是点击的返回键，新建并设置Intent，然后启动Activity的跳转，跳转成功返回真 失败返回假
	 * 
	 */
	/*
	 * public boolean onKeyDown(int keyCode, KeyEvent event) { if (keyCode ==
	 * KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { Intent intent =
	 * new Intent(); intent.setClass(this, ExitActivity.class);
	 * startActivity(intent); return true; }
	 * 
	 * return false; }
	 */
	private long exitTime = 0;

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			if ((System.currentTimeMillis() - exitTime) > 2500) {
				Toast.makeText(getApplicationContext(), "再按一次退出控制界面",
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();
			} else {
				finish();
				System.exit(0);
			}

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			shotSnap();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				&& event.getAction() == KeyEvent.ACTION_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

}
