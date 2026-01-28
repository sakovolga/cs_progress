package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TaskTopicCount;
import com.example.cs_progress.service.TaskTopicCountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class TaskTopicCountServiceImpl extends BaseService implements TaskTopicCountService {

    @Override
    @Transactional
    public void updateTaskTopicCount(@NonNull final TaskStatsChangedEvent event,
                                     @NonNull CourseOverview courseOverview) {
        log.info("Updating task topic count for topicId: {}", event.getTopicId());


        TaskTopicCount taskTopicCount = courseOverview.getTaskTopicCounts().stream()
                .filter(ttc -> Objects.equals(ttc.getTopicId(), event.getTopicId()))
                .findFirst()
                .orElseGet(() -> {
                        TaskTopicCount count = TaskTopicCount.builder()
                                .topicId(event.getTopicId())
                                .build();
                        courseOverview.getTaskTopicCounts().add(count);
                        count.setCourseOverview(courseOverview);
                        return count;
                });

        if(event.getIsTaskCreated()) {
            log.info("Incrementing task topic count for topicId: {}", event.getTopicId());
            taskTopicCount.incrementCount();
        } else if (event.getIsTaskDeleted()) {
            log.info("Decrementing task topic count for topicId: {}", event.getTopicId());
            taskTopicCount.decrementCount();
        }


//        taskTopicCountRepository.save(taskTopicCount);
    }
}
