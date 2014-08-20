package com.stu;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import com.net.ConnectionHelper;

class StuChooseDocState implements StuState {
	private String cookie;
	private Map<String, String> inputValueMap;
	
	public StuChooseDocState() {}
	
	public StuChooseDocState(String cookie) {
		this.cookie = cookie;
	}
	
	@Override
	public StuStateEnum getState() {
		return StuStateEnum.CHOOSE_DOC;
	}
	
	@Override
	public StuStateEnum nextState() {
		boolean result = doGet(StuURL.STU_MENU_URL);
		result &= doPost(StuURL.CHOOSE_DOC_URL);
		
		return result ? StuStateEnum.CHOICE_COURSE : StuStateEnum.ERROR;
	}
	
	@Override
	public boolean refresh() {
		return doGet(StuURL.CHOOSE_DOC_URL);
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
		HttpURLConnection URLConn = ConnectionHelper.doGet(StuURL.CHOOSE_DOC_URL, cookie, referer);
		try {
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			result = (URLConn.getResponseCode() == 200);
			inputValueMap = ConnectionHelper.getInputValueMap(URLConn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("doGet :¡@" + result);
		return result;
	}
	
	private boolean doPost(String referer) {
		boolean result = false;
		try {
			String viewState = URLEncoder.encode(inputValueMap.get("__VIEWSTATE"), "utf-8");
			String data =  "__VIEWSTATE=" + viewState + 
					"&Button1=%E9%80%B2%E5%85%A5%E9%81%B8%E8%AA%B2%E7%B3%BB%E7%B5%B1";
			HttpURLConnection URLConn = ConnectionHelper.doPost(StuURL.CHOOSE_DOC_URL, data, cookie, referer);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			result = (StuURL.CHOICE_COURSE_URL.equals(StuURL.WEB_SERVER_URL + URLConn.getHeaderField("Location"))); // 302
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}

}
