package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.CourseCompletionRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.UserCourseCompletion;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.repository.UserCourseCompletionRepository;
import com.example.cs_progress.service.CourseCompletionService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class CourseCompletionServiceImpl extends BaseService implements CourseCompletionService {

    private final UserCourseCompletionRepository userCourseCompletionRepository;
    private final TopicProgressRepository topicProgressRepository;
    private final CourseOverviewRepository courseOverviewRepository;

    @Override
    @Transactional
    public void checkAndMarkCourseCompleted(@NonNull final String userId,
                                            @NonNull final String courseId) {
        if (userCourseCompletionRepository.existsByUserIdAndCourseId(userId, courseId)) {
            return;
        }

        CourseOverview courseOverview = courseOverviewRepository.findByCourseId(courseId).orElse(null);
        if (courseOverview == null || courseOverview.getTotalTopics() == null
                || courseOverview.getTotalTopics() == 0) {
            log.warn("CourseOverview not found or has no topics for courseId: {}", courseId);
            return;
        }

        long completedTopics = topicProgressRepository
                .countByUserIdAndCourseIdAndStatus(userId, courseId, TopicStatus.COMPLETED);

        if (completedTopics >= courseOverview.getTotalTopics()) {
            log.info("Course {} completed by user {}. Saving completion record.", courseId, userId);
            UserCourseCompletion completion = UserCourseCompletion.builder()
                    .userId(userId)
                    .courseId(courseId)
                    .courseName(courseOverview.getCourseName())
                    .completedAt(LocalDateTime.now())
                    .build();
            userCourseCompletionRepository.save(completion);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CourseCompletionRs getCourseCompletion(@NonNull final String userId,
                                                  @NonNull final String courseId) {
        UserCourseCompletion completion = userCourseCompletionRepository
                .findByUserIdAndCourseId(userId, courseId)
                .orElseThrow(() -> new NotFoundException(
                        "Course completion not found for userId: " + userId + " and courseId: " + courseId,
                        ENTITY_NOT_FOUND_ERROR));

        return new CourseCompletionRs(completion.getUserId(), completion.getCourseId(),
                completion.getCourseName(), completion.getCompletedAt());
    }

}