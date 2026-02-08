package com.example.cs_progress.service;

import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressListRs;

public interface DashboardService {

    DashboardRs getUserDashboard(String userId);

    DashboardTopicProgressListRs getUserDashboardTopicsTab(String userId, String courseId);

    DashboardTagsTabRs getUserDashboardTagsTab(String userId, String courseId);

}
