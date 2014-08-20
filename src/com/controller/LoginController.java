package com.controller;


import java.awt.Component;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JOptionPane;

import com.Main;
import com.Users;
import com.stu.StuModel;
import com.view.LoginPane;

public class LoginController implements LoginControllerInterface {
	private LoginPane component;
	private StuModel model;
	private Main main;
	
	private Set<LoginControllerObservable> observersSet;
	
	public LoginController(Main main, StuModel model) {
		this.main = main;
		this.model = model;
		initialize();
	}
	
	private void initialize() {
		component = new LoginPane(this);
		observersSet = new HashSet<LoginControllerObservable>();
	}

	@Override
	public boolean login(String studentNo, String idCard, String birthday,
			String password) {
		
		boolean isUserValid = false;
		
		for(String pattern : Users.USERS_REGEX) {
			if(studentNo.matches(pattern)) {
				isUserValid = true;
				break;
			}
		}
		
		if(!isUserValid) {
			JOptionPane.showMessageDialog(component, "登入失敗!\n" + "非法使用者", 
					null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		/*
		if(!studentNo.contains("9802")) {
			JOptionPane.showMessageDialog(component, "登入失敗!\n" + "非法使用者", 
					null, JOptionPane.WARNING_MESSAGE);
			return false;
		}
		*/
		
	
		boolean result = false;
		
		int actionResult = model.action(studentNo, idCard, birthday, password);
		
		switch(actionResult) {
		case StuModel.MSG_SUCCESS :
			result = true;
			break;
		case StuModel.MSG_LOGIN_FAIL :
			JOptionPane.showMessageDialog(component, "登入失敗!", null, JOptionPane.WARNING_MESSAGE);
			break;
		case StuModel.MSG_NOT_CHOICE_TIME :
			JOptionPane.showMessageDialog(component, "非選課時段", null, JOptionPane.WARNING_MESSAGE);
			break;
		default :
			JOptionPane.showMessageDialog(component, "系統錯誤!", null, JOptionPane.ERROR_MESSAGE);
		}
		
		notifyObserver(result); 
		return result;
	}

	@Override
	public Component getComponent() {
		return component;
	}

	@Override
	public void notifyObserver(boolean result) {
		synchronized(observersSet) {
			for(LoginControllerObservable observer : observersSet) {
				observer.setLoginResult(result);
			}
		}
	}

	@Override
	public void registerObserver(LoginControllerObservable observer) {
		synchronized(observersSet) {
			observersSet.add(observer);
		}
	}

	@Override
	public boolean removeObserver(LoginControllerObservable observer) {
		synchronized(observersSet) {
			return observersSet.remove(observer);
		}
	}

}
