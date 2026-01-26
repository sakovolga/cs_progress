package com.example.cs_progress.controller;

import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.TopicProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {

    private final TopicProgressService topicProgressService;

    @GetMapping
    public DashboardRs getDashboard(@RequestParam String userId) {
        log.info("Request to get dashboard for userId: {}", userId);

        return topicProgressService.getUserDashboard(userId);
    }
}
