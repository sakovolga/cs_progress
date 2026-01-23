package com.example.cs_progress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ответ с ИИ-анализом прогресса пользователя
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIInsightResponse implements Serializable {

    /**
     * Краткий обзор прогресса (2-3 предложения)
     */
    private String summary;

    /**
     * Сильные стороны пользователя
     */
    private List<String> strengths;

    /**
     * Рекомендации для улучшения
     */
    private List<Recommendation> recommendations;

    /**
     * Когда был сгенерирован анализ
     */
    private LocalDateTime generatedAt;
}