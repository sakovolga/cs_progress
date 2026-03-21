package com.example.cs_progress.service;

import com.example.cs_common.dto.response.CourseCompletionRs;

public interface CourseCompletionService {

    void checkAndMarkCourseCompleted(String userId, String courseId);

    CourseCompletionRs getCourseCompletion(String userId, String courseId);

}