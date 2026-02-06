package com.example.cs_progress.service;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_common.dto.response.CourseOverviewSynchronizationRs;

public interface CourseOverviewService {

//    void handleTaskStatsChangedEvent(TaskStatsChangedEvent event);

    CourseOverviewSynchronizationRs synchronizeCourseOverview(CourseOverviewDto courseOverviewDto);
}
