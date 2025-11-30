package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_progress.service.TestProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TestItemResolvedEventListener {

    private final TestProgressService testProgressService;

    /**
     * Listens for "test item resolved" events from RabbitMQ
     */
    @RabbitListener(queues = "${rabbitmq.queue.test-item-resolved:progress.test.item.resolved}")
    public void handleTestItemResolved(@Payload TestItemResolvedEvent event) {
        log.info("Received test item resolved event: userId={}, testId={}, score={}",
                event.getUserId(), event.getTestId(), event.getTestItemScore());

        try {
            validateEvent(event);

            testProgressService.processResolvedTestItem(event);

            log.info("Successfully processed test item resolved event for testId={}", event.getTestId());

        } catch (IllegalArgumentException e) {
            log.error("Invalid test item resolved event: {}", e.getMessage());
            // ack and remove
        } catch (Exception e) {
            log.error("Failed to process test item resolved event", e);
            throw e; // retry
        }
    }

    private void validateEvent(TestItemResolvedEvent event) {
        if (event.getUserId() == null || event.getUserId().isBlank()) {
            throw new IllegalArgumentException("userId cannot be null or empty");
        }
        if (event.getTestId() == null || event.getTestId().isBlank()) {
            throw new IllegalArgumentException("testId cannot be null or empty");
        }
        if (event.getTestItemScore() == null) {
            throw new IllegalArgumentException("score cannot be null");
        }
    }
}
