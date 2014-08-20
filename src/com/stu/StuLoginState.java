package com.stu;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;


import com.net.ConnectionHelper;
import com.ocr.CaptchaDecoder;


class StuLoginState implements StuState {

	private String cookie;
	private Map<String, String> inputValueMap;
	
	private boolean hasLogin;
	
	public StuLoginState() {};
	
	public StuLoginState(String cookie) {
		this.cookie = cookie;
	}
	
	public boolean login(String studentNo, String idCard, String birthday, String password) {
		boolean result = doGet(null);
		String codeBox = getValidCode(StuURL.STU_URL);
		
		if(codeBox == null) {
			System.out.println("Captcha Decoder : decode failed!");
			return false;
		}
		
		result &= doPost(studentNo, idCard, birthday, password, codeBox, StuURL.STU_URL);
		hasLogin = result;
		System.out.println(result ? "login succeeded!" : "login failed!");
		return result;
	}
	
	@Override
	public StuStateEnum getState() {
		return StuStateEnum.LOGIN;
	}

	@Override
	public StuStateEnum nextState() {
		return hasLogin() ? StuStateEnum.MENU : StuStateEnum.ERROR;
	}
	
	@Override
	public boolean refresh() {
		return doGet(StuURL.STU_URL);
	}
	
	@Override
	public String getCookie() {
		return cookie;
	}

	@Override
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	public boolean hasLogin() {
		return hasLogin;
	}
	
	private boolean doGet(String referer) {
		boolean result = false;
		HttpURLConnection URLConn = ConnectionHelper.doGet(StuURL.STU_URL, cookie, referer);
		try {
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			result = (URLConn.getResponseCode() == 200);
			inputValueMap = ConnectionHelper.getInputValueMap(URLConn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean doPost(String studentNo, String idCard, String birthday, 
			String password, String codeBox, String referer) {
		boolean result = false;

		try {
			String viewState = URLEncoder.encode(inputValueMap.get("__VIEWSTATE"), "utf-8");
			String data = "__VIEWSTATE=" + viewState + "&studentno=" + studentNo + 
					"&idcard=" + idCard + "&birthday=" + birthday + "&password=" + password + 
					"&code_box=" + codeBox + "&Button1=%E7%99%BB%E5%85%A5%E7%B3%BB%E7%B5%B1";
			HttpURLConnection URLConn = ConnectionHelper.doPost(StuURL.STU_URL, data, cookie, referer);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			//print(new InputStreamReader(URLConn.getInputStream()));
			result = (StuURL.STU_MENU_URL.equals(StuURL.WEB_SERVER_URL + URLConn.getHeaderField("Location"))); // 302
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	/*
	// temp :
	private String getValidCode(String referer) {
		ImageIcon icon = new ImageIcon(StuValidCode.getValidCodeImage(cookie, referer));
		String validCode = (String)JOptionPane.showInputDialog(null, "請輸入驗證碼", null, 0, icon, null, null);
		return validCode;
	}
	*/
	
	
	private String getValidCode(String referer) {
		final int MAX_TRY_NUM = 10;
		BufferedImage image = null;
		String validCode = null;
		
		for(int i = 0; i < MAX_TRY_NUM && validCode == null; i++) {
			image = StuValidCode.getValidCodeImage(cookie, referer);
			validCode = CaptchaDecoder.decode(image);
		}
		
		System.out.println("Captcha Decoder : " + validCode);
		
		if(validCode == null) {
			System.out.println("由於辨識失敗冊數已達上限(" + MAX_TRY_NUM + ")，改由手動輸入驗證碼");
			ImageIcon icon = new ImageIcon(image);
			validCode = (String)JOptionPane.showInputDialog(null, "由於辨識失敗冊數已達上限(" + 
					MAX_TRY_NUM + ")，請手動輸入驗證碼", null, 0, icon, null, null);
			System.out.println("Captcha : " + validCode);
		}
		
		return validCode;
	}
	
	
	/*
	 * for test captcha decoder
	private String getValidCode(String referer) {
		final int MAX_TRY_NUM = 100;
		BufferedImage image = null;
		String validCode = null;
		
		int successConuter = 0;
		
		for(int i = 0; i < MAX_TRY_NUM; i++) {
			image = StuValidCode.getValidCodeImage(cookie, referer);
			validCode = CaptchaDecoder.decode(image);
			String s = "Samples : " + i + " Captcha : " + validCode;
			ImageIcon icon = new ImageIcon(image);
			int result = JOptionPane.showConfirmDialog(
					null, s, null, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, icon);
			if(result == JOptionPane.YES_OPTION) {
				successConuter++;
			}
			
		}
		
		JOptionPane.showMessageDialog(null, "Samples : " + MAX_TRY_NUM + ", Accuracy : " + 
				((double) successConuter / (double) MAX_TRY_NUM));
		
		return null;
	}
	*/
	
}
