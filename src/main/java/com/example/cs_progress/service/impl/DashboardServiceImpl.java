package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagProgressRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressListRs;
import com.example.cs_common.dto.response.DashboardTopicProgressRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagProgress;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.CourseOverviewCacheService;
import com.example.cs_progress.service.DashboardService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl extends BaseService implements DashboardService {

    private final CourseOverviewCacheService courseOverviewCacheService;
    private final TopicProgressRepository topicProgressRepository;
    private final TagProgressRepository tagProgressRepository;

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

    @Override
    @Transactional(readOnly = true)
    public DashboardTopicProgressListRs getUserDashboardTopicsTab(@NonNull final String userId,
                                                                  @NonNull final String courseId) {
        log.info("Getting dashboard topics progress for userId: {}, courseId: {}", userId, courseId);

        List<DashboardTopicProgressRs> dashboardTopicProgressList = topicProgressRepository
                .findByUserIdAndCourseId(userId, courseId);

        DashboardTopicProgressListRs rs = DashboardTopicProgressListRs.builder()
                .userId(userId)
                .courseId(courseId)
                .topicProgressList(dashboardTopicProgressList)
                .build();

        log.info("{} Dashboard topics progress received successfully", rs.getTopicProgressList().size());
        return rs;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTagsTabRs getUserDashboardTagsTab(
            @NonNull final String userId,
            @NonNull final String courseId
    ) {
        log.info("Attempting to get dashboard tags tab for userId: {} and courseId: {}", userId, courseId);

        Map<String, TagProgress> tagProgressMap = tagProgressRepository
                .findByUserIdAndCourseId(userId, courseId)
                .stream()
                .collect(Collectors.toMap(
                        TagProgress::getTagName,
                        Function.identity()
                ));

        Set<TagCount> tagCounts = courseOverviewCacheService
                .findByCourseId(courseId)
                .orElseThrow(() -> new NotFoundException(
                        "CourseOverview not found for courseId: " + courseId + ", synchronization needed",
                        ENTITY_NOT_FOUND_ERROR
                ))
                .getTagCounts();

        List<DashboardTagProgressRs> tagProgressList = tagCounts.stream()
                .map(tagCount -> {
                    TagProgress tagProgress = tagProgressMap.get(tagCount.getTagName());

                    int totalCount = tagCount.getCount();
                    int completedCount = tagProgress != null ? tagProgress.getResolvedTasks() : 0;

                    double strengthScore = totalCount > 0
                            ? (double) completedCount / totalCount
                            : 0.0;

                    return DashboardTagProgressRs.builder()
                            .tagName(tagCount.getTagName())
                            .totalCount(totalCount)
                            .completedCount(completedCount)
                            .strengthScore(strengthScore)
                            .build();
                })
                .sorted(Comparator.comparingDouble(DashboardTagProgressRs::getStrengthScore))
                .toList();

        DashboardTagsTabRs result = DashboardTagsTabRs.builder()
                .userId(userId)
                .courseId(courseId)
                .tagProgressList(tagProgressList)
                .build();

        log.info(
                "Dashboard tags tab for userId: {} and courseId: {} constructed successfully with {} tags",
                userId, courseId, tagProgressList.size()
        );

        return result;
    }

}
