package com.example.cs_progress.controller;

import com.example.cs_common.util.BaseController;
import com.example.cs_progress.model.AIInsightResponse;
import com.example.cs_progress.service.AIInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard/ai-insights")
public class AIInsightController extends BaseController {

    private final AIInsightService aiInsightService;

    @GetMapping("/{userId}")
    public ResponseEntity<AIInsightResponse> getInsight(@PathVariable String userId) {
        log.info("Request to get AI insight for userId: {}", userId);

        AIInsightResponse insight = aiInsightService.getInsight(userId);

        return ResponseEntity.ok(insight);
    }
}
