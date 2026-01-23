package com.example.cs_progress.service;

import com.example.cs_progress.model.AIInsightResponse;

public interface AIInsightService {

    AIInsightResponse getInsight(String userId);

    void invalidateCache(String userId);
}
