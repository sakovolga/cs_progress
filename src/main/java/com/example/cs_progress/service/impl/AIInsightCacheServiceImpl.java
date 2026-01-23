package com.example.cs_progress.service.impl;

import com.example.cs_common.exception.AIInsightGenerationException;
import com.example.cs_common.exception.AIInsightParsingException;
import com.example.cs_common.util.BaseCacheService;
import com.example.cs_progress.model.AIInsightResponse;
import com.example.cs_progress.model.PromptData;
import com.example.cs_progress.model.Recommendation;
import com.example.cs_progress.service.AIInsightCacheService;
import com.example.cs_progress.service.PromptDataCollectorService;
import com.example.cs_progress.service.PromptGeneratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIInsightCacheServiceImpl extends BaseCacheService implements AIInsightCacheService {

    private final PromptDataCollectorService promptDataCollector;
    private final PromptGeneratorService promptGenerator;
    private final ChatClient chatClient;

    private static final String CACHE = "ai-insights";

    /**
     * Получить инсайт из кэша или сгенерировать новый
     */
    @Cacheable(cacheNames = CACHE, key = "#userId")
    public AIInsightResponse getInsight(@NonNull String userId) {
        log.info("[CACHE MISS] Key={}, generating AI insight", userId);

        return generateInsight(userId);
    }

    /**
     * Обновить инсайт в кэше
     */
    @CachePut(cacheNames = CACHE, key = "#userId")
    public AIInsightResponse putInsightInCache(@NonNull String userId,
                                               @NonNull AIInsightResponse insight) {
        log.info("[CACHE PUT] Updating cache for key={}", userId);
        return insight;
    }

    /**
     * Инвалидация кэша
     */
    @CacheEvict(cacheNames = CACHE, key = "#userId")
    public void evictInsight(@NonNull String userId) {
        log.info("[CACHE EVICT] Evicting cache for key={}", userId);
    }

    /**
     * Генерация инсайта (внутренний метод)
     */
    private AIInsightResponse generateInsight(String userId) {
        long startTime = System.currentTimeMillis();

        try {
            // 1. Собираем данные
            PromptData promptData = promptDataCollector.collectData(userId);

            // 2. Генерируем промпт
            String prompt = promptGenerator.generatePrompt(promptData);

            log.debug("Generated prompt length: {} characters", prompt.length());

            // 3. Получаем ответ от ИИ через ChatClient
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Received AI response length: {} characters", aiResponse.length());

            // 4. Парсим ответ
            AIInsightResponse insight = parseAIResponse(aiResponse);
            insight.setGeneratedAt(LocalDateTime.now());

            long duration = System.currentTimeMillis() - startTime;
            log.info("[AI GENERATION] Insight generated in {}ms for user: {}", duration, userId);

            return insight;

        } catch (Exception e) {
            log.error("[AI GENERATION ERROR] Failed for user: {}", userId, e);
            throw new AIInsightGenerationException("Failed to generate AI insight", e);
        }
    }

    /**
     * Парсинг ответа от ИИ
     */
    private AIInsightResponse parseAIResponse(String aiResponse) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(aiResponse);

            String summary = rootNode.path("summary").asText();

            List<String> strengths = new ArrayList<>();
            JsonNode strengthsNode = rootNode.path("strengths");
            if (strengthsNode.isArray()) {
                strengthsNode.forEach(node -> strengths.add(node.asText()));
            }

            List<Recommendation> recommendations = new ArrayList<>();
            JsonNode recommendationsNode = rootNode.path("recommendations");
            if (recommendationsNode.isArray()) {
                recommendationsNode.forEach(recNode -> {
                    Recommendation rec = Recommendation.builder()
                            .title(recNode.path("title").asText())
                            .description(recNode.path("description").asText())
                            .priority(recNode.path("priority").asText())
                            .build();
                    recommendations.add(rec);
                });
            }

            return AIInsightResponse.builder()
                    .summary(summary)
                    .strengths(strengths)
                    .recommendations(recommendations)
                    .build();

        } catch (Exception e) {
            log.error("Failed to parse AI response: {}", aiResponse, e);
            throw new AIInsightParsingException("Failed to parse AI response", e);
        }
    }
}
