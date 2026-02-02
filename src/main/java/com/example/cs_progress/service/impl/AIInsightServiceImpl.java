package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.analitics.AIInsightResponse;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.service.AIInsightCacheService;
import com.example.cs_progress.service.AIInsightService;
import com.example.cs_progress.service.ProgressChangeDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIInsightServiceImpl extends BaseService implements AIInsightService {
    private final AIInsightCacheService cacheService;
    private final ProgressChangeDetector changeDetector;

    @Override
    public AIInsightResponse getInsight(String userId) {
        log.info("Getting AI insight for user: {}", userId);

        AIInsightResponse cachedInsight = cacheService.getCachedInsight(userId);

        if (cachedInsight == null) {
            log.info("No cached insight found, generating new one");
            return cacheService.generateAndCache(userId);
        }

        log.info("Found cached insight from: {}", cachedInsight.getGeneratedAt());
        boolean hasChanges = changeDetector.hasChanges(userId, cachedInsight.getGeneratedAt());

        if (hasChanges) {
            log.info("Changes detected since {}, regenerating", cachedInsight.getGeneratedAt());
            return cacheService.generateAndCache(userId);
        }

        log.info("No changes since {}, returning cached insight", cachedInsight.getGeneratedAt());
        return cachedInsight;
    }

    @Override
    public void invalidateCache(String userId) {
        log.info("Invalidating AI insight cache for user: {}", userId);
        cacheService.evictInsight(userId);
    }
}