package com.controller;

public interface LoginControllerInterface extends ControllerInterface {
	void notifyObserver(boolean result);
	void registerObserver(LoginControllerObservable observer);
	boolean removeObserver(LoginControllerObservable observer);
	boolean login(String studentNo, String idCard, String birthday, String password);
}
