package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.TaskCompletedEvent;
import com.example.cs_common.util.BaseListener;
import com.example.cs_progress.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskCompletedEventListener extends BaseListener {

    private final TaskProgressService taskProgressService;

    @RabbitListener(queues = "task.completed.queue")
    public void handleTaskCompletedEvent(@Payload TaskCompletedEvent event) {
        log.info("Received task completed event: taskProgressId={}, taskStatus={}, codeQualityRating={}",
                event.getTaskProgressId(), event.getTaskStatus(), event.getCodeQualityRating());

        try {
            validateEvent(event);
            taskProgressService.updateStatusAndRating(event);
            log.info("Successfully processed task completed event for taskProgressId: {}",
                    event.getTaskProgressId());

        } catch (IllegalArgumentException e) {
            log.error("Invalid task completed event: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Failed to task completed event", e);
            throw e;
        }
    }

    private void validateEvent(TaskCompletedEvent event) {
        if (event.getTaskProgressId() == null ||
                event.getTaskProgressId().isBlank()) {
            throw new IllegalArgumentException("taskProgressId cannot be null or empty");
        }
        if (event.getTaskStatus() == null) {
            throw new IllegalArgumentException("taskStatus cannot be null");
        }
        if (event.getCodeQualityRating() == null) {
            throw new IllegalArgumentException("getCodeQualityRating cannot be null");
        }
    }
}
