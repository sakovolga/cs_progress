package com.example.cs_progress.service;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_common.dto.key.LastTopicId;

public interface LastTopicService {

    String get(LastTopicId id);

    void saveOrUpdateLastTopic(LessonViewedEvent event);
}
