package com.stu;

public interface StuState {
	boolean refresh();
	void setCookie(String cookie);
	String getCookie();
	StuStateEnum getState();
	StuStateEnum nextState();
}
