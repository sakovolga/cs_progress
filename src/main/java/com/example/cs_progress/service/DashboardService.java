package com.example.cs_progress.service;

import com.example.cs_common.dto.response.DashboardRs;

public interface DashboardService {

    DashboardRs getUserDashboard(String userId);
}
