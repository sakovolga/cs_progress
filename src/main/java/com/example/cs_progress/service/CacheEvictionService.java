package com.example.cs_progress.service;

public interface CacheEvictionService {

    void evictTopicProgress(String userId, String courseId);

    void evictAIInsights(String userId);

    void evictCourseOverview(String courseId);
}
