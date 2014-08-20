package com.course;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.net.ConnectionHelper;

public class CourseWebObserver {
	private static final int UPDATE_CYCLE = 1000;
	private static final int TRY_ERROR_TIMES = 5;
	
	private int updateCycle;
	private int tryErrorTimes;
	private String code;
	private String cookie;
	private Map<String, String> inputValueMap;
	private Map<String, String> dataMap;
	private Set<CourseObservable> observersSet;
	
	private boolean conAlive;
	
	private CourseObserverThread observerThread;
	
	public static final String CHARSET = "utf-8";
	public static final String WEB_SERVER_URL = "http://140.118.31.215";
	public static final String QUERY_CONDITION_URL = 
			"http://140.118.31.215/querycourse/ChCourseQuery/QueryCondition.aspx";
	public static final String QUERY_RESULT_URL = 
			"http://140.118.31.215/querycourse/ChCourseQuery/QueryResult.aspx";
	public static final String DETAIL_COURSE_URL = 
			"http://140.118.31.215/querycourse/ChCourseQuery/DetailCourse.aspx?chooseCourseNo=";
	
	public static final String FIELD_COURSE_NAME = "課程名稱";
	public static final String FIELD_LECTURER = "授課教師";
	public static final String FIELD_CURRENT_REG_NUM = "目前選課人數";
	public static final String FIELD_MAX_REG_NUM = "加退選課人數上限";
	public static final String FIELD_FINAL_REG_NUM = "正式選課人數上限";
	
	
	
	public CourseWebObserver(String courseCode) {
		code = courseCode;
		initialize();
	}
	
	public CourseWebObserver(String courseCode, String cookie) {
		code = courseCode;
		this.cookie = cookie;
		initialize();
	}
	
	private void initialize() {
		dataMap = new HashMap<String, String>();
		observersSet = new CopyOnWriteArraySet<CourseObservable>();
		updateCycle = UPDATE_CYCLE;
		tryErrorTimes = TRY_ERROR_TIMES;
	}
	
	private boolean reconnect() {
		boolean result;
		result = doGetQueryCondition(null);
		result &= doPostQueryCondition(QUERY_CONDITION_URL);	
		result &= doGetDetailCourse(QUERY_CONDITION_URL);
		
		setConnectionAlive(result);
		return result;
	}
	
	private boolean updateConnect() {
		boolean result = false;
		int count = 0;
		
		while(!result && count < tryErrorTimes) {
			result = doGetDetailCourse(QUERY_CONDITION_URL);
			count++;
		}
		setConnectionAlive(result);
		return result;
	}
	
	private boolean isConnectionAlive() {
		return conAlive;
	}
	
	private void setConnectionAlive(boolean alive) {
		conAlive = alive;
	}
	
	public void setUpdateCycle(int time) {
		updateCycle = time;
	}
	
	public void setTryErrorTimes(int times) {
		tryErrorTimes = times;
	}

	public String getCookie() {
		return cookie;
	}
	
	public String getCourseCode() {
		return code;
	}
	
	public boolean hasRegister(CourseObservable observer) {
		return observersSet.contains(observer);
	}
	
	public void registerObserver(CourseObservable observer) {
		observersSet.add(observer);
		// observer.update(code, new HashMap<String, String>(dataMap));
		if(observerThread == null) {
			observerThread = new CourseObserverThread();
			observerThread.start();
		} else if(!observerThread.isAlive()) {
			observerThread = new CourseObserverThread();
			observerThread.start();
		}
	}
	
	public boolean removeObserver(CourseObservable observer) {
		synchronized(observersSet) {
			return observersSet.remove(observer);
		}
	}
	
	public void notifyObserver() {
		synchronized(observersSet) {
			for(CourseObservable observer : observersSet) {
				System.out.println("notify observer : " + observer);
				observer.update(code, new HashMap<String, String>(dataMap));
			}
		}
	}
	
	protected int getNumObservers() {
		return observersSet.size();
	}
	
