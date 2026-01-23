package com.example.cs_progress.service.impl;

import com.example.cs_common.enums.TaskStatus;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TaskProgressRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.ProgressChangeDetector;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProgressChangeDetectorImpl extends BaseService implements ProgressChangeDetector {

    private final TopicProgressRepository topicProgressRepository;
    private final TaskProgressRepository taskProgressRepository;
    private final TagProgressRepository tagProgressRepository;

    /**
     * Проверить были ли изменения в прогрессе с указанного момента
     */
    @Override
    public boolean hasChanges(@NonNull String userId, @NonNull LocalDateTime since) {
        log.debug("Checking for changes for user: {} since: {}", userId, since);

        // Проверяем завершенные топики
        long completedTopics = topicProgressRepository
                .countByUserIdAndStatusAndUpdatedAtAfter(userId, TopicStatus.COMPLETED, since);

        if (completedTopics > 0) {
            log.info("Found {} completed topics since {}", completedTopics, since);
            return true;
        }

        // Проверяем решенные задачи
        long completedTasks = taskProgressRepository
                .countByUserIdAndTaskStatusAndUpdatedAtAfter(userId, TaskStatus.SOLVED, since);

        if (completedTasks > 0) {
            log.info("Found {} completed tasks since {}", completedTasks, since);
            return true;
        }

        // Проверяем обновления навыков
        long updatedTags = tagProgressRepository
                .countByUserIdAndLastActivityAfter(userId, since);

        if (updatedTags > 0) {
            log.info("Found {} updated skills since {}", updatedTags, since);
            return true;
        }

        log.info("No changes detected for user: {} since: {}", userId, since);
        return false;
    }
}