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
 * ��¼�ɹ��󣬽�ִ���������̳���Activityʵ����OnCheckedChangeListener������
 * OnCheckedChangeListener�ǵ���RadioGroup��RadioButton��ѡ�л�ı�ʱ��һ���ص��ӿڱ�������ִ��
 * ��Ҫ��1��RadioGroup��5��RadioButton��ʵ����5���ؼ�����ת����û��ʵ�־���Ĳ�����ֻ�ǵ���ĳ����ȥִ��
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
	private int i = 0;// �����
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
	private float gravity[] = new float[3];// �������ٶ������������ϵ�ͶӰ
	private Boolean right_hand_flag = false, send_flag = false,
			sound_flag = true;
	private Chronometer timer;
	Timer send_timer = new Timer();
	SoundPool snd = new SoundPool(10, AudioManager.STREAM_SYSTEM, 5);// ���ڲ������֣��Ȳ���
	int photo_sound, video_sound;// soundID
	private TextView leftText, rightText, forwardText, backwardText;
	private SeekBar left_right_seekbar = null, forward_backward_seekbar = null;
	private static final String TEMP_INFO = "temp_info";
	private char left_right_bias_progress, forward_backward_bias_progress;

	/**
	 * ����Activit��һ�δ����󣬸÷����������� 1.���ò��� 2.��ʼ����Ƶ������ 3.������R.java��id�ҵ��ؼ�
	 * 4.ΪRadioGroup���ü���������RadioButton�����»�ı�ʱ���������onCheckedChanged����
	 * 5.���SD������ʼ��mjpegview��ͼ�������Ϳɿ�����ػ�����
	 */
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ctrl);/* ����RadioGroup��5��RadioButton */

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
		// ���SharedPreferencesʵ��
		SharedPreferences sp = getSharedPreferences(TEMP_INFO,
				MODE_WORLD_READABLE);
		// ��SharedPreferences��ñ���¼������
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
		video_sound = snd.load(CtrlActivity.this, R.raw.ping_short, 0);// soundID��ֵ
		photo_sound = snd.load(CtrlActivity.this, R.raw.take_photo, 0);// soundID��ֵ
		BiasLayout = (RelativeLayout) findViewById(R.id.BiasLayout);
		biasButton = (ImageButton) findViewById(R.id.biasButton);
		leftText = (TextView) findViewById(R.id.leftText);
		rightText = (TextView) findViewById(R.id.rightText);
		forwardText = (TextView) findViewById(R.id.forwardText);
		backwardText = (TextView) findViewById(R.id.backwardText);
		// ��������
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/HOBOSTD.ttf");
		// ʹ������
		leftText.setTypeface(typeFace);
		rightText.setTypeface(typeFace);
		forwardText.setTypeface(typeFace);
		backwardText.setTypeface(typeFace);
		// ȡ�����õ���ƫ��seekbar
		left_right_seekbar = (SeekBar) findViewById(R.id.left_right_bias);
		forward_backward_seekbar = (SeekBar) findViewById(R.id.forward_backward_bias);
		// ��SharedPreferences��ñ���¼������,����seekbar�ĳ�ʼֵ
		left_right_bias_progress = (char) sp.getInt("left_right_bias_progress",
				120);
		forward_backward_bias_progress = (char) sp.getInt(
				"forward_backward_bias_progress", 120);
		// ����seekbar�ĳ�ʼֵ
		left_right_seekbar.setProgress(left_right_bias_progress);
		forward_backward_seekbar.setProgress(forward_backward_bias_progress);
		// ��ü�ʱ������
		timer = (Chronometer) findViewById(R.id.chronometer);
		// �����Ļ�ֱ���
		Display mDisplay = getWindowManager().getDefaultDisplay();
		W = mDisplay.getWidth();
		H = mDisplay.getHeight();
		if (right_hand_flag) {
			CtrlLayout1.setVisibility(View.GONE);
			CtrlLayout2.setVisibility(View.VISIBLE);
		}
		// ��ȡ�������д�������sensorManager����
		sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		// ��ȡ���ٶȴ���������
		accelerometerSensor = sensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		checkSdcard();
		initMjpegView();

		// ������ƫ�Ĵ���
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

		// ��ȡͼ��
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

		// ����
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
				// System.exit(0);//�Ͽ�����
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
		 * 0.5, 0, 0, 1); } if (video_flag == false) { // ����ʱ������
		 * timer.setBase(SystemClock.elapsedRealtime()); // ��ʼ��ʱ timer.start();
		 * video_flag = true; } else {// ֹͣ��ʱ timer.stop(); // ����ʱ������
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
				intent.setClass(CtrlActivity.this, PhotoListActivity.class);// ֱ�ӽ�����Ƭ���ļ������
				CtrlActivity.this.startActivity(intent);
				// scanPic();
			}
		});
		// �������
		ctrl_open.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				// TODO Auto-generated method stub
//				if (sound_flag) {
//					snd.play(video_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
//				}
				if (ctrl_flag == false) {
					ctrl_open.setImageDrawable(getResources().getDrawable(
							R.drawable.gyro_icon));
					Toast.makeText(CtrlActivity.this, "���ģʽ", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(CtrlActivity.this, "ҡ��ģʽ", Toast.LENGTH_SHORT).show();
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

		// �������ƹ���
		// ΪaccelerometerSensorע�������
		sensorManager.registerListener(new SensorEventListener() {
			// ����������һ�仯��ִ�еĴ���
			public void onSensorChanged(SensorEvent event) {

				// �����������ٶ��������ϵ�ͶӰֵ
				gravity[0] = event.values[0];
				gravity[1] = event.values[1];
				gravity[2] = event.values[2];

				// ����ʱ��ӡ

				/*
				 * System.out.println("x/////" + gravity[0]);
				 * System.out.println("y/////" + gravity[1]);
				 * System.out.println("z/////" + gravity[2]);
				 */

				if (gyro_flag == true) {
					if (gravity[0] < -0.5)// ��ǰ
					{
						pitch = (int) (512 - (gravity[0] + 0.5) * 220);
						if (pitch > 1024)
							pitch = 1024;
					} else if (gravity[0] > 0.5) // ����
					{
						pitch = (int) (512 - (gravity[0] - 0.5) * 220);
						if (pitch < 0)
							pitch = 0;
					} else {
						pitch = 512;
					}

					if (gravity[1] > 0.5)// ����
					{
						roll = (int) (512 + (gravity[1] - 0.5) * 180);
						if (roll > 1024)
							roll = 1024;
					} else if (gravity[1] < -0.5)// ����
					{
						roll = (int) (512 + (gravity[1] + 0.5) * 180);
						if (roll < 0)
							roll = 0;
					} else {
						roll = 512;
					}
				}
			}

			// ����������һ�仯��ִ�еĴ���
			public void onAccuracyChanged(Sensor sensor, int accuracy) {

			}
		}, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

		// ���
		motorCircle.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// ���£��õ���ǰ������λ��
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();
					// �õ�ͼ���λ��
					Loldl = CtrlActivity.this.motorCircle.getLeft();
					Loldr = CtrlActivity.this.motorCircle.getRight();
					Loldt = CtrlActivity.this.motorCircle.getTop();
					Loldb = CtrlActivity.this.motorCircle.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// ����
					int newX = (int) event.getRawX();// �õ���������ֵ
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// �õ�ͼ���λ��
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

					// �����ƶ�����λ�õ�ͼ��
					CtrlActivity.this.motorCircle
							.layout(newl, newt, newr, newb);

					yaw = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					motor = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);
					// motor = (int)((1024 -
					// 4*DensityUtil.px2dip(CtrlActivity.this, (newt+newb)/2) -
					// 180)/(844.0-180.0)*1024);
					// ��ӡ·��
					// System.out.println("motorCircle"+":"+ yaw +" "+ motor );
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();

					Loldt = newt;
					Loldb = newb;
					break;
				case MotionEvent.ACTION_UP: // ̧�𣬼�¼λ��
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

		// �Ҹ�
		directionCircle.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// ���£��õ���ǰ������λ��
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					// �õ�ͼ���λ��
					Roldl = CtrlActivity.this.directionCircle.getLeft();
					Roldr = CtrlActivity.this.directionCircle.getRight();
					Roldt = CtrlActivity.this.directionCircle.getTop();
					Roldb = CtrlActivity.this.directionCircle.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// ����
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// �õ�ͼ���λ��
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

					// �����ƶ�����λ�õ�ͼ��
					CtrlActivity.this.directionCircle.layout(newl, newt, newr,
							newb);

					roll = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					pitch = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);

					// ��ӡ·��
					// System.out.println("directionCircle"+":"+ roll +" "+
					// pitch );
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP: // ̧�𣬼�¼λ��
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

		// �Ҹ�
		motorCircle2.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// ���£��õ���ǰ������λ��
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();
					// �õ�ͼ���λ��
					Loldl = CtrlActivity.this.motorCircle2.getLeft();
					Loldr = CtrlActivity.this.motorCircle2.getRight();
					Loldt = CtrlActivity.this.motorCircle2.getTop();
					Loldb = CtrlActivity.this.motorCircle2.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// ����
					int newX = (int) event.getRawX();// �õ���������ֵ
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// �õ�ͼ���λ��
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

					// �����ƶ�����λ�õ�ͼ��
					CtrlActivity.this.motorCircle2.layout(newl, newt, newr,
							newb);

					yaw = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					motor = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);
					// motor = (int)((1024 -
					// 4*DensityUtil.px2dip(CtrlActivity.this, (newt+newb)/2) -
					// 180)/(844.0-180.0)*1024);
					// ��ӡ·��
					// System.out.println("motorCircle"+":"+ yaw +" "+ motor );
					LstartX = (int) event.getRawX();
					LstartY = (int) event.getRawY();

					Loldt = newt;
					Loldb = newb;
					break;
				case MotionEvent.ACTION_UP: // ̧�𣬼�¼λ��
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

		// ���
		directionCircle2.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:// ���£��õ���ǰ������λ��
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					// �õ�ͼ���λ��
					Roldl = CtrlActivity.this.directionCircle2.getLeft();
					Roldr = CtrlActivity.this.directionCircle2.getRight();
					Roldt = CtrlActivity.this.directionCircle2.getTop();
					Roldb = CtrlActivity.this.directionCircle2.getBottom();

					break;
				case MotionEvent.ACTION_MOVE:// ����
					int newX = (int) event.getRawX();
					int newY = (int) event.getRawY();

					int newt,
					newb,
					newl,
					newr;

					// �õ�ͼ���λ��
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

					// �����ƶ�����λ�õ�ͼ��
					CtrlActivity.this.directionCircle2.layout(newl, newt, newr,
							newb);

					roll = 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newl + newr) / 2);
					pitch = 1024 - 4 * DensityUtil.px2dip(CtrlActivity.this,
							(newt + newb) / 2);

					// ��ӡ·��
					// System.out.println("directionCircle"+":"+ roll +" "+
					// pitch );
					RstartX = (int) event.getRawX();
					RstartY = (int) event.getRawY();
					break;
				case MotionEvent.ACTION_UP: // ̧�𣬼�¼λ��
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
			send_timer.schedule(send_task, 0, 20);// �ڶ��������ǵȴ��೤ʱ�����ִ��run������������������֮��ÿ���೤ʱ�����һ��run��������λ���Ǻ��롣
			// myApp.setSendFlag(false);
		}
		// send_timer.schedule(send_task2,0,200);//200ms���һ��socket����
	}

	public void onBack() {//ģ��һ�����ؼ�
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
		// ��ñ༭��
		SharedPreferences.Editor editor = getSharedPreferences(TEMP_INFO,
				MODE_WORLD_WRITEABLE).edit();
		// ��EditText�е��ı�������ӵ��༭��
		editor.putInt("left_right_bias_progress",
				left_right_seekbar.getProgress());
		editor.putInt("forward_backward_bias_progress",
				forward_backward_seekbar.getProgress());
		editor.putBoolean("right", right_hand_flag);
		// �ύ�༭������
		editor.commit();
	}

	// ���ƽ������ٺ�ͣ��timer
	protected void onDestroy() {
		if (send_timer != null) {
			send_timer.cancel();
			send_timer = null;
		}
		super.onDestroy();
	}

	// �������̣߳�ÿ��200ms������һ֡����
	TimerTask send_task = new TimerTask() {

		public void run() {
			if (i > 99)
				i = 0;// i��0��99֮��ѭ��
			/*
			 * if (light_flag == false) { SettingsActivity.Send_Command16(i,
			 * motor, yaw, pitch, roll); } else {
			 * SettingsActivity.Send_Command16_LightOn(i, motor, yaw, pitch,
			 * roll); }
			 */
			SettingsActivity.Send_Command16(i, motor, yaw, pitch, roll,
					(char) left_right_seekbar.getProgress(),
					(char) forward_backward_seekbar.getProgress());// 120����û�о�ƫ
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
	 * ����:���SD·�� �����SD�����򴴽����ͼƬ��picturePathĿ¼
	 */
	private void checkSdcard() {
		sdCardFile = Generic.getSdCardFile();
		if (sdCardFile == null)
			Generic.showMsg(this, "�����SD��", true);
		else {
			picturePath = sdCardFile.getAbsolutePath() + "/BuEye/";
			File f = new File(picturePath);
			if (!(f.exists() && f.isDirectory()))
				f.mkdir();
		}
	}

	/**
	 * ����com/mjpeg/view��mjpegView.java����mjpegView���ڶ෽������ʼ���Զ���ؼ�com.mjpeg.view.
	 * MjpegView MjpegView������ͷϷ
	 */
	private void initMjpegView() {
		if (mis != null) {
			mjpegView.setSource(mis);// ����������Դ
			// mjpegView.setDisplayMode(mjpegView.getDisplayMode());/*����mjpegview����ʾģʽ*/
			mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);/* ȫ�� */
			/**
			 * setFps��getFps������Ϊ������Ļ�����ϽǶ�̬��ʾ��ǰ��֡�� �������ֻ��ۿ����棬���������ȫ����ʡȥ
			 */
			mjpegView.setFps(mjpegView.getFps());
			/**
			 * ����mjpegView�е��̵߳�run��������ʼ��ʾ����
			 */
			mjpegView.startPlay();
		}
	}

	// ��ע�͵��Ļ���MainActivity�˳���FlashActivity�����
	/*
	 * @Override
	 *//**
	 * ����Activity��finishʱ���÷��������� �ȵ���mjpegview��stopplay������Ȼ����ø����onDestroy����
	 */
	/*
	 * protected void onDestroy() { if (mjpegView != null) mjpegView.stopPlay();
	 * super.onDestroy(); }
	 */

	/*
	 * @Override
	 *//**
	 * ��RadioGroup�ĳ�Ա�ı�ʱ���÷���������
	 * 
	 * @parm group ��RadioButton���ڵ���
	 * @parm checkedId�����Ը������ֵ���ж����ĸ�Button 1.�Ȳ�Checked RadioButton�ؼ�
	 *       2.����RadioGroup�ҵ������Button��Ա��id 3.����ID���RadioBotton�ؼ�
	 *       4.���ݵ����ͬ��RadioBotton��ִ����Ӧ�Ĳ���
	 */
	/*
	 * public void onCheckedChanged(RadioGroup group, int checkedId) { int
	 * radioButtonId = group.getCheckedRadioButtonId(); // ����ID��ȡRadioButton��ʵ��
	 * RadioButton rb = ((RadioButton) this.findViewById(radioButtonId));
	 * rb.setChecked(false);
	 * 
	 * switch (checkedId) { case R.id.radiobtn0: //shotSnap(rb); break; case
	 * R.id.radiobtn1:
	 *//**
	 * ����ûʵ��¼���ܣ�ֻ����һ��"¼��"Toast
	 */
	/*
	 * Toast.makeText(this, "¼��", Toast.LENGTH_SHORT).show(); break; case
	 * R.id.radiobtn2: //scanPic(); /*��ӵ����
	 */
	/*
	 * break; case R.id.radiobtn3: //setFullScreen(rb); break; case
	 * R.id.radiobtn4:
	 *//**
	 * ��ת��settingActivi.java
	 */
	/*
	 * startActivity(new Intent(this, SettingActivity.class)); break; }
	 * 
	 * }
	 */

	@Override
	/**
	 * �ص���һ��Activity�Ľ��������
	 * ��Activity����resume()֮ǰ,��������Activityʱ���ø÷���
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
	}

	/**
	 * ��ʾģʽ���� 1.��õ�ǰ��ʾģʽ 2.�����ǰ��ȫ����ʾ�����"set"��ť��Button��text��Ϊ"��׼",������л�����׼ģʽ
	 * 3.�����ǰ�Ǳ�׼��ʾ����ͬһ��ť����ť��Ϊ"ȫ��",���л���ȫ����ʾģʽ
	 * 
	 * @param rb
	 */
	/*
	 * private void setFullScreen(RadioButton rb) { int mode =
	 * mjpegView.getDisplayMode();
	 * 
	 * if (mode == MjpegView.FULLSCREEN_MODE) {
	 *//**
	 * ������xml�ļ�����RadioButton��text��Ҳ���Ե��ÿؼ��ĵ�setText����������text
	 */
	/*
	 * //rb.setText(R.string.fullscreen);
	 * mainTab.setBackgroundResource(R.drawable.maintab_toolbar_bg);��������
	 * mjpegView.setDisplayMode(MjpegView.KEEP_SCALE_MODE);��׼ } else {
	 * //rb.setText(R.string.standard);/*"��׼"
	 */
	/*
	 * mainTab.setBackgroundColor(Color.TRANSPARENT);͸������
	 * mjpegView.setDisplayMode(MjpegView.FULLSCREEN_MODE);ȫ�� } }
	 */

	/**
	 * ����:����
	 * 
	 * @parm RadioButton rb 1.��disable RadioButton,��ʹ����
	 *       2.�����SD��������picturePath�½��Ե�ǰϵͳʱ��Ϊǰ׺��ͼƬ�ļ�
	 *       3.����mjpegview��getbitmap�������λͼ 4.λͼ ��Ϊ�գ�����ͼƬ�ļ���û��������
	 *       5.����λͼ��ѹ��������ͼƬѹ��ΪJPEG��ʽ��ˢ�»��棬�ر���
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
							captureFile));/* File-->����� */
					curBitmap.compress(Bitmap.CompressFormat.JPEG, 80, bos);/* ѹ�� */
					bos.flush();
					bos.close();
					snd.play(photo_sound, (float) 0.5, (float) 0.5, 0, 0, 1);
					Generic.showMsg(this, "���ճɹ�", true);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Generic.showMsg(this, "����SD��", true);
		}
		// rb.setEnabled(true);
	}

	/**
	 * ����:�����ɾ��ͼƬ ���ж��Ƿ���SD��
	 * ��SD�������ý�֮ǰ�����õ�picturePath���ݸ�ScanPicActivity������͹���ScanPicActivity
	 * 
	 */
	/*
	 * private void scanPic() { if (sdCardFile != null) {
	 * ��������Intent��putExtra������������ startActivity(new Intent(this,
	 * ScanPicActivity.class).putExtra( "picturePath", picturePath)); } else {
	 * Generic.showMsg(this, "����SD��", true); } }
	 */

	/*
	 * @Override
	 *//**
	 * ������ֻ��ķ��ؼ��������ô˷��� ����ExitActivityִ��
	 * 
	 * @parm keyCode:��ֵ
	 * @parm event:�������� ����ǵ���ķ��ؼ����½�������Intent��Ȼ������Activity����ת����ת�ɹ������� ʧ�ܷ��ؼ�
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
				Toast.makeText(getApplicationContext(), "�ٰ�һ���˳����ƽ���",
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
