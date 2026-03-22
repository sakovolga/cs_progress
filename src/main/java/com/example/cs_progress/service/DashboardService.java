package com.example.cs_progress.service;

import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressListRs;

import java.util.Set;

public interface DashboardService {

    DashboardRs getUserDashboard(String userId);

    DashboardTopicProgressListRs getUserDashboardTopicsTab(String userId, String courseId);

    DashboardTagsTabRs getUserDashboardTagsTab(String userId, String courseId);

    Set<String> getCompletedTopicIds(String userId, String courseId);

}
