/**
 * ���ߣ�Holmezhao
 * ��˾������Ƽ�
 * ��ʼʱ�䣺2015/8/18
 * ����ʱ�䣺2015/9/30
 * ���ܣ�ʵ���������ϵİ�������
 *     
 * ��ϵ��ʽ��  QQ��471023785
 *        ���䣺qitiansizhou@163.com
 *        �Ա���http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import java.util.ArrayList;
import java.util.List;

import tools.ViewPagerAdapter;
import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class HelpActivity extends Activity implements OnClickListener,
		OnPageChangeListener {

	private ViewPager vp;
	private ViewPagerAdapter vpAdapter;
	private List<View> views;

	// ����ͼƬ��Դ
	private static final int[] pics = { R.drawable.imager1, R.drawable.imager2,
			R.drawable.imager3 };

	// �ײ�СԲ��ͼƬ
	private ImageView[] dots;

	// ��¼��ǰѡ��λ��
	private int currentIndex;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.help);
		views = new ArrayList<View>();
		LinearLayout.LayoutParams mParams = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);

		// ��ʼ������ͼƬ�б�
		for (int i = 0; i < pics.length; i++) {
			ImageView iv = new ImageView(this);
			iv.setLayoutParams(mParams);// ȥ��Ҳ����
			iv.setImageResource(pics[i]);
			views.add(iv);
		}
		
		vp = (ViewPager) findViewById(R.id.viewpager);
		// ��ʼ��Adapter
		vpAdapter = new ViewPagerAdapter(views);
		vp.setAdapter(vpAdapter);
		// �󶨻ص�
		vp.setOnPageChangeListener(this);

		// ��ʼ���ײ�С��
		initDots();
	}

	private void initDots() {
		LinearLayout ll = (LinearLayout) findViewById(R.id.dotgroup);

		dots = new ImageView[pics.length];

		// ѭ��ȡ��С��ͼƬ
		for (int i = 0; i < pics.length; i++) {
			dots[i] = (ImageView) ll.getChildAt(i);
			dots[i].setEnabled(true);// ����Ϊ��ɫ
			dots[i].setOnClickListener(this);
			dots[i].setTag(i);// ����λ��tag������ȡ���뵱ǰλ�ö�Ӧ
		}

		currentIndex = 0;
		dots[currentIndex].setEnabled(false);// ����Ϊ��ɫ����ѡ��״̬
	}

	/**
	 * ���õ�ǰ������ҳ
	 */
	private void setCurView(int position) {
		if (position < 0 || position >= pics.length) {
			return;
		}

		vp.setCurrentItem(position);
	}

	/**
	 * ��ֻ��ǰ����С���ѡ��
	 */
	private void setCurDot(int positon) {
		if (positon < 0 || positon > pics.length - 1 || currentIndex == positon) {
			return;
		}

		// ���Ƶײ���СԲ��
		dots[positon].setEnabled(false);
		dots[currentIndex].setEnabled(true);

		currentIndex = positon;
	}

	// ������״̬�ı�ʱ����
	@Override
	public void onPageScrollStateChanged(int arg0) {
		// TODO Auto-generated method stub

	}

	// ����ǰҳ�汻����ʱ����
	@Override
	public void onPageScrolled(int arg0, float arg1, int arg2) {
		// TODO Auto-generated method stub

	}

	// ���µ�ҳ�汻ѡ��ʱ����
	@Override
	public void onPageSelected(int arg0) {
		// ���õײ�С��ѡ��״̬
		setCurDot(arg0);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.back2) {
			finish();
			return;
		} else {
			int position = (Integer) v.getTag();
			setCurView(position);
			setCurDot(position);
		}
	}

}
