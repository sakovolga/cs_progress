package com.example.cs_progress.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SkillSummary {

    private String skillName; // название навыка (tag)
    private String topicId;
    private String topicTitle; // в каком топике этот навык

    // Прогресс навыка в этом топике
    private Double progressInTopic;

    // Активность топика
    private LocalDateTime topicLastActivity;
}
