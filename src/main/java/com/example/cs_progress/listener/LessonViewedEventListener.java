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

    /**
     * Слушает события просмотра уроков из RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.lesson-viewed:progress.lesson.viewed}")
    public void handleLessonViewedEvent(@Payload LessonViewedEvent event) {
        log.info("Received lesson viewed event: userId={}, courseId={}, topicId={}",
                event.getUserId(), event.getCourseId(), event.getTopicId());

        try {
            // Валидация события
            validateEvent(event);

            // Сохранение/обновление последнего топика
            lastTopicService.saveOrUpdateLastTopic(event);

            log.info("Successfully processed lesson viewed event for user: {}", event.getUserId());

        } catch (IllegalArgumentException e) {
            // Невалидное событие - логируем и не ретраим
            log.error("Invalid lesson viewed event: {}", e.getMessage());
            // Сообщение будет acknowledge и удалено из очереди

        } catch (Exception e) {
            // Ошибка обработки - пробрасываем для retry
            log.error("Failed to process lesson viewed event", e);
            throw e; // RabbitMQ сделает retry согласно настройкам
        }
    }

    /**
     * Валидация события
     */
    private void validateEvent(LessonViewedEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        if (event.getCourseId() == null || event.getCourseId().isBlank()) {
            throw new IllegalArgumentException("courseId cannot be null or empty");
        }
        if (event.getTopicId() == null || event.getTopicId().isBlank()) {
            throw new IllegalArgumentException("topicId cannot be null or empty");
        }
    }
}
