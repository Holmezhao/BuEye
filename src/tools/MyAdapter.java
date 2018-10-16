/**
 * ���ߣ�Holmezhao
 * ��˾������Ƽ�
 * ��ʼʱ�䣺2015/8/18
 * ����ʱ�䣺2015/9/30
 * ���ܣ�1��ʵ���ļ���������BaseAdapter
 *     
 * ��ϵ��ʽ��  QQ��471023785
 *        ���䣺qitiansizhou@163.com
 *        �Ա���http://shop125061094.taobao.com/
 */
package tools;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.holmezhao.activity.R;

public class MyAdapter extends BaseAdapter {
	private LayoutInflater inflater;
	//private Bitmap directory, file;
	// �洢�ļ�����
	private ArrayList<String> names = null;
	// �洢�ļ�·��
	private ArrayList<String> paths = null;
	private AsyncTask<String, Void, Bitmap> bAsyncTask;

	// ������ʼ��
	public MyAdapter(Context context, ArrayList<String> na, ArrayList<String> pa) {
		names = na;
		paths = pa;
		/*directory = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.d);
		file = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.f);
		// ��СͼƬ
		directory = small(directory, 0.16f);
		file = small(file, 0.1f);*/
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return names.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return names.get(position);
	}

	public int getItemViewType(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.file, null);
			holder = new ViewHolder();
			holder.text = (TextView) convertView.findViewById(R.id.textView);
			holder.image = (ImageView) convertView.findViewById(R.id.imageView);
			holder.file_size = (TextView) convertView
					.findViewById(R.id.file_size);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		File f = new File(paths.get(position).toString());
		if (names.get(position).equals("@1")) {
			holder.text.setText("/");
			//holder.image.setImageBitmap(directory);
		} else if (names.get(position).equals("@2")) {
			holder.text.setText("..");
			//holder.image.setImageBitmap(directory);
		} else {
			holder.text.setText(f.getName());
			if (f.isDirectory()) {
				//holder.image.setImageBitmap(directory);
			} else if (f.isFile()) {
				String fileName = f.getName();
				if (fileName.endsWith(".jpg")) {
					holder.image.setImageBitmap(getImageThumbnail(f.getPath(),
							160, 120));
				} else if (fileName.endsWith(".mp4")) {
					/*holder.image.setImageBitmap(getVideoThumbnail(f.getPath(),
							160, 120, MediaStore.Images.Thumbnails.MICRO_KIND));*/  //������Ƶ��������ͼ̫���ˣ�����ע�͵�
					//��һ������
					//bAsyncTask=new BmpAsyncTask(holder.image);
				 	//bAsyncTask.execute(f.getPath());
				}
				holder.file_size.setText(setFileSize(getFileSize(f)));// �ļ���С
				
				 
				 
			} else {
				System.out.println(f.getName());
			}
		}
		return convertView;
	}

	private class ViewHolder {
		private TextView text;
		private ImageView image;
		private TextView file_size;
	}

	private Bitmap small(Bitmap map, float num) {
		Matrix matrix = new Matrix();
		matrix.postScale(num, num);
		return Bitmap.createBitmap(map, 0, 0, map.getWidth(), map.getHeight(),
				matrix, true);
	}

	public static long getFileSize(File file) {
		long size = 0;
		if (file.isFile())
			size = file.length();
		return size;
	}

	/**
	 * �ļ���С��λת��
	 * 
	 * @param size
	 * @return
	 */
	@SuppressLint("UseValueOf")
	public String setFileSize(long size) {
		DecimalFormat df = new DecimalFormat("###.###");
		float f = ((float) size / (float) (1024 * 1024));/* �ж��Ƿ�<1M */
		if (size < 1024) {
			return df.format(new Float(size).doubleValue()) + "B";
		} else if (f < 1.0) {
			float f1 = ((float) size / (float) (1024));// 1KB
			return df.format(new Float(f1).doubleValue()) + "KB";
		} else if (f / (float) (1024) < 1.0) {
			return df.format(new Float(f).doubleValue()) + "M";
		} else {
			return df.format(new Float(f / (float) (1024)).doubleValue()) + "G";
		}
	}

	/**
	 * ����ָ����ͼ��·���ʹ�С����ȡ����ͼ �˷���������ô��� 
	 * 1.
	 * ʹ�ý�С���ڴ�ռ䣬��һ�λ�ȡ��bitmapʵ����Ϊnull��ֻ��Ϊ�˶�ȡ��Ⱥ͸߶ȣ�
	 * �ڶ��ζ�ȡ��bitmap�Ǹ��ݱ���ѹ������ͼ�񣬵����ζ�ȡ��bitmap����Ҫ������ͼ��
	 * 2.
	 * ����ͼ����ԭͼ������û�����죬����ʹ����2.2�汾���¹���ThumbnailUtils��ʹ ������������ɵ�ͼ�񲻻ᱻ���졣
	 * 
	 * @param imagePath
	 *            ͼ���·��
	 * @param width
	 *            ָ�����ͼ��Ŀ��
	 * @param height
	 *            ָ�����ͼ��ĸ߶�
	 * @return ���ɵ�����ͼ
	 */
	private Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// ��ȡ���ͼƬ�Ŀ�͸ߣ�ע��˴���bitmapΪnull
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // ��Ϊ false
		// �������ű�
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// ���¶���ͼƬ����ȡ���ź��bitmap��ע�����Ҫ��options.inJustDecodeBounds ��Ϊ false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// ����ThumbnailUtils����������ͼ������Ҫָ��Ҫ�����ĸ�Bitmap����
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * ��ȡ��Ƶ������ͼ ��ͨ��ThumbnailUtils������һ����Ƶ������ͼ��Ȼ��������ThumbnailUtils������ָ����С������ͼ��
	 * �����Ҫ������ͼ�Ŀ�͸߶�С��MICRO_KIND��������Ҫʹ��MICRO_KIND��Ϊkind��ֵ���������ʡ�ڴ档
	 * 
	 * @param videoPath
	 *            ��Ƶ��·��
	 * @param width
	 *            ָ�������Ƶ����ͼ�Ŀ��
	 * @param height
	 *            ָ�������Ƶ����ͼ�ĸ߶ȶ�
	 * @param kind
	 *            ����MediaStore.Images.Thumbnails���еĳ���MINI_KIND��MICRO_KIND��
	 *            ���У�MINI_KIND: 512 x 384��MICRO_KIND: 96 x 96
	 * @return ָ����С����Ƶ����ͼ
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// ��ȡ��Ƶ������ͼ
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}