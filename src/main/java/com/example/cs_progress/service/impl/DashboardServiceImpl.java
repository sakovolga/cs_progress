package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import org.springframework.cache.annotation.Cacheable;
import java.util.Set;
import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagProgressRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressListRs;
import com.example.cs_common.dto.response.DashboardTopicProgressRs;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagProgress;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.repository.UserCourseCompletionRepository;
import com.example.cs_progress.service.CourseCompletionService;
import com.example.cs_progress.service.CourseOverviewCacheService;
import com.example.cs_progress.service.DashboardService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final UserCourseCompletionRepository userCourseCompletionRepository;
    private final CourseCompletionService courseCompletionService;

    @Override
    @Transactional
    public DashboardRs getUserDashboard(@NonNull final String userId) {
        log.info("Attempting to get dashboard for userId: {}", userId);

        List<DashboardCourseInfoRs> courses = topicProgressRepository.findDashboardByUserId(userId);

        if (courses.isEmpty()) {
            log.info("No topic progress found for userId: {}", userId);
            return DashboardRs.builder().userId(userId).courses(List.of()).build();
        }

        courses.forEach(course -> {
            if (course.getTotalTopics() != null && course.getTotalTopics() > 0
                    && course.getCompletedTopics() >= course.getTotalTopics()
                    && course.getCompletedAt() == null) {
                courseCompletionService.checkAndMarkCourseCompleted(userId, course.getCourseId());
                course.setCompletedAt(LocalDateTime.now());
            }
        });

        List<String> toMarkShown = courses.stream()
                .filter(c -> c.getCompletedAt() != null && !c.isCelebrationShown())
                .map(DashboardCourseInfoRs::getCourseId)
                .toList();

        if (!toMarkShown.isEmpty()) {
            userCourseCompletionRepository.markCelebrationShown(userId, toMarkShown);
        }

        log.info("Dashboard for userId: {} constructed successfully with {} courses", userId, courses.size());
        return DashboardRs.builder().userId(userId).courses(courses).build();
    }

    @Override
    @Cacheable(value = "topic-progress", key = "#userId + ':' + #courseId")
    public Set<String> getCompletedTopicIds(@NonNull final String userId, @NonNull final String courseId) {
        log.info("Getting completed topic IDs for userId: {}, courseId: {}", userId, courseId);

        return topicProgressRepository.findCompletedTopicIdsByUserIdAndCourseId(userId, courseId);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardTopicProgressListRs getUserDashboardTopicsTab(@NonNull final String userId,
                                                                  @NonNull final String courseId) {
        log.info("Getting dashboard topics progress for userId: {}, courseId: {}", userId, courseId);

        List<DashboardTopicProgressRs> dashboardTopicProgressList = topicProgressRepository
                .findByUserIdAndCourseId(userId, courseId);

        Double avgSkillScore = averageStrengthScore(safeBuildTagProgressList(userId, courseId));

        DashboardTopicProgressListRs rs = DashboardTopicProgressListRs.builder()
                .userId(userId)
                .courseId(courseId)
                .topicProgressList(dashboardTopicProgressList)
                .avgSkillScore(avgSkillScore)
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

        List<DashboardTagProgressRs> tagProgressList = buildTagProgressList(userId, courseId);

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

    private List<DashboardTagProgressRs> buildTagProgressList(final String userId, final String courseId) {
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

        return tagCounts.stream()
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
                .sorted(Comparator.comparingDouble(DashboardTagProgressRs::getStrengthScore).reversed())
                .toList();
    }

    /**
     * Unlike the tags tab, the topics tab renders on every dashboard page view, so a course
     * whose tag overview hasn't been synchronized yet must not break the whole page — it just
     * means no skill average is available yet.
     */
    private List<DashboardTagProgressRs> safeBuildTagProgressList(final String userId, final String courseId) {
        try {
            return buildTagProgressList(userId, courseId);
        } catch (NotFoundException e) {
            log.info("CourseOverview not yet synchronized for courseId: {}, skipping avgSkillScore", courseId);
            return List.of();
        }
    }

    private Double averageStrengthScore(final List<DashboardTagProgressRs> tagProgressList) {
        if (tagProgressList == null || tagProgressList.isEmpty()) {
            return null;
        }
        return tagProgressList.stream()
                .mapToDouble(DashboardTagProgressRs::getStrengthScore)
                .average()
                .getAsDouble();
    }

}
