/**
 * ���ߣ�Holmezhao
 * ��˾������Ƽ�
 * ��ʼʱ�䣺2015/8/18
 * ����ʱ�䣺2015/9/30
 * ���ܣ�1��������������ȫ�ֱ���
 *     2�������ṩ�޸ĺ͵���ȫ�ֱ����ķ��� 
 *     
 * ��ϵ��ʽ��  QQ��471023785
 *        ���䣺qitiansizhou@163.com
 *        �Ա���http://shop125061094.taobao.com/
 */
package com.holmezhao.activity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import android.app.Application;

public class MyApp extends Application {

	private Socket socket = null;
	private Boolean right_hand_flag = false;
	private Boolean send_flag = false;
	private Boolean wifi_state = false;
	private Boolean sound_flag = true;

	public Boolean getWifiState() {
		return wifi_state;
	}

	public void setWifiState(Boolean w) {
		this.wifi_state = w;
	}

	public Boolean getRightHandMode() {
		return right_hand_flag;
	}

	public void setRightHandMode(Boolean b) {
		this.right_hand_flag = b;
	}

	public Boolean getSendFlag() {
		return send_flag;
	}

	public void setSendFlag(Boolean sf) {
		this.send_flag = sf;
	}
	
	public Boolean getSoundFlag() {
		return sound_flag;
	}

	public void setSoundFlag(Boolean sf) {
		this.sound_flag = sf;
	}

	/*public Socket getSocket() {
		return socket;
	}

	public void setSocket(Socket s) {
		this.socket = s;
	}*/

	/* ������Activity���õ��ĺ����ŵ�MyApp�У��Լ򻯴��� */
	public boolean sendData(byte[] data) throws IOException {
		OutputStream out = socket.getOutputStream();
		if (out == null)
			return false;// ������Ƿ�����
		out.write(data);// ��dataд�������
		return true;
	}

}
