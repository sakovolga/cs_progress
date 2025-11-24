package com.example.cs_progress.mapper;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_progress.model.entity.LastTopic;

public interface LastTopicMapper {

    LastTopic toLastTopic(LessonViewedEvent event);
}
