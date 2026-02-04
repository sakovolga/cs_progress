package com.example.cs_progress.service.impl;

import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TopicOverview;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.repository.TopicOverviewRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.CacheEvictionService;
import com.example.cs_progress.service.TopicProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TopicProgressServiceImpl extends BaseService implements TopicProgressService {

    private final TopicProgressRepository topicProgressRepository;
    private final TopicOverviewRepository topicOverviewRepository;
    private final CacheEvictionService cacheEvictionService;

    @Override
    @Transactional
    public void updateTaskStatsInTopicProgress(@NonNull final String userId,
                                               @NonNull final String courseId,
                                               @NonNull final String topicId) {
        log.info("Updating task stats in TopicProgress for userId: {}, courseId: {}, topicId: {}",
                userId, courseId, topicId);

        TopicProgress topicProgress = topicProgressRepository.findByUserIdAndTopicId(userId, topicId)
                .orElse(TopicProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .topicId(topicId)
                        .build());

        TopicOverview topicOverview = topicOverviewRepository.findByTopicId(topicId)
                .orElseThrow(() -> new NotFoundException(
                        "TaskTopicCount not found for topicId: " + topicId, ENTITY_NOT_FOUND_ERROR)
                );
        topicProgress.setTotalTasks(topicOverview.getCount());
        topicProgress.incrementCompletedTasks();
        topicProgressRepository.save(topicProgress);
        cacheEvictionService.evictTopicProgress(userId);
    }

}
