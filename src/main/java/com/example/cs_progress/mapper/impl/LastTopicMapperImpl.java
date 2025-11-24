package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_progress.mapper.LastTopicMapper;
import com.example.cs_progress.model.entity.LastTopic;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LastTopicMapperImpl implements LastTopicMapper {

    @Override
    public LastTopic toLastTopic(@NonNull final LessonViewedEvent event) {
        log.info("Mapping LessonViewedEvent: {} to LastTopic", event);

        return LastTopic.builder()
                .id(event.getLastTopicId())
                .topicId(event.getTopicId())
                .build();
    }
}
