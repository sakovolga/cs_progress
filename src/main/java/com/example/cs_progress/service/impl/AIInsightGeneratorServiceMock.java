package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.analitics.AIInsightResponse;
import com.example.cs_common.dto.analitics.Recommendation;
import com.example.cs_progress.service.AIInsightGeneratorService;
import lombok.NonNull;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Profile("dev")
public class AIInsightGeneratorServiceMock implements AIInsightGeneratorService {

    @Override
    public AIInsightResponse generate(@NonNull String userId) {
        return AIInsightResponse.builder()
                .summary("You're making solid progress! You've completed several Java topics and are building good momentum. Keep focusing on consistency and you'll reach your goals soon.")
                .strengths(List.of(
                        "Strong consistency in completing lessons",
                        "Good performance on Java fundamentals",
                        "Improving speed on coding tasks"
                ))
                .recommendations(List.of(
                        Recommendation.builder()
                                .title("Practice OOP concepts")
                                .description("Spend more time on inheritance and polymorphism exercises to solidify your object-oriented foundation.")
                                .priority("high")
                                .build(),
                        Recommendation.builder()
                                .title("Review Collections framework")
                                .description("You have gaps in List and Map usage. Try solving 3-5 tasks focused on Collections this week.")
                                .priority("medium")
                                .build(),
                        Recommendation.builder()
                                .title("Explore Java Streams")
                                .description("Streams will unlock more expressive and efficient code — a great next step after Collections.")
                                .priority("low")
                                .build()
                ))
                .generatedAt(LocalDateTime.now())
                .build();
    }
}