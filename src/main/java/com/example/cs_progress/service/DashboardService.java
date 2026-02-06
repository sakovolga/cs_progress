package com.example.cs_progress.service;

import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicsTabRs;

public interface DashboardService {

    DashboardRs getUserDashboard(String userId);

    DashboardTopicsTabRs getUserDashboardTopicsTab(String userId, String courseId);

    DashboardTagsTabRs getUserDashboardTagsTab(String userId, String courseId);

}
