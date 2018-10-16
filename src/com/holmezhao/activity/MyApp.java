/**
 * 作者：Holmezhao
 * 公司：启天科技
 * 开始时间：2015/8/18
 * 结束时间：2015/9/30
 * 功能：1，存放整个程序的全局变量
 *     2，对外提供修改和调用全局变量的方法 
 *     
 * 联系方式：  QQ：471023785
 *        邮箱：qitiansizhou@163.com
 *        淘宝：http://shop125061094.taobao.com/
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

	/* 将两个Activity都用到的函数放到MyApp中，以简化代码 */
	public boolean sendData(byte[] data) throws IOException {
		OutputStream out = socket.getOutputStream();
		if (out == null)
			return false;// 输出流是否正常
		out.write(data);// 将data写入输出流
		return true;
	}

}
