/**
 * ���ߣ�Holmezhao
 * ��˾������Ƽ�
 * ��ʼʱ�䣺2015/8/18
 * ����ʱ�䣺2015/9/30
 * ���ܣ�1��ý����У���Ƭ���ļ�������
 *     
 * ��ϵ��ʽ��  QQ��471023785
 *        ���䣺qitiansizhou@163.com
 *        �Ա���http://shop125061094.taobao.com/
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
	// �洢�ļ�����
	private ArrayList<String> names = null;
	// �洢�ļ�·��
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
		// ��ʾ�ļ��б�
		// showFileDir(ROOT_PATH);
		getListView().setOnItemLongClickListener(this);// ע�᳤���¼�
		showFileDir("" + rootPath + media_path);
	}

	private void showFileDir(String path) {
		names = new ArrayList<String>();
		paths = new ArrayList<String>();
		File file = new File(path);
		File[] files = file.listFiles();
		// �����ǰĿ¼���Ǹ�Ŀ¼
		/*
		 * if (!ROOT_PATH.equals(path)) { names.add("@1"); paths.add(ROOT_PATH);
		 * 
		 * names.add("@2"); paths.add(file.getParent()); }
		 */
		// ��������ļ�
		/*
		 * for (File f : files) { names.add(f.getName());
		 * paths.add(f.getPath()); }
		 */
		haveFileFlag = false;
		// �������png�ļ�
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
		Collections.sort(names, String.CASE_INSENSITIVE_ORDER); // ������ĸ˳������
		Collections.sort(paths, String.CASE_INSENSITIVE_ORDER);
		this.setListAdapter(new MyAdapter(this, names, paths));
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String path = paths.get(position);
		File file = new File(path);
		// �ļ����ڲ��ɶ�
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				// ��ʾ��Ŀ¼���ļ�
				showFileDir(path);
			} else {
				// �����ļ�
				// fileHandle(file);
				// ���ļ�
				openFile(file);
			}
		}
		// û��Ȩ��
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
		// �ļ����ڲ��ɶ�
		if (file.exists() && file.canRead()) {
			if (file.isDirectory()) {
				// ��ʾ��Ŀ¼���ļ�
				showFileDir(path);
			} else {
				// �����ļ�
				fileHandle(file);
			}
		}
		// û��Ȩ��
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

	// ���ļ�������ɾ��
	private void fileHandle(final File file) {
		OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// ���ļ�
				/*
				 * if (which == 0) { openFile(file); } // �޸��ļ��� else
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
								// �ų�û���޸����
								if (!modifyName.equals(file.getName())) {
									new AlertDialog.Builder(
											PhotoListActivity.this)
											.setTitle("ע��!")
											.setMessage("�ļ����Ѵ��ڣ��Ƿ񸲸ǣ�")
											.setPositiveButton(
													"ȷ��",
													new DialogInterface.OnClickListener() {
														@Override
														public void onClick(
																DialogInterface dialog,
																int which) {
															if (file.renameTo(newFile)) {
																showFileDir(fpath);
																displayToast("�������ɹ���");
															} else {
																displayToast("������ʧ�ܣ�");
															}
														}
													})
											.setNegativeButton(
													"ȡ��",
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
									displayToast("�������ɹ���");
								} else {
									displayToast("������ʧ�ܣ�");
								}
							}
						}
					};
					AlertDialog renameDialog = new AlertDialog.Builder(
							PhotoListActivity.this).create();
					renameDialog.setView(view);
					renameDialog.setButton("ȷ��", listener2);
					renameDialog.setButton2("ȡ��",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									// TODO Auto-generated method stub

								}
							});
					renameDialog.show();
				}
				// ɾ���ļ�
				else {
					new AlertDialog.Builder(PhotoListActivity.this)
							.setTitle("ע��!")
							.setMessage("ȷ��Ҫɾ�����ļ���")
							.setPositiveButton("ȷ��",
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (file.delete()) {
												// �����ļ��б�
												showFileDir(file.getParent());
												displayToast("ɾ���ɹ���");
											} else {
												displayToast("ɾ��ʧ�ܣ�");
											}
										}
									})
							.setNegativeButton("ȡ��",
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
		// ѡ���ļ�ʱ��������ɾ�ò���ѡ��Ի���
		String[] menu = {/* "���ļ�", */"������", "ɾ���ļ�" };
		new AlertDialog.Builder(PhotoListActivity.this).setTitle("��ѡ��Ҫ���еĲ���!")
				.setItems(menu, listener)
				.setPositiveButton("ȡ��", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {

					}
				}).show();
	}

	// ���ļ�
	private void openFile(File file) {
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);

		String type = getMIMEType(file);
		intent.setDataAndType(Uri.fromFile(file), type);
		startActivity(intent);
	}

	// ��ȡ�ļ�mimetype
	private String getMIMEType(File file) {
		String type = "";
		String name = file.getName();
		// �ļ���չ��
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
			// ����޷�ֱ�Ӵ򿪣������б����û�ѡ��
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