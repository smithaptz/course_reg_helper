package com.stu;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CopyOnWriteArraySet;

import com.course.CourseMediator;
import com.course.CourseObservable;
import com.course.CourseWebObserver;

public class StuModel implements CourseObservable {
	
	private Set<String> waitCourseSet;
	private CourseMediator courseMediator;

	private Set<StuModelObservable> observersSet;
	
	private StuState state;
	private StuLoginState loginState;
	private StuMenuState menuState;
	private StuChooseDocState chooseDocState;
	private StuChoiceCourseState choiceCourseState;
	
	private HashMap<String, String> loginInfoMap;
	
	private String cookie;
	
	private Timer timer;
	
	private static final int MAX_CONNECTION_TIMES = 5;
	private static final int REFRESH_TIME = 60000; //600000;
	
	public static final int MSG_ERROR = -1;
	public static final int MSG_SUCCESS = 1;
	public static final int MSG_SCHEDULE_CLASS_REPEAT = 2;
	public static final int MSG_SCHEDULE_CONFLICT = 3;
	public static final int MSG_CONDITION_NOT_SATISFY = 4;
	public static final int MSG_CLASS_FULL = 5;
	public static final int MSG_CLASS_NOT_FOUND = 6;
	public static final int MSG_CLASS_CODE_ERROR = 7;
	public static final int MSG_CONNECT_ERROR = 8;
	
	public static final int MSG_LOGIN_FAIL = 100;
	public static final int MSG_NOT_CHOICE_TIME = 101;
	
		
	public StuModel() {
		initialize();
	}
	
	private void initialize() {
		waitCourseSet = new CopyOnWriteArraySet <String>();
		courseMediator = CourseMediator.getService();
		observersSet = new CopyOnWriteArraySet <StuModelObservable>();
		loginState = new StuLoginState();
		menuState = new StuMenuState();
		chooseDocState = new StuChooseDocState();
		choiceCourseState = new StuChoiceCourseState();
		loginInfoMap = new HashMap<String, String>();
		timer = new Timer();
		
		state = loginState;
	}
	
	public boolean hasRegister(CourseObservable observer) {
		return observersSet.contains(observer);
	}
	
	public void registerObserver(StuModelObservable observer) {
		observersSet.add(observer);
	}
	
	public boolean removeObserver(StuModelObservable observer) {
		synchronized(observersSet) {
			return observersSet.remove(observer);
		}
	}
	
	public void notifyObserver(String courseCode, int result) {
		synchronized(observersSet) {
			for(StuModelObservable observer : observersSet) {
				observer.setCourseRegResult(courseCode, result);
			}
		}
	}
	
	public void notifyLoginState() {
		if(isReady())
			return;
		
		synchronized(observersSet) {
			for(StuModelObservable observer : observersSet) {
				observer.updateLoginState();
			}
		}
	}
	
	private void registerCourseObserverAll(String courseCode) {
		synchronized(observersSet) {
			for(CourseObservable observer : observersSet) {
				courseMediator.registerCourseObserver(observer, courseCode);
			}
		}
	}
	
	private void removeCourseObserverAll(String courseCode) {
		System.out.println("StuModel.removeCourseObserverAll()");
		synchronized(observersSet) {
			for(CourseObservable observer : observersSet) {
				courseMediator.removeCourseObserver(observer, courseCode);
			}
		}
	}
		
	public int getReady() {
		timer.schedule(new NullTask(), 0);
		
		loginState.login(loginInfoMap.get("studentNo"), 
				loginInfoMap.get("idCard"), 
				loginInfoMap.get("birthday"), 
				loginInfoMap.get("password"));
		
		cookie = loginState.getCookie();
		state = loginState;
		StuStateEnum nextState = state.nextState();
		int count = 0;
		while(!(nextState.equals(StuStateEnum.ERROR) || nextState.equals(StuStateEnum.NULL)) && 
				count++ < MAX_CONNECTION_TIMES) {
			System.out.println("CurrentState : " + state.getState().toString());
			setState(nextState);
			nextState = state.nextState();
		}
		System.out.println("CurrentState : " + state.getState().toString());
		System.out.println("NextState : " + nextState.toString());
		
		if(StuStateEnum.LOGIN.equals(state.getState()))
			return MSG_LOGIN_FAIL;
		
		if(!isReady())
			return MSG_NOT_CHOICE_TIME;
		
		timer.schedule(new RefreshTask(), REFRESH_TIME, REFRESH_TIME);
		
		return MSG_SUCCESS;
	}
	
