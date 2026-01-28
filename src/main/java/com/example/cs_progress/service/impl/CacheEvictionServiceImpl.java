package com.example.cs_progress.service.impl;

import com.example.cs_common.util.BaseCacheService;
import com.example.cs_progress.service.CacheEvictionService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

@Service
public class CacheEvictionServiceImpl extends BaseCacheService implements CacheEvictionService {

    @CacheEvict(value = "topic-progress", key = "#userId")
    public void evictTopicProgress(String userId) {
        log.info("Evicting topic-progress cache for userId={}", userId);
    }

    @CacheEvict(value = "ai-insights", key = "#userId")
    public void evictAIInsights(String userId) {
        log.info("Evicting ai-insights cache for userId={}", userId);
    }
}
