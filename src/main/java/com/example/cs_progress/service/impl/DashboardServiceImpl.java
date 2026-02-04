package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.CourseOverviewCacheService;
import com.example.cs_progress.service.DashboardService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl extends BaseService implements DashboardService {

    private final CourseOverviewCacheService courseOverviewCacheService;
    private final TopicProgressRepository topicProgressRepository;

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

        List<CourseOverview> courses = new ArrayList<>();
        courseIds
                .forEach(courseId -> {
                    CourseOverview courseOverview = courseOverviewCacheService.findByCourseId(courseId)
                            .orElseThrow(() -> new NotFoundException(
                                    "CourseOverview not found for courseId: " + courseId + ", synchronization needed",
                                    ENTITY_NOT_FOUND_ERROR)
                            );
                    courses.add(courseOverview);
                });

        Map<String, CourseOverview> courseMap = courses.stream()
                .collect(Collectors.toMap(CourseOverview::getCourseId, ci -> ci));

        DashboardRs dashboardRs = new DashboardRs();
        dashboardRs.setUserId(userId);
        completedTopicsByCourse.forEach((key, value) -> {
            DashboardCourseInfoRs courseInfoRs = DashboardCourseInfoRs.builder()
                    .courseId(key)
                    .courseName(courseMap.get(key).getCourseName())
                    .totalTopics(courseMap.get(key).getTotalTopics())
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
