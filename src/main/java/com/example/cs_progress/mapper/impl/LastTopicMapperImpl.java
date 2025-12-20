package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_common.util.BaseMapper;
import com.example.cs_progress.mapper.LastTopicMapper;
import com.example.cs_progress.model.entity.LastTopic;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class LastTopicMapperImpl extends BaseMapper implements LastTopicMapper {

    @Override
    public LastTopic toLastTopic(@NonNull final LessonViewedEvent event) {
        log.info("Mapping LessonViewedEvent: {} to LastTopic", event);

        return LastTopic.builder()
                .id(event.getLastTopicId())
                .topicId(event.getTopicId())
                .build();
    }
}
