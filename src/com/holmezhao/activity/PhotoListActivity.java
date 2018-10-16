/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，媒体库中，照片的文件管理器
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import tools.MyAdapter;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import tools.FileUtils;
import tools.FileUtils.NoSdcardException;

public class PhotoListActivity extends ListActivity implements
		OnItemLongClickListener {
	private String rootPath = null;
	private static final String media_path = "BuEye/";
	// 存储文件名称
	private ArrayList<String> names = null;
	// 存储文件路径
	private ArrayList<String> paths = null;
	private View view;
	public int MID;
	private EditText editText;
	private boolean haveFileFlag = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photolist);
		try {
			rootPath = new FileUtils().getSDCardRoot();
		} catch (NoSdcardException e) {
			e.printStackTrace();
		}
		// 显示文件列表
		// showFileDir(ROOT_PATH);
		getListView().setOnItemLongClickListener(this);// 注册长按事件
		showFileDir("" + rootPath + media_path);
	}

	private void showFileDir(String path) {
		names = new ArrayList<String>();
		paths = new ArrayList<String>();
		File file = new File(path);
		File[] files = file.listFiles();
		// 如果当前目录不是根目录
		/*
		 * if (!ROOT_PATH.equals(path)) { names.add("@1"); paths.add(ROOT_PATH);
		 * 
		 * names.add("@2"); paths.add(file.getParent()); }
		 */
		// 添加所有文件
		/*
		 * for (File f : files) { names.add(f.getName());
		 * paths.add(f.getPath()); }
		 */
		haveFileFlag = false;
		// 添加所有png文件
		for (File f : files) {
			String fileName = f.getName();
			if (fileName.endsWith(".jpg")) {
				haveFileFlag = true;
				names.add(f.getName());
				paths.add(f.getPath());
			}
		}
		if (haveFileFlag == false) {
			Toast.makeText(this, "No files!", Toast.LENGTH_LONG).show();
			return;
		}
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER); // 按照字母顺序排序
		Collections.sort(paths, String.CASE_INSENSITIVE_ORDER);
		this.setListAdapter(new MyAdapter(this, names, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String path = paths.get(position);
		File file = new File(path);
		// 文件存在并可读
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				// 显示子目录及文件
				showFileDir(path);
			} else {
				// 处理文件
				// fileHandle(file);
				// 打开文件
				openFile(file);
			}
		}
		// 没有权限
		else {
			Resources res = getResources();
			new AlertDialog.Builder(this).setTitle("Message")
					.setMessage(res.getString(R.string.no_permission))
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
		}
		super.onListItemClick(l, v, position, id);
	}

	@Override
	public boolean onItemLongClick(AdapterView<?> parent, View view,
			int position, long id) {
		// TODO Auto-generated method stub
		String path = paths.get(position);
		File file = new File(path);
		// 文件存在并可读
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				// 显示子目录及文件
				showFileDir(path);
			} else {
				// 处理文件
				fileHandle(file);
			}
		}
		// 没有权限
		else {
			Resources res = getResources();
			new AlertDialog.Builder(this).setTitle("Message")
					.setMessage(res.getString(R.string.no_permission))
					.setPositiveButton("OK", new OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

						}
					}).show();
		}
		return true;
	}

	// 对文件进行增删改
	private void fileHandle(final File file) {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// 打开文件
				/*
				 * if (which == 0) { openFile(file); } // 修改文件名 else
				 */if (which == 0) {
					LayoutInflater factory = LayoutInflater
							.from(PhotoListActivity.this);
					view = factory.inflate(R.layout.rename_dialog, null);
					editText = (EditText) view.findViewById(R.id.editText);
					editText.setText(file.getName());

					OnClickListener listener2 = new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							String modifyName = editText.getText().toString();
							final String fpath = file.getParentFile().getPath();
							final File newFile = new File(fpath + "/"
									+ modifyName);
							if (newFile.exists()) {
								// 排除没有修改情况
								if (!modifyName.equals(file.getName())) {
									new AlertDialog.Builder(
											PhotoListActivity.this)
											.setTitle("注意!")
											.setMessage("文件名已存在，是否覆盖？")
											.setPositiveButton(
													"确定",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															if (file.renameTo(newFile)) {
																showFileDir(fpath);
																displayToast("重命名成功！");
															} else {
																displayToast("重命名失败！");
															}
														}
													})
											.setNegativeButton(
													"取消",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {

														}
													}).show();
								}
							} else {
								if (file.renameTo(newFile)) {
									showFileDir(fpath);
									displayToast("重命名成功！");
								} else {
									displayToast("重命名失败！");
								}
							}
						}
					};
					AlertDialog renameDialog = new AlertDialog.Builder(
							PhotoListActivity.this).create();
					renameDialog.setView(view);
					renameDialog.setButton("确定", listener2);
					renameDialog.setButton2("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					renameDialog.show();
				}
				// 删除文件
				else {
					new AlertDialog.Builder(PhotoListActivity.this)
							.setTitle("注意!")
							.setMessage("确定要删除此文件吗？")
							.setPositiveButton("确定",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (file.delete()) {
												// 更新文件列表
												showFileDir(file.getParent());
												displayToast("删除成功！");
											} else {
												displayToast("删除失败！");
											}
										}
									})
							.setNegativeButton("取消",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {

										}
									}).show();
				}
			}
		};
		// 选择文件时，弹出增删该操作选项对话框
		String[] menu = {/* "打开文件", */"重命名", "删除文件" };
		new AlertDialog.Builder(PhotoListActivity.this).setTitle("请选择要进行的操作!")
				.setItems(menu, listener)
				.setPositiveButton("取消", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}

	// 打开文件
	private void openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		startActivity(intent);
	}

	// 获取文件mimetype
	private String getMIMEType(File file) {
		String type = "";
		String name = file.getName();
		// 文件扩展名
		String end = name.substring(name.lastIndexOf(".") + 1, name.length())
				.toLowerCase();
		if (end.equals("m4a") || end.equals("mp3") || end.equals("wav")) {
			type = "audio";
		} else if (end.equals("mp4") || end.equals("3gp")) {
			type = "video";
		} else if (end.equals("jpg") || end.equals("png") || end.equals("jpeg")
				|| end.equals("bmp") || end.equals("gif")) {
			type = "image";
		} else {
			// 如果无法直接打开，跳出列表由用户选择
			type = "*";
		}
		type += "/*";
		return type;
	}

	private void displayToast(String message) {
		Toast.makeText(PhotoListActivity.this, message, Toast.LENGTH_SHORT)
				.show();
	}

}