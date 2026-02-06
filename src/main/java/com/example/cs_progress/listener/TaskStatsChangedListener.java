//package com.example.cs_progress.listener;
//
//import com.example.cs_common.dto.event.TaskStatsChangedEvent;
//import com.example.cs_common.util.BaseListener;
//import com.example.cs_progress.service.CourseOverviewService;
//import lombok.RequiredArgsConstructor;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.messaging.handler.annotation.Payload;
//import org.springframework.stereotype.Component;
//
//@Component
//@RequiredArgsConstructor
//public class TaskStatsChangedListener extends BaseListener {
//
//    private final CourseOverviewService courseOverviewService;
//
//    @RabbitListener(queues = "task.stats.changed.queue")
//    public void handleTagsUpdatedEvent(@Payload TaskStatsChangedEvent event) {
//        log.info(
//                "Received task stats changed event: " +
//                        "courseId={}, topicId={}, tagsAdded={}, tagsRemoved={}, isTaskCreated={}, isTaskDeleted={}",
//                event.getCourseId(),
//                event.getTopicId(),
//                event.getTagNamesAdded(),
//                event.getTagNamesRemoved(),
//                event.getIsTaskCreated(),
//                event.getIsTaskDeleted()
//        );
//        try {
//            validateEvent(event);
//            courseOverviewService.handleTaskStatsChangedEvent(event);
//
//            log.info("Successfully processed task stats changed event");
//
//        } catch (IllegalArgumentException e) {
//            log.error("Invalid task stats changed event: {}", e.getMessage());
//
//        } catch (Exception e) {
//            log.error("Failed to process task stats changed event", e);
//            throw e;
//        }
//    }
//
//    private void validateEvent(TaskStatsChangedEvent event) {
//        if (event.getCourseId() == null || event.getCourseId().isBlank()) {
//            throw new IllegalArgumentException("courseId cannot be null or empty");
//        }
//        if (event.getTopicId() == null || event.getTopicId().isBlank()) {
//            throw new IllegalArgumentException("snapshot cannot be null");
//        }
//        if ((event.getTagNamesAdded() == null || event.getTagNamesAdded().isEmpty()) &&
//        (event.getTagNamesRemoved() == null || event.getTagNamesRemoved().isEmpty())) {
//            throw new IllegalArgumentException("At least one of tagNamesAdded or tagNamesRemoved must be provided");
//        }
//        if (event.getIsTaskCreated() && event.getIsTaskDeleted()) {
//            throw new IllegalArgumentException("A task cannot be both created and deleted");
//        }
//    }
//}
