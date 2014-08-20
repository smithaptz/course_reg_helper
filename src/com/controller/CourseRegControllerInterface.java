package com.controller;


import com.stu.StuModelObservable;

public interface CourseRegControllerInterface extends ControllerInterface, StuModelObservable {
	boolean addWaitCourse(String courseCode);
	boolean removeWaitCourse(String courseCode);
}
