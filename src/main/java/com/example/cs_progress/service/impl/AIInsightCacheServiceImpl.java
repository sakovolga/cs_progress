package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.analitics.AIInsightResponse;
import com.example.cs_common.util.BaseCacheService;
import com.example.cs_progress.service.AIInsightCacheService;
import com.example.cs_progress.service.AIInsightGeneratorService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AIInsightCacheServiceImpl extends BaseCacheService implements AIInsightCacheService {

    private final AIInsightGeneratorService generatorService;
    private final CacheManager cacheManager;

    private static final String CACHE = "ai-insights";

    @Override
    public AIInsightResponse getCachedInsight(@NonNull String userId) {
        Cache cache = cacheManager.getCache(CACHE);
        if (cache == null) {
            return null;
        }

        Cache.ValueWrapper wrapper = cache.get(userId);
        if (wrapper == null) {
            log.debug("[CACHE MISS] No cached insight for key={}", userId);
            return null;
        }

        log.debug("[CACHE HIT] Found cached insight for key={}", userId);
        return (AIInsightResponse) wrapper.get();
    }

    @Override
    @CachePut(cacheNames = CACHE, key = "#userId")
    public AIInsightResponse generateAndCache(@NonNull String userId) {
        log.info("[CACHE PUT] Generating and caching new insight for key={}", userId);
        return generatorService.generate(userId);
    }

    @Override
    @CacheEvict(cacheNames = CACHE, key = "#userId")
    public void evictInsight(@NonNull String userId) {
        log.info("[CACHE EVICT] Evicting cache for key={}", userId);
    }
}