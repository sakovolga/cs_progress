package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.SnapshotSentEvent;
import com.example.cs_common.util.BaseListener;
import com.example.cs_progress.service.TaskProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SnapshotSentEventListener extends BaseListener {

    private final TaskProgressService taskProgressService;

    @RabbitListener(queues = "snapshot.sent.queue")
    public void handleSnapshotSentEvent(@Payload SnapshotSentEvent event) {
        log.info("Received snapshot sent event: taskProgressId={}, lastSnapshot={}",
                event.getTaskProgressId(), event.getLastSnapshot());
        try {
            validateEvent(event);
            taskProgressService.saveSnapshot(event);
            log.info("Successfully processed snapshot sent event for taskProgressId: {}",
                    event.getTaskProgressId());

        } catch (IllegalArgumentException e) {
            log.error("Invalid snapshot sent event: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Failed to snapshot sent event", e);
            throw e;
        }
    }

    private void validateEvent(SnapshotSentEvent event) {
        if (event.getTaskProgressId() == null || event.getTaskProgressId().isBlank()) {
            throw new IllegalArgumentException("taskProgressId cannot be null or empty");
        }
        if (event.getLastSnapshot() == null || event.getLastSnapshot().isBlank()) {
            throw new IllegalArgumentException("snapshot cannot be null");
        }
    }
}
