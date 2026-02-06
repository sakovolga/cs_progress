package com.example.cs_progress.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SkillSummary {

    private String skillName;
    private String topicTitle;

    private Double taskCompletionRate;

    private LocalDateTime topicLastActivity;
}
