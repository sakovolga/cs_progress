package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.AIRatingUpdatedEvent;
import com.example.cs_common.util.BaseListener;
import com.example.cs_progress.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AIRatingUpdatedEventListener extends BaseListener {

    private final TaskProgressService taskProgressService;

    @RabbitListener(queues = "ai.rating.updated.queue")
    public void handleAIRatingUpdatedEvent(@Payload AIRatingUpdatedEvent event) {
        log.info("Received AI rating updated event: taskProgressId={}, codeQualityRating={}",
                event.getTaskProgressId(), event.getCodeQualityRating());

        try {
            validateEvent(event);
            taskProgressService.processAIRatingUpdatedEvent(event);
            log.info("Successfully processed AI rating updated event for taskProgressId: {}",
                    event.getTaskProgressId());

        } catch (IllegalArgumentException e) {
            log.error("Invalid AI rating updated event: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Failed to process AI rating updated event", e);
            throw e;
        }
    }

    private void validateEvent(AIRatingUpdatedEvent event) {
        if (event.getTaskProgressId() == null || event.getTaskProgressId().isBlank()) {
            throw new IllegalArgumentException("taskProgressId cannot be null or empty");
        }
    }
}