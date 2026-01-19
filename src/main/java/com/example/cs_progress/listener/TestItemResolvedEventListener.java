package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseListener;
import com.example.cs_progress.service.TagProgressService;
import com.example.cs_progress.service.TestProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TestItemResolvedEventListener extends BaseListener {

    private final TestProgressService testProgressService;
    private final TagProgressService tagProgressService;

    @RabbitListener(queues = "test.item.resolved.queue")
    public void handleTestItemResolved(@Payload TestItemResolvedEvent event) {
        log.info("Received test item resolved event: " +
                        "userId={}, testId={}, testItemId={}, index={}, score={}, topicId={}, courseId={}",
                event.getUserId(),
                event.getTestId(),
                event.getTestItemId(),
                event.getIndex(),
                event.getTestItemScore(),
                event.getTopicId(),
                event.getCourseId());

        try {
            validateEvent(event);

            // 1. ✅ Обновляем TestProgress (критично - должно быть атомарно)
            testProgressService.processResolvedTestItem(event);
            log.info("Successfully updated TestProgress for testId={}", event.getTestId());

            // 2. ✅ Обновляем TagProgress (не критично если упадет)
            if (event.getTagNames() != null && !event.getTagNames().isEmpty()) {
                try {
                    tagProgressService.processTagsFromResolvedTestItem(
                            event.getCourseId(),
                            event.getTopicId(),
                            event.getUserId(),
                            event.getTagNames(),
                            event.getTestItemScore()
                    );
                    log.info("Successfully updated TagProgress for userId={}", event.getUserId());
                } catch (Exception e) {
                    log.error("Failed to update TagProgress, but TestProgress was saved", e);
                }
            }

            log.info("Successfully processed test item resolved event for testId={}",
                    event.getTestId());

        } catch (IllegalArgumentException e) {
            log.error("Invalid test item resolved event: {}", e.getMessage());

        } catch (NotFoundException e) {
            log.warn("TestProgress not found for testId: {}. Event will be acknowledged.",
                    event.getTestId());
            log.info("Event details: {}", event);

        } catch (Exception e) {
            log.error("Failed to process test item resolved event", e);
            throw e;
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