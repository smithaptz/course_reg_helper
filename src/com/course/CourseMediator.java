package com.course;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CourseMediator {
	private volatile static CourseMediator uniqueInstance;
	private Map<String, CourseWebObserver> courseObserverMap;
	
	private CourseMediator() {
		courseObserverMap = new ConcurrentHashMap<String, CourseWebObserver>();
	}
	
	public static CourseMediator getService() {
		if(uniqueInstance == null) {
			synchronized(CourseMediator.class) {
				if(uniqueInstance == null) {
					uniqueInstance = new CourseMediator();
				}
			}
		}
		return uniqueInstance;
	}
	
	public void registerCourseObserver(CourseObservable observer, String courseCode) {
		String code = courseCode.toUpperCase();
		CourseWebObserver webObserver = courseObserverMap.get(code);
		if(webObserver == null) {
			webObserver = new CourseWebObserver(code);
			courseObserverMap.put(code, webObserver);
		} 
		if(!webObserver.hasRegister(observer)) {
			webObserver.registerObserver(observer);
		}
	}
	
	public boolean removeCourseObserver(CourseObservable observer, String courseCode) {
		String code = courseCode.toUpperCase();
		CourseWebObserver webObserver = courseObserverMap.get(code);
		if(webObserver == null)
			return false;
		if(!webObserver.hasRegister(observer)) {
			return false;
		}
		
		boolean result = webObserver.removeObserver(observer);
		
		if(webObserver.getNumObservers() == 0) {
			courseObserverMap.remove(code);
		}
		
		return result;
	}
}
