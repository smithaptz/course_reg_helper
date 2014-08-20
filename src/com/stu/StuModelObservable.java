package com.stu;

import com.course.CourseObservable;

public interface StuModelObservable extends CourseObservable {
	void setCourseRegResult(String courseCode, int result);
	void updateLoginState();
}
