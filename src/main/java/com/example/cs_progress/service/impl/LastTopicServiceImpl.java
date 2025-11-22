package com.example.cs_progress.service.impl;

import com.example.cs_progress.repository.LastTopicRepository;
import com.example.cs_progress.model.entity.LastTopic;
import com.example.cs_progress.service.LastTopicService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastTopicServiceImpl implements LastTopicService {

    private final LastTopicRepository lastTopicRepository;

    @Override
    public String get(@NonNull final String courseId, @NonNull final String userId) {
        log.info("Attempting to get current topicId in course with id: {} for user with id: {}",
                courseId, userId);

        LastTopic lastTopic = lastTopicRepository.getByCourseIdAndUserId(courseId, userId).orElse(null);

        if (lastTopic != null) {
            log.info("TopicId: {} was received", lastTopic.getTopicId());
            return lastTopic.getTopicId();
        } else {
            log.info("There is no current topic for the userId: {}", userId);
            return null;
        }
    }
}
