/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1.欢迎页，显示欢迎图片，之后finish这个activity
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {

	private final long SPLASH_LENGTH = 2000;
	private Handler handler = new Handler();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash);

		// 使用handler的postDelayed实现延时跳转
		handler.postDelayed(new Runnable() {

			public void run() {
				Intent intent = new Intent(SplashActivity.this,
						MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, SPLASH_LENGTH);// 2秒后跳转至应用主界面MainActivity

	}
}