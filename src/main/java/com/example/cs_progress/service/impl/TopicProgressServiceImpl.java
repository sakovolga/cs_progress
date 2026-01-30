package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TaskTopicCount;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.model.projection.CourseInfo;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.repository.TaskTopicCountRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.TopicProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TopicProgressServiceImpl extends BaseService implements TopicProgressService {

    private final TopicProgressRepository topicProgressRepository;
    private final TaskTopicCountRepository taskTopicCountRepository;
    private final CourseOverviewRepository courseOverviewRepository;

    @Override
    @Transactional
    @CacheEvict(value = "topic-progress", key = "#userId")
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

        TaskTopicCount taskTopicCount = taskTopicCountRepository.findByTopicId(topicId)
                .orElseThrow(() -> new NotFoundException(
                        "TaskTopicCount not found for topicId: " + topicId, ENTITY_NOT_FOUND_ERROR)
                );
        topicProgress.setTotalTasks(taskTopicCount.getCount());
        topicProgress.incrementCompletedTasks();
        topicProgressRepository.save(topicProgress);

    }

    @Override
    @Transactional(readOnly = true)
    public DashboardRs getUserDashboard(@NonNull final String userId) {
        log.info("Attempting to get dashboard for userId: {}", userId);

        List<TopicProgress> topicProgressList = topicProgressRepository.findByUserId(userId);

        if (topicProgressList.isEmpty()) {
            log.info("No topic progress found for userId: {}", userId);
            return DashboardRs.builder()
                    .userId(userId)
                    .courses(List.of())
                    .build();
        }

        Map<String, Integer> completedTopicsByCourse = topicProgressList.stream()
                .collect(Collectors.groupingBy(
                        TopicProgress::getCourseId,
                        Collectors.summingInt(tp ->
                                tp.getStatus() == TopicStatus.COMPLETED ? 1 : 0
                        )
                ));

        List<String> courseIds = completedTopicsByCourse.keySet().stream().toList();
        List<CourseInfo> courseInfos = courseOverviewRepository.findByCourseIdIn(courseIds);
        Map<String, CourseInfo> courseInfoMap = courseInfos.stream()
                .collect(Collectors.toMap(CourseInfo::getCourseId, ci -> ci));

        DashboardRs dashboardRs = new DashboardRs();
        dashboardRs.setUserId(userId);
        completedTopicsByCourse.forEach((key, value) -> {
            DashboardCourseInfoRs courseInfoRs = DashboardCourseInfoRs.builder()
                    .courseId(key)
                    .courseName(courseInfoMap.get(key).getCourseName())
                    .totalTopics(courseInfoMap.get(key).getTotalTopics())
                    .completedTopics(value)
                    .build();
            dashboardRs.getCourses().add(courseInfoRs);
        });

        log.info(
                "Dashboard for userId: {} constructed successfully with {} courses",
                userId, dashboardRs.getCourses().size()
        );
        return dashboardRs;
    }
}
