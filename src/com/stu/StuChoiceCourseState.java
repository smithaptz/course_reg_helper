package com.stu;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.Map;

import com.net.ConnectionHelper;

class StuChoiceCourseState implements StuState {
	private String cookie;
	private Map<String, String> inputValueMap;
	
	public static final int MSG_ERROR = -1;
	public static final int MSG_SUCCESS = 1;
	public static final int MSG_SCHEDULE_CLASS_REPEAT = 2;
	public static final int MSG_SCHEDULE_CONFLICT = 3;
	public static final int MSG_CONDITION_NOT_SATISFY = 4;
	public static final int MSG_CLASS_FULL = 5;
	public static final int MSG_CLASS_NOT_FOUND = 6;
	public static final int MSG_CLASS_CODE_ERROR = 7;
	public static final int MSG_CONNECT_ERROR = 8;
	
	public StuChoiceCourseState() {}
	
	public StuChoiceCourseState(String cookie) {
		this.cookie = cookie;
	}
	
	@Override
	public StuStateEnum getState() {
		return StuStateEnum.CHOICE_COURSE;
	}

	@Override
	public StuStateEnum nextState() {
		return StuStateEnum.NULL;
	}
	
	@Override
	public boolean refresh() {
		return doGet(StuURL.CHOICE_COURSE_URL);
	}
	
	@Override
	public String getCookie() {
		return cookie;
	}

	@Override
	public void setCookie(String cookie) {
		this.cookie = cookie;
	}
	
	public int registerCourse(String courseCode) {
		doGet(StuURL.CHOICE_COURSE_URL);
		return doPost(courseCode, StuURL.CHOICE_COURSE_URL);
	}
	
	private boolean doGet(String referer) {
		boolean result = false;
		HttpURLConnection URLConn = ConnectionHelper.doGet(StuURL.CHOICE_COURSE_URL, cookie, referer);
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
	
	private int doPost(String courseCode, String referer) {
		try {
			String viewState = URLEncoder.encode(inputValueMap.get("__VIEWSTATE"), "utf-8");
			String data =  "__VIEWSTATE=" + viewState + "&courseno=" + courseCode + "&B_add=%E5%8A%A0%E9%81%B8";
			HttpURLConnection URLConn = ConnectionHelper.doPost(StuURL.CHOICE_COURSE_URL, data, cookie, referer);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			if(URLConn.getResponseCode() == 200) {
				return readResult(new InputStreamReader(URLConn.getInputStream(), "utf-8"));
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return MSG_CONNECT_ERROR;
	}
	
	private int readResult(InputStreamReader input) {
		BufferedReader buf = new BufferedReader(input);
		String line;
		
		try {
			while((line = buf.readLine()) != null) {
				if(line.contains("span id=\"err_msg\"")) {
					System.out.println("result : " + line);
					if(line.contains("加選成功")) 
						return MSG_SUCCESS;
					if(line.contains("額滿"))
						return MSG_CLASS_FULL;
					if(line.contains("重複選課"))
						return MSG_SCHEDULE_CLASS_REPEAT;
					if(line.contains("衝堂"))
						return MSG_SCHEDULE_CONFLICT;
					if(line.contains("不符合條件"))
						return MSG_CONDITION_NOT_SATISFY;
					if(line.contains("找不到"))
						return MSG_CLASS_NOT_FOUND;
					if(line.contains("重新輸入"))
						return MSG_CLASS_CODE_ERROR;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return MSG_ERROR;
	}


}
