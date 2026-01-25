package com.example.cs_progress.service;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;

public interface TagCountService {

     void update(TaskStatsChangedEvent event);
}
