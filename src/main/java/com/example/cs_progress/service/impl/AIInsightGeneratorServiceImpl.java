package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.analitics.AIInsightResponse;
import com.example.cs_common.dto.analitics.Recommendation;
import com.example.cs_common.exception.AIInsightGenerationException;
import com.example.cs_common.exception.AIInsightParsingException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.PromptData;
import com.example.cs_progress.service.AIInsightGeneratorService;
import com.example.cs_progress.service.PromptDataCollectorService;
import com.example.cs_progress.service.PromptGeneratorService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AIInsightGeneratorServiceImpl extends BaseService implements AIInsightGeneratorService {

    private final PromptDataCollectorService promptDataCollector;
    private final PromptGeneratorService promptGenerator;
    private final ChatClient chatClient;

    @Override
    public AIInsightResponse generate(@NonNull String userId) {
        long startTime = System.currentTimeMillis();

        LocalDateTime generationStartedAt = LocalDateTime.now();

        try {
            log.info("[AI GENERATION] Starting for user: {}", userId);

            // 1. Собираем данные
            PromptData promptData = promptDataCollector.collectData(userId);

            // 2. Генерируем промпт
            String prompt = promptGenerator.generatePrompt(promptData);
            log.debug("Generated prompt length: {} characters", prompt.length());

            // 3. Получаем ответ от ИИ
            String aiResponse = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            log.debug("Received AI response length: {} characters", aiResponse.length());

            // 4. Парсим ответ
            AIInsightResponse insight = parseAIResponse(aiResponse);
            insight.setGeneratedAt(generationStartedAt);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[AI GENERATION] Completed in {}ms for user: {}", duration, userId);

            return insight;

        } catch (Exception e) {
            log.error("[AI GENERATION ERROR] Failed for user: {}", userId, e);
            throw new AIInsightGenerationException("Failed to generate AI insight", e);
        }
    }

    private AIInsightResponse parseAIResponse(String aiResponse) {
        try {
            String cleanedResponse = cleanResponse(aiResponse);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(cleanedResponse);

            String summary = rootNode.path("summary").asText();
            List<String> strengths = parseStringArray(rootNode.path("strengths"));
            List<Recommendation> recommendations = parseRecommendations(rootNode.path("recommendations"));

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

    private String cleanResponse(String aiResponse) {
        String cleaned = aiResponse.trim();

        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.substring(7);
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.substring(3);
        }

        if (cleaned.endsWith("```")) {
            cleaned = cleaned.substring(0, cleaned.length() - 3);
        }

        return cleaned.trim();
    }

    private List<String> parseStringArray(JsonNode arrayNode) {
        List<String> result = new ArrayList<>();

        if (arrayNode.isArray()) {
            arrayNode.forEach(node -> result.add(node.asText()));
        }

        return result;
    }

    private List<Recommendation> parseRecommendations(JsonNode recommendationsNode) {
        List<Recommendation> recommendations = new ArrayList<>();

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

        return recommendations;
    }
}