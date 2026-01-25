package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TaskTopicCount;
import com.example.cs_progress.repository.TaskTopicCountRepository;
import com.example.cs_progress.service.TaskTopicCountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TaskTopicCountServiceImpl extends BaseService implements TaskTopicCountService {

    private final TaskTopicCountRepository taskTopicCountRepository;

    @Override
    @Transactional
    public void updateTaskTopicCount(@NonNull final TaskStatsChangedEvent event) {
        log.info("Updating task topic count for topicId: {}", event.getTopicId());

        TaskTopicCount taskTopicCount = taskTopicCountRepository.findByTopicId(event.getTopicId())
                .orElse(TaskTopicCount.builder()
                        .courseId(event.getCourseId())
                        .topicId(event.getTopicId())
                        .build());
        if(event.getIsTaskCreated()) {
            log.info("Incrementing task topic count for topicId: {}", event.getTopicId());
            taskTopicCount.incrementCount();
        } else if (event.getIsTaskDeleted()) {
            log.info("Decrementing task topic count for topicId: {}", event.getTopicId());
            taskTopicCount.decrementCount();
        }
        taskTopicCountRepository.save(taskTopicCount);
    }
}