	private boolean doGetQueryCondition(String referer) {
		boolean result = false;
		HttpURLConnection URLConn = ConnectionHelper.doGet(QUERY_CONDITION_URL, cookie, referer);
		try {
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			result = (URLConn.getResponseCode() == 200);
			
			System.out.println("URLConn.getResponseCode() : " + URLConn.getResponseCode());
			
			inputValueMap = ConnectionHelper.getInputValueMap(URLConn);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean doPostQueryCondition(String referer) {
		boolean result = false;
		
		try {
			String viewState = URLEncoder.encode(inputValueMap.get("__VIEWSTATE"), "utf-8");
			String eventValidation = URLEncoder.encode(inputValueMap.get("__EVENTVALIDATION"), "utf-8");
			String semesterList = URLEncoder.encode(inputValueMap.get("semester_list"), "utf-8");
			
			String data = "__EVENTTARGET=&__EVENTARGUMENT=&__LASTFOCUS=&__VIEWSTATE=" + viewState + 
					"&__EVENTVALIDATION=" + eventValidation + "&semester_list=" + semesterList +
					"&Acb0101=on&BCH0101=on&Ctb0101=" + code + "&Ctb0201=&Ctb0301=&QuerySend=" +
							"%E9%80%81%E5%87%BA%E6%9F%A5%E8%A9%A2";
					
			HttpURLConnection URLConn = ConnectionHelper.doPost(QUERY_CONDITION_URL, data, cookie, referer);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			result = (QUERY_RESULT_URL.equals(WEB_SERVER_URL + URLConn.getHeaderField("Location"))); // 302
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean doGetDetailCourse(String referer) {
		boolean result = false;
		HttpURLConnection URLConn = ConnectionHelper.doGet(DETAIL_COURSE_URL + code, cookie, referer);
		try {
			//System.out.println(DETAIL_COURSE_URL + code);
			//System.out.println("Cookie : " + cookie);
			if(ConnectionHelper.getCookie(URLConn) != null)
				cookie = ConnectionHelper.getCookie(URLConn);
			//System.out.println("Cookie : " + cookie);
			//System.out.println("URLConn.getResponseCode() : " + URLConn.getResponseCode());
			if(URLConn.getResponseCode() == 200) {
				 readCourseData(new InputStreamReader(
						 URLConn.getInputStream(), CHARSET));
				 result = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private boolean readCourseData(InputStreamReader input) {
		BufferedReader buf = new BufferedReader(input);
		String line;
		boolean hasUpdate = false;
		
		try {
			while((line = buf.readLine()) != null) {
				String key = null;
				String data = null;
				
				
				if(line.contains(FIELD_COURSE_NAME) && !line.contains("英文課程名稱")) {
					key = FIELD_COURSE_NAME;
					data = line.substring(line.indexOf("<td>") + 4 , line.lastIndexOf("</td>"));
					data = data.split(" +")[0];
				} else if(line.contains(FIELD_LECTURER)) {
					key = FIELD_LECTURER;
					data = line.substring(line.indexOf("_blank>") + 7, line.lastIndexOf("</a>")); 
				} else if(line.contains(FIELD_CURRENT_REG_NUM)) {
					key = FIELD_CURRENT_REG_NUM;
					data = line.substring(line.indexOf("<td>") + 4 , line.lastIndexOf("</td>"));
				} else if(line.contains(FIELD_MAX_REG_NUM)) {
					key = FIELD_MAX_REG_NUM;
					data = line.substring(line.indexOf("<td>") + 4 , line.lastIndexOf("</td>"));
				} else if(line.contains(FIELD_FINAL_REG_NUM)) {
					key = FIELD_FINAL_REG_NUM;
					data = line.substring(line.indexOf("<td>") + 4 , line.lastIndexOf("</td>"));
				} 
				
				if(key != null) {
					hasUpdate |= updateDataMap(key, data);
				}
			}
			if(hasUpdate) {
				System.out.println("課程資訊已更新 : 課程代碼 : " + code + ", 課程名稱 : " + dataMap.get(FIELD_COURSE_NAME));
				notifyObserver();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean updateDataMap(String key, String data) {
		if(data.equals(dataMap.get(key)))
			return false;
		dataMap.put(key, data);
		return true;
	}
	
	private class CourseObserverThread extends Thread {
		@Override
		public void run() {
			reconnect();
			while(!observersSet.isEmpty()) {
				//System.out.println(observersSet.toString());
				if(isConnectionAlive()) {
					System.out.println("重新查詢 : 課程代碼 : " + code + ", 課程名稱 : " + dataMap.get(FIELD_COURSE_NAME));
					updateConnect();
				} else {
					System.out.println("重新連線 : 課程代碼 : " + code + ", 課程名稱 : " + dataMap.get(FIELD_COURSE_NAME));
					reconnect();
				}
				try {
					Thread.sleep(updateCycle);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			dataMap.clear();
		}
	}
	
}
