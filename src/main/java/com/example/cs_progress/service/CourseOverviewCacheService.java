package com.example.cs_progress.service;

import com.example.cs_progress.model.entity.CourseOverview;

import java.util.Optional;

public interface CourseOverviewCacheService {

    Optional<CourseOverview> findByCourseId(String courseId);
}
