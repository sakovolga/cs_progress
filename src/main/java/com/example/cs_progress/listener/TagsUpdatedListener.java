package com.example.cs_progress.listener;

import com.example.cs_common.dto.event.TagsUpdatedEvent;
import com.example.cs_common.util.BaseListener;
import com.example.cs_progress.service.TagTaskCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TagsUpdatedListener extends BaseListener {

    private final TagTaskCountService tagTaskCountService;

    @RabbitListener(queues = "tags.updated.queue")
    public void handleTagsUpdatedEvent(@Payload TagsUpdatedEvent event) {
        log.info("Received tags updated event: courseId={}, topicId={}, tagsAdded={}, tagsRemoved={}",
                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(), event.getTagNamesRemoved());
        try {
            validateEvent(event);
            tagTaskCountService.update(event);
            log.info("Successfully processed tags updated event");

        } catch (IllegalArgumentException e) {
            log.error("Invalid tags updated event: {}", e.getMessage());

        } catch (Exception e) {
            log.error("Failed to process tags updated event", e);
            throw e;
        }
    }

    private void validateEvent(TagsUpdatedEvent event) {
        if (event.getCourseId() == null || event.getCourseId().isBlank()) {
            throw new IllegalArgumentException("courseId cannot be null or empty");
        }
        if (event.getTopicId() == null || event.getTopicId().isBlank()) {
            throw new IllegalArgumentException("snapshot cannot be null");
        }
        if ((event.getTagNamesAdded() == null || event.getTagNamesAdded().isEmpty()) &&
        (event.getTagNamesRemoved() == null || event.getTagNamesRemoved().isEmpty())) {
            throw new IllegalArgumentException("At least one of tagNamesAdded or tagNamesRemoved must be provided");
        }
    }
}
