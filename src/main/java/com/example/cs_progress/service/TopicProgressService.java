package com.example.cs_progress.service;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import lombok.NonNull;

public interface TopicProgressService {

    void updateTaskStatsInTopicProgress(String userId, String courseId, String topicId);
}
