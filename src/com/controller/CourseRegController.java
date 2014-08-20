package com.controller;

import java.awt.Component;
import java.text.DateFormat;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import com.Main;
import com.course.CourseWebObserver;
import com.stu.StuModel;
import com.stu.StuModelObservable;
import com.view.CourseRegPane;

public class CourseRegController implements CourseRegControllerInterface {
	private CourseRegPane component;
	private StuModel model;
	private DefaultListModel<String> listModel;
	private Map<String, String> infoMap;
	private Map<String, String> listInfoMap;
	private DateFormat mediumFormat;
	private Main main;
	
	private static final int MAX_RELOGIN_NUM = 3;
	private int reloginCounter = 0;
	
	private static final String[] MSG_RESULT = new String[] 
			{"未知錯誤", "加選成功", "重複加選", "課程衝堂", "不符合條件", "人數已滿", "找不到課程", "課程代碼錯誤", "連線錯誤"};
	
	public CourseRegController(Main main, StuModel model) {
		this.main = main;
		this.model = model;
		initialize();
	}
	
	private void initialize() {
		component = new CourseRegPane(this);
		listModel = component.getDefaultListModel();
		infoMap = new TreeMap<String, String>();
		listInfoMap = new TreeMap<String, String>();
		mediumFormat = DateFormat.getDateTimeInstance(
				DateFormat.MEDIUM, DateFormat.MEDIUM);
		
		
		model.registerObserver((StuModelObservable)this);
		
		String info = formatString("課程代碼", 12) + formatString("課程名稱", 15) + 
				formatString("授課教師", 15) + formatString("目前選課人數", 14) + 
				formatString("正式選課人數上限", 13) + formatString("目前狀態", 15) +
				formatString("最後更新時間", 15);
		
		
		
		listModel.addElement(info);
		
	}

	@Override
	public void setCourseRegResult(String courseCode, int result) {
		String status = (result > 0 && result <= 7) ? MSG_RESULT[result] :  MSG_RESULT[0];
		String info = infoMap.get(courseCode);
		addListElement(courseCode, info, status);
	}

	@Override
	public void update(String courseCode, Map<String, String> dataMap) {
		String courseName = dataMap.get(CourseWebObserver.FIELD_COURSE_NAME);
		String lecturer = dataMap.get(CourseWebObserver.FIELD_LECTURER);
		String currentRegNum = dataMap.get(CourseWebObserver.FIELD_CURRENT_REG_NUM);
		String maxRegNum = dataMap.get(CourseWebObserver.FIELD_MAX_REG_NUM);
		
		String info = formatString(courseCode, 15) + formatString(courseName, 15) + 
				formatString(lecturer, 15) + formatString(currentRegNum, 15) + 
				formatString(maxRegNum, 15);

		addListElement(courseCode, info, "執行中");
	}
	
	
	private String formatString(String s, int length) {
		s = (s == null) ? "null" : s;
		StringBuilder sb = new StringBuilder(s);
		int offset = length - s.length();
		for(int i = 0; i < offset; i++)
			sb.append("　");
		return sb.toString();
	}

	@Override
	public boolean addWaitCourse(String courseCode) {
		if(courseCode.length() != 9) {
			JOptionPane.showMessageDialog(component, "課程代碼錯誤!", null, JOptionPane.ERROR_MESSAGE);
			return false;
		}
		if(infoMap.containsKey(courseCode)) {
			JOptionPane.showMessageDialog(component, "課程" + courseCode + "已在排程中", null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		model.addWaitCourse((StuModelObservable)this, courseCode);
		return true;
	}

	@Override
	public boolean removeWaitCourse(String courseCode) {
		if(!infoMap.containsKey(courseCode)) {
			JOptionPane.showMessageDialog(component, "課程" + courseCode + "不在排程中", null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		boolean result = model.removeWaitCourse((StuModelObservable)this, courseCode);
		removeListElment(courseCode);
		return result;
	}
	

	@Override
	public Component getComponent() {
		return component;
	}
	
	
	private boolean removeListElment(String courseCode) {
		if(!infoMap.containsKey(courseCode))
			return false;
		String s = listInfoMap.get(courseCode);
		listModel.removeElement(s);
		infoMap.remove(courseCode);
		listInfoMap.remove(courseCode);
		return true;
	}
	
	private void addListElement(String courseCode, String element, String status) {
		String line = element + formatString(status, 15) + formatString(mediumFormat.format(
				System.currentTimeMillis()), 30);
		
		if(listInfoMap.containsKey(courseCode)) {
			String listInfo = listInfoMap.get(courseCode);
			int index = listModel.indexOf(listInfo);
			listModel.remove(index);
			listModel.add(index, line);
		} else {
			listModel.addElement(line);
			
		}
		infoMap.put(courseCode, element);
		listInfoMap.put(courseCode, line);
	}

	/*
	@Override
	synchronized public void updateLoginState() {
		if(model.isReady())
			return;
		
		//JOptionPane.showMessageDialog(component, "重新登入", null, JOptionPane.WARNING_MESSAGE);
		int confirmResult = JOptionPane.showConfirmDialog(component, "請重新登入", null, 
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		
		if(confirmResult != JOptionPane.OK_OPTION) {
			main.setLoginView();
			return;
		}
		
		int result = model.getReady();
		if(result == StuModel.MSG_SUCCESS)
			return;
		if(result == StuModel.MSG_NOT_CHOICE_TIME) {
			JOptionPane.showMessageDialog(component, "現在已是非選課時段", null, JOptionPane.WARNING_MESSAGE);
			return;
		}
		if(result == StuModel.MSG_LOGIN_FAIL) {
			JOptionPane.showMessageDialog(component, "登入失敗", null, JOptionPane.WARNING_MESSAGE);
			updateLoginState();
			return;
		}
		JOptionPane.showMessageDialog(component, "未知錯誤", null, JOptionPane.ERROR_MESSAGE);
	}
	*/
	
	synchronized public void updateLoginState() {
		if(model.isReady())
			return;
		
		if(reloginCounter >= MAX_RELOGIN_NUM) {
			System.out.println("已達自動重登次數上限!");
			reloginCounter = 0;
			main.setLoginView();
			return;
		}
		
		reloginCounter++;
		int result = model.getReady();
		if(result == StuModel.MSG_SUCCESS) {
			reloginCounter = 0;
			return;
		}
		if(result == StuModel.MSG_NOT_CHOICE_TIME) {
			JOptionPane.showMessageDialog(component, "現在已是非選課時段", null, JOptionPane.WARNING_MESSAGE);
			reloginCounter = 0;
			main.setLoginView();
			return;
		}
		if(result == StuModel.MSG_LOGIN_FAIL) {
			System.out.println("登入失敗，再次重新登入。");
			updateLoginState();
			return;
		}
		JOptionPane.showMessageDialog(component, "未知錯誤", null, JOptionPane.ERROR_MESSAGE);
	}
}
