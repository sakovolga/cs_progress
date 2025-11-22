package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_progress.repository.LastTopicRepository;
import com.example.cs_progress.model.entity.LastTopic;
import com.example.cs_progress.service.LastTopicService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LastTopicServiceImpl implements LastTopicService {

    private final LastTopicRepository lastTopicRepository;

    @Override
    @Transactional(readOnly = true)
    public String get(@NonNull final String courseId, @NonNull final String userId) {
        log.info("Attempting to get current topicId in course with id: {} for user with id: {}",
                courseId, userId);

        //TO DO можно не вытягивать всю сущность, а только айдишник
        LastTopic lastTopic = lastTopicRepository.getByCourseIdAndUserId(courseId, userId).orElse(null);

        if (lastTopic != null) {
            log.info("TopicId: {} was received", lastTopic.getTopicId());
            return lastTopic.getTopicId();
        } else {
            log.info("There is no current topic for the userId: {}", userId);
            return null;
        }
    }

    @Override
    @Transactional
    public void saveOrUpdateLastTopic(LessonViewedEvent event) {
        log.info("Processing lesson viewed event for user: {}, course: {}, topic: {}",
                event.getUserId(), event.getCourseId(), event.getTopicId());

        try {
            // Ищем существующую запись
            Optional<LastTopic> existingTopic = lastTopicRepository
                    .getByCourseIdAndUserId(event.getCourseId(), event.getUserId());

            if (existingTopic.isPresent()) {
                // Обновляем существующую запись
                LastTopic lastTopic = existingTopic.get();
                lastTopic.setTopicId(event.getTopicId());
                lastTopicRepository.save(lastTopic);

                log.info("Updated last topic for user: {}, course: {}, new topic: {}",
                        event.getUserId(), event.getCourseId(), event.getTopicId());
            } else {
                // Создаём новую запись
                LastTopic newLastTopic = LastTopic.builder()
                        .userId(event.getUserId())
                        .courseId(event.getCourseId())
                        .topicId(event.getTopicId())
                        .build();

                lastTopicRepository.save(newLastTopic);

                log.info("Created new last topic record for user: {}, course: {}, topic: {}",
                        event.getUserId(), event.getCourseId(), event.getTopicId());
            }

        } catch (Exception e) {
            log.error("Failed to save/update last topic for user: {}, course: {}, topic: {}",
                    event.getUserId(), event.getCourseId(), event.getTopicId(), e);
            throw e; // Пробрасываем исключение для retry механизма
        }
    }
}
