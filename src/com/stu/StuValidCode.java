package com.stu;

import java.awt.image.BufferedImage;
import java.net.HttpURLConnection;

import javax.imageio.ImageIO;

import com.net.ConnectionHelper;

public class StuValidCode {
	public static final String VCODE_URL = 
			"https://stu255.ntust.edu.tw/ntust_stu/VCode.aspx";
	
	public static BufferedImage getValidCodeImage(String cookie, String referer) {
		BufferedImage image = null;
		try {
			HttpURLConnection URLConn = ConnectionHelper.doGet(VCODE_URL, cookie, referer);
			image = ImageIO.read(URLConn.getInputStream());	
		} catch(Exception e) {
			e.printStackTrace();
		}
		return image;
	}
	
	
}
