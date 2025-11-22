package com.example.cs_progress.service;

import com.example.cs_common.dto.event.LessonViewedEvent;

public interface LastTopicService {

    String get(String courseId, String userId);

    void saveOrUpdateLastTopic(LessonViewedEvent event);
}