	public int action(String studentNo, String idCard, String birthday, String password) {
		loginInfoMap.put("studentNo", studentNo);
		loginInfoMap.put("idCard", idCard);
		loginInfoMap.put("birthday", birthday);
		loginInfoMap.put("password", password);
		
		return getReady();
	}
	
	public boolean isReady() {
		return state.getState() == StuStateEnum.CHOICE_COURSE;
	}
	
	private void setState(StuStateEnum e) {
		switch(e) {
		case LOGIN :
			state = loginState;
			break;
		case MENU :
			state = menuState;
			break;
		case CHOOSE_DOC :
			state = chooseDocState;
			break;
		case CHOICE_COURSE :
			state = choiceCourseState;
			break;
		}
		state.setCookie(cookie);
	}
	
	public boolean addWaitCourse(StuModelObservable observer, String courseCode) {
		if(!observersSet.contains(observer))
			return false;
		String code = courseCode.toUpperCase();
		waitCourseSet.add(code);
		registerCourseObserverAll(code);
		courseMediator.registerCourseObserver((CourseObservable)this, code);
		return true;
	}
	
	public boolean removeWaitCourse(StuModelObservable observer, String courseCode) {
		if(!observersSet.contains(observer))
			return false;
		return removeWaitCourse(courseCode);
	}
	
	private boolean removeWaitCourse(String courseCode) {
		System.out.println("StuModel.removeWaitCourse()");
		String code = courseCode.toUpperCase();
		if(!waitCourseSet.contains(code))
			return false;
		waitCourseSet.remove(code);
		removeCourseObserverAll(code);
		return courseMediator.removeCourseObserver((CourseObservable)this, code);
	}
	
	@Override
	public void update(String courseCode, Map<String, String> dataMap) {
		if(!isReady()) {
			System.out.println("Not ready yet.");
			return;
		}
		
		if(dataMap.get(CourseWebObserver.FIELD_CURRENT_REG_NUM) == null 
				|| dataMap.get(CourseWebObserver.FIELD_MAX_REG_NUM) == null)
					return;
		int currentRegNum = Integer.parseInt(dataMap.get(CourseWebObserver.FIELD_CURRENT_REG_NUM));
		int maxRegNum = Integer.parseInt(dataMap.get(CourseWebObserver.FIELD_MAX_REG_NUM));
		if(currentRegNum < maxRegNum) {
			if(registerCourse(courseCode) != MSG_CLASS_FULL) {
				System.out.println("StuModel.update()");
				removeWaitCourse(courseCode);
			}
		}
	}
	
	private int registerCourse(String courseCode) {
		refresh();
		if(!isReady())
			return MSG_CONNECT_ERROR;
		System.out.println("¹Á¸Õµù¥U½Òµ{ : " + courseCode);
		String code = courseCode.toUpperCase();
		int result = choiceCourseState.registerCourse(code);
		notifyObserver(code, result);
		return result;
	}
	
	private void refresh() {
		boolean result = false;
		if(isReady()) {
			System.out.println("Auto refresh");
			result = StuModel.this.state.refresh();
			for(int i = 0; !result && i < 3; i++) {
				result = StuModel.this.state.refresh();
			}
		}
		if(!result) {
			System.out.println("has been kicked out!");
			state = loginState;
			notifyLoginState();
		}
	}
	
	private class RefreshTask extends TimerTask {
		@Override
		public void run() {
			refresh();
		}
	}
	
	private class NullTask extends TimerTask {
		@Override
		public void run() {
			timer.cancel();
			timer = new Timer();
		}
	}
	
}
