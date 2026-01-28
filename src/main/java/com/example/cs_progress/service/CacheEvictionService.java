package com.example.cs_progress.service;

public interface CacheEvictionService {

    void evictTopicProgress(String userId);

    void evictAIInsights(String userId);
}
