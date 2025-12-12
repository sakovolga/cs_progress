package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_progress.service.LastTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonViewedEventListener {

    private final LastTopicService lastTopicService;

    @RabbitListener(queues = "lesson.viewed.queue") // Точное имя очереди!
    public void handleLessonViewedEvent(@Payload LessonViewedEvent event) {
        log.info("🎯 Received lesson viewed event: userId={}, courseId={}, topicId={}",
                event.getLastTopicId().getUserId(),
                event.getLastTopicId().getCourseId(),
                event.getTopicId());

        try {
            validateEvent(event);
            lastTopicService.saveOrUpdateLastTopic(event);
            log.info("✅ Successfully processed lesson viewed event for user: {}",
                    event.getLastTopicId().getUserId());

        } catch (IllegalArgumentException e) {
            log.error("❌ Invalid lesson viewed event: {}", e.getMessage());

        } catch (Exception e) {
            log.error("❌ Failed to process lesson viewed event", e);
            throw e;
        }
    }

    private void validateEvent(LessonViewedEvent event) {
        if (event.getLastTopicId().getUserId() == null ||
                event.getLastTopicId().getUserId().isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        if (event.getLastTopicId().getCourseId() == null ||
                event.getLastTopicId().getCourseId().isBlank()) {
            throw new IllegalArgumentException("courseId cannot be null or empty");
        }
        if (event.getTopicId() == null || event.getTopicId().isBlank()) {
            throw new IllegalArgumentException("topicId cannot be null or empty");
        }
    }
}