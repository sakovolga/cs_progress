package com.example.cs_progress.service;

import com.example.cs_common.dto.response.DashboardRs;

public interface TopicProgressService {

    void updateTaskStatsInTopicProgress(String userId, String courseId, String topicId);

    DashboardRs getUserDashboard(String userId);
}
