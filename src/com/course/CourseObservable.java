package com.course;

import java.util.Map;

public interface CourseObservable {
	void update(String courseCode, Map<String, String> dataMap);
}
