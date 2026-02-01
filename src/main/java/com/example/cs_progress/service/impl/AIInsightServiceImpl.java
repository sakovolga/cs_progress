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

    /**
     * Получить инсайт
     */
    @Override
    public AIInsightResponse getInsight(String userId) {
        log.info("Getting AI insight for user: {}", userId);

        AIInsightResponse insight = cacheService.getInsight(userId);

        boolean hasChanges = changeDetector.hasChanges(userId, insight.getGeneratedAt());

        if (hasChanges) {
            log.info("Changes detected, regenerating");
            cacheService.evictInsight(userId);
            insight = cacheService.getInsight(userId);
        } else {
            log.info("No changes, returning cached insight");
        }

        return insight;
    }

    /**
     * Инвалидация кэша (вызывается при значимых изменениях прогресса)
     */
    @Override
    public void invalidateCache(String userId) {
        log.info("Invalidating AI insight cache for user: {}", userId);
        cacheService.evictInsight(userId);
    }
}
