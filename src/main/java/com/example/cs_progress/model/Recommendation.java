package com.example.cs_progress.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Рекомендация от ИИ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recommendation implements Serializable {

    /**
     * Заголовок рекомендации
     */
    private String title;

    /**
     * Описание рекомендации
     */
    private String description;

    /**
     * Приоритет: "high", "medium", "low"
     */
    private String priority;
}