package com.example.cs_progress.controller;

import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressListRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dashboard")
public class DashboardController extends BaseController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardRs getDashboard(@RequestParam String userId) {
        log.info("Request to get dashboard for userId: {}", userId);

        return dashboardService.getUserDashboard(userId);
    }

    @GetMapping("/topics-tab")
    public DashboardTopicProgressListRs getDashboardTopicProgressList(@RequestParam String userId,
                                                                      @RequestParam String courseId) {
        log.info("Request to get dashboard topics tab for userId: {} and courseId: {}", userId, courseId);

        return dashboardService.getUserDashboardTopicsTab(userId, courseId);
    }

    @GetMapping("/tags-tab")
    public DashboardTagsTabRs getDashboardTagsTab(@RequestParam String userId,
                                                  @RequestParam String courseId) {
        log.info("Request to get dashboard tags tab for userId: {} and courseId: {}", userId, courseId);

        return dashboardService.getUserDashboardTagsTab(userId, courseId);
    }
}
