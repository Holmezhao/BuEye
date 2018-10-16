/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，媒体库界面
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
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
		// 设置字体
		// 将字体文件保存在assets/fonts/目录下，创建Typeface对象
		Typeface typeFace = Typeface.createFromAsset(getAssets(),
				"fonts/HOBOSTD.ttf");
		// 使用字体
		photo_count.setTypeface(typeFace);
		video_count.setTypeface(typeFace);
		
		checkSdcard();

		// 返回
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
				intent.setClass(MediaActivity.this, PhotoListActivity.class);// 要启动VideoActivity
				MediaActivity.this.startActivity(intent);
				//scanPic();
			}
		});
		videos.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent();
				intent.setClass(MediaActivity.this, VideoListActivity.class);// 要启动VideoActivity
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
	 * 功能:获得SD路径
	 * 如果有SD卡，则创建存放图片的picturePath目录
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
	 * 功能:浏览、删除图片
	 * 先判断是否有SD卡
	 * 有SD卡就设置将之前创建好的picturePath传递给ScanPicActivity，下面就关心ScanPicActivity
	 * 
	 */
	private void scanPic() {
		if (sdCardFile != null) {
			/*可以设置Intent的putExtra，来传递数据*/
			startActivity(new Intent(this, ScanPicActivity.class).putExtra(
					"picturePath", picturePath));
		} else {
			Generic.showMsg(this, "请检查SD卡", true);
		}
	}
}
