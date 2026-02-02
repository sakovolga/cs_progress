package com.example.cs_progress.service;

import com.example.cs_common.dto.analitics.AIInsightResponse;

public interface AIInsightCacheService {

    AIInsightResponse getCachedInsight(String userId);

    AIInsightResponse generateAndCache(String userId);

    void evictInsight(String userId);
}
