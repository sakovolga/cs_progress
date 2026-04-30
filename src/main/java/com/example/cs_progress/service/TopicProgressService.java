package com.example.cs_progress.service;

import com.example.cs_progress.model.entity.TopicProgress;

public interface TopicProgressService {

    void updateTaskStatsInTopicProgress(String userId, String courseId, String topicId);

    TopicProgress getOrCreateTopicProgress(String userId, String courseId, String topicId);

}
