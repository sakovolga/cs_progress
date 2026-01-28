package com.example.cs_progress.service;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_progress.model.entity.CourseOverview;

public interface TagCountService {

     void update(TaskStatsChangedEvent event, CourseOverview courseOverview);
}
