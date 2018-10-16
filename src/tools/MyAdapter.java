/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，实现文件管理器的BaseAdapter
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
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
	// 存储文件名称
	private ArrayList<String> names = null;
	// 存储文件路径
	private ArrayList<String> paths = null;
	private AsyncTask<String, Void, Bitmap> bAsyncTask;

	// 参数初始化
	public MyAdapter(Context context, ArrayList<String> na, ArrayList<String> pa) {
		names = na;
		paths = pa;
		/*directory = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.d);
		file = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.f);
		// 缩小图片
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
							160, 120, MediaStore.Images.Thumbnails.MICRO_KIND));*/  //这种视频生成缩略图太慢了！！先注释掉
					//另一种做法
					//bAsyncTask=new BmpAsyncTask(holder.image);
				 	//bAsyncTask.execute(f.getPath());
				}
				holder.file_size.setText(setFileSize(getFileSize(f)));// 文件大小
				
				 
				 
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
	 * 文件大小单位转换
	 * 
	 * @param size
	 * @return
	 */
	@SuppressLint("UseValueOf")
	public String setFileSize(long size) {
		DecimalFormat df = new DecimalFormat("###.###");
		float f = ((float) size / (float) (1024 * 1024));/* 判断是否<1M */
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
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 
	 * 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。
	 * 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	private Bitmap getImageThumbnail(String imagePath, int width, int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
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
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/**
	 * 获取视频的缩略图 先通过ThumbnailUtils来创建一个视频的缩略图，然后再利用ThumbnailUtils来生成指定大小的缩略图。
	 * 如果想要的缩略图的宽和高都小于MICRO_KIND，则类型要使用MICRO_KIND作为kind的值，这样会节省内存。
	 * 
	 * @param videoPath
	 *            视频的路径
	 * @param width
	 *            指定输出视频缩略图的宽度
	 * @param height
	 *            指定输出视频缩略图的高度度
	 * @param kind
	 *            参照MediaStore.Images.Thumbnails类中的常量MINI_KIND和MICRO_KIND。
	 *            其中，MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
	 * @return 指定大小的视频缩略图
	 */
	private Bitmap getVideoThumbnail(String videoPath, int width, int height,
			int kind) {
		Bitmap bitmap = null;
		// 获取视频的缩略图
		bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
		System.out.println("w" + bitmap.getWidth());
		System.out.println("h" + bitmap.getHeight());
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}
}