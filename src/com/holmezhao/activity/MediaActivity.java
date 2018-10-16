/**
 * ���ߣ�Holmezhao
 * ��˾������Ƽ�
 * ��ʼʱ�䣺2015/8/18
 * ����ʱ�䣺2015/9/30
 * ���ܣ�1��ý������
 *     
 * ��ϵ��ʽ��  QQ��471023785
 *        ���䣺qitiansizhou@163.com
 *        �Ա���http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import java.io.File;

import tools.FileUtils;
import tools.Generic;
import tools.FileUtils.NoSdcardException;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MediaActivity extends Activity {

	private ImageButton photos, videos, back;
	private String photo_path, video_path;
	private TextView photo_count, video_count;
	private File sdCardFile = null;
	private String picturePath = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.media);
		Intent intent = getIntent();
		photos = (ImageButton) findViewById(R.id.photos);
		videos = (ImageButton) findViewById(R.id.videos);
		back = (ImageButton) findViewById(R.id.back1);
		photo_count = (TextView) findViewById(R.id.photocount);
		video_count = (TextView) findViewById(R.id.videocount);
		// ��������
		// �������ļ�������assets/fonts/Ŀ¼�£�����Typeface����
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/HOBOSTD.ttf");
		// ʹ������
		photo_count.setTypeface(typeFace);
		video_count.setTypeface(typeFace);
		
		checkSdcard();

		// ����
		back.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		photos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MediaActivity.this, PhotoListActivity.class);// Ҫ����VideoActivity
				MediaActivity.this.startActivity(intent);
				//scanPic();
			}
		});
		videos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MediaActivity.this, VideoListActivity.class);// Ҫ����VideoActivity
				MediaActivity.this.startActivity(intent);
			}
		});
		try {
			photo_path = new FileUtils().getSDCardRoot() + "BuEye";
			video_path = new FileUtils().getSDCardRoot() + "BuEye/video";
		} catch (NoSdcardException e) {
			e.printStackTrace();
		}
	
		photo_count.setText("" + (getFiles(photo_path) - 2));
		video_count.setText("" + getFiles(video_path));
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
		photo_count.setText("" + (getFiles(photo_path) - 2));
		video_count.setText("" + getFiles(video_path));
	}

	private int getFiles(String string) {
		// TODO Auto-generated method stub
		File file = new File(string);
		File[] files = file.listFiles();
		return files.length;
	}
	/**
	 * ����:���SD·��
	 * �����SD�����򴴽����ͼƬ��picturePathĿ¼
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
	 * ����:�����ɾ��ͼƬ
	 * ���ж��Ƿ���SD��
	 * ��SD�������ý�֮ǰ�����õ�picturePath���ݸ�ScanPicActivity������͹���ScanPicActivity
	 * 
	 */
	private void scanPic() {
		if (sdCardFile != null) {
			/*��������Intent��putExtra������������*/
			startActivity(new Intent(this, ScanPicActivity.class).putExtra(
					"picturePath", picturePath));
		} else {
			Generic.showMsg(this, "����SD��", true);
		}
	}
}
