package com.example.cs_progress.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Кэш для прогресса по топикам
        cacheManager.registerCustomCache("topic-progress",
                Caffeine.newBuilder()
                        .expireAfterWrite(10, TimeUnit.MINUTES)
                        .maximumSize(50)
                        .build()
        );

        // Кэш для ИИ-анализа
        cacheManager.registerCustomCache("ai-insights",
                Caffeine.newBuilder()
                        .expireAfterWrite(24, TimeUnit.HOURS) // ← 24 часа
                        .maximumSize(10_000) // ← больше места (для всех пользователей)
                        .build()
        );

        return cacheManager;
    }
}