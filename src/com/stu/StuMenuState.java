package com.stu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import com.net.ConnectionHelper;

public class StuMenuState implements StuState {
	private String cookie;
	private Map<String, String> inputValueMap;

	public StuMenuState() {};
	
	public StuMenuState(String cookie) {
		this.cookie = cookie;
	}
	
	@Override
	public StuStateEnum getState() {
		return StuStateEnum.MENU;
	}
	
	@Override
	public StuStateEnum nextState() {
		doGet(StuURL.STU_MENU_URL);
		return doPost(StuURL.STU_MENU_URL);
	}
	
	public boolean refresh() {
		return doGet(StuURL.STU_MENU_URL);
	}
	
	@Override
	public String getCookie() {
		return cookie;
	}

	@Override
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	private boolean doGet(String referer) {
		boolean result = false;
		HttpURLConnection URLConn = ConnectionHelper.doGet(StuURL.STU_MENU_URL, cookie, referer);
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
	
	private StuStateEnum doPost(String referer) {
		try {
			String viewState = URLEncoder.encode(inputValueMap.get("__VIEWSTATE"), "utf-8");
			String data = "__VIEWSTATE=" + viewState + "&Button1=%E9%81%B8%E8%AA%B2%E7%B3" +
					"%BB%E7%B5%B1%28%E9%81%B8%E8%AA%B2%29";
			HttpURLConnection URLConn = ConnectionHelper.doPost(StuURL.STU_MENU_URL, data, 
					cookie, referer);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			String redirect = StuURL.WEB_SERVER_URL + URLConn.getHeaderField("Location");
			if(StuURL.CHOOSE_DOC_URL.equals(redirect)){
				return StuStateEnum.CHOOSE_DOC;
			} else if(StuURL.CHOICE_COURSE_URL.equals(redirect)) {
				return StuStateEnum.CHOICE_COURSE;
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return StuStateEnum.ERROR;
	}
}
