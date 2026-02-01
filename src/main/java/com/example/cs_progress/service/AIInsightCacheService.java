package com.example.cs_progress.service;

import com.example.cs_common.dto.analitics.AIInsightResponse;

public interface AIInsightCacheService {

    AIInsightResponse getInsight(String userId);

    AIInsightResponse putInsightInCache(String userId, AIInsightResponse insight);

    void evictInsight(String userId);
}
