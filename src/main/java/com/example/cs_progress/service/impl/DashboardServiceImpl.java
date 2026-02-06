package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import com.example.cs_common.dto.response.DashboardRs;
import com.example.cs_common.dto.response.DashboardTagProgressRs;
import com.example.cs_common.dto.response.DashboardTagsTabRs;
import com.example.cs_common.dto.response.DashboardTopicProgressRs;
import com.example.cs_common.dto.response.DashboardTopicsTabRs;
import com.example.cs_common.dto.response.ParentGroupRs;
import com.example.cs_common.dto.response.TopicGroupRs;
import com.example.cs_common.dto.response.TopicProgressRs;
import com.example.cs_common.dto.response.TopicWithProgressRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagProgress;
import com.example.cs_progress.model.entity.TopicOverview;
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
    public DashboardTopicsTabRs getUserDashboardTopicsTab(
            @NonNull String userId,
            @NonNull String courseId
    ) {
        log.info("Getting dashboard topics tab for userId: {}, courseId: {}", userId, courseId);

        // 1. Получаем прогресс
        Map<String, DashboardTopicProgressRs> progressMap =
                topicProgressRepository.findByUserIdAndCourseId(userId, courseId)
                        .stream()
                        .collect(Collectors.toMap(
                                DashboardTopicProgressRs::getTopicId,
                                Function.identity()
                        ));

        log.info("Found {} topic progress records for user", progressMap.size());

        CourseOverview courseOverview = courseOverviewCacheService
                .findByCourseId(courseId)
                .orElseThrow(() -> new NotFoundException(
                        "CourseOverview not found for courseId: " + courseId + ", synchronization needed",
                        ENTITY_NOT_FOUND_ERROR)
                );

        log.info("CourseOverview has {} topic overviews", courseOverview.getTopicOverviews().size());

        courseOverview.getTopicOverviews().forEach(topic ->
                log.info("TopicOverview: topicId={}, topicName={}, parentId={}, parentName={}, grandparentId={}, grandparentName={}",
                        topic.getTopicId(),
                        topic.getTopicName(),
                        topic.getParentId(),
                        topic.getParentName(),
                        topic.getGrandparentId(),
                        topic.getGrandparentName()
                )
        );

        List<TopicGroupRs> topicGroups = buildTopicTree(
                courseOverview.getTopicOverviews(),
                progressMap
        );

        // ← ДОБАВЬТЕ ЭТО ЛОГИРОВАНИЕ
        log.info("Built {} topic groups", topicGroups.size());

        topicGroups.forEach(group -> {
            log.info("TopicGroup: grandparentName={}, parents count={}",
                    group.getGrandparentName(),
                    group.getParents().size()
            );

            group.getParents().forEach(parent ->
                    log.info("  ParentGroup: parentName={}, topics count={}",
                            parent.getParentName(),
                            parent.getTopics().size()
                    )
            );
        });

        DashboardTopicsTabRs rs = DashboardTopicsTabRs.builder()
                .courseId(courseId)
                .userId(userId)
                .topicGroups(topicGroups)
                .build();

        log.info("Dashboard topics tab constructed successfully");
        return rs;
    }

    private List<TopicGroupRs> buildTopicTree(List<TopicOverview> topicOverviews,
                                              Map<String, DashboardTopicProgressRs> progressMap) {
        // Группируем по grandparent (null → "ROOT")
        Map<String, List<TopicOverview>> byGrandparent = topicOverviews.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getGrandparentId() != null ? t.getGrandparentId() : "ROOT"
                ));

        return byGrandparent.entrySet().stream()
                .map(entry -> {
                    String grandparentKey = entry.getKey();
                    List<TopicOverview> topics = entry.getValue();
                    TopicOverview first = topics.getFirst();

                    // Строим parent groups
                    List<ParentGroupRs> parents = buildParentGroups(topics, progressMap);

                    // Если grandparent = "ROOT", используем null
                    boolean isRoot = "ROOT".equals(grandparentKey);

                    return TopicGroupRs.builder()
                            .grandparentId(isRoot ? null : first.getGrandparentId())
                            .grandparentName(isRoot ? null : first.getGrandparentName())
                            .grandparentOrder(isRoot ? null : first.getGrandparentOrder())
                            .parents(parents)
                            .build();
                })
                // Сортировка: сначала с grandparent, потом без
                .sorted(Comparator.comparing(
                        group -> group.getGrandparentOrder() != null ? group.getGrandparentOrder() : Integer.MAX_VALUE
                ))
                .toList();
    }

    private List<ParentGroupRs> buildParentGroups(
            List<TopicOverview> topics,
            Map<String, DashboardTopicProgressRs> progressMap
    ) {
        // Группируем по parent (null → "ROOT")
        Map<String, List<TopicOverview>> byParent = topics.stream()
                .collect(Collectors.groupingBy(
                        t -> t.getParentId() != null ? t.getParentId() : "ROOT"
                ));

        return byParent.entrySet().stream()
                .map(entry -> {
                    String parentKey = entry.getKey();
                    List<TopicOverview> parentTopics = entry.getValue();
                    TopicOverview first = parentTopics.getFirst();

                    boolean isRoot = "ROOT".equals(parentKey);

                    // Строим топики с прогрессом
                    List<TopicWithProgressRs> topicsWithProgress = parentTopics.stream()
                            .map(topic -> buildTopicWithProgress(topic, progressMap))
                            .sorted(Comparator.comparing(TopicWithProgressRs::getOrderIndex))
                            .toList();

                    return ParentGroupRs.builder()
                            .parentId(isRoot ? null : first.getParentId())
                            .parentName(isRoot ? null : first.getParentName())
                            .parentOrder(isRoot ? null : first.getParentOrder())
                            .topics(topicsWithProgress)
                            .build();
                })
                // Сортировка: сначала с parent, потом без
                .sorted(Comparator.comparing(
                        group -> group.getParentOrder() != null ? group.getParentOrder() : Integer.MAX_VALUE
                ))
                .toList();
    }

    private TopicWithProgressRs buildTopicWithProgress(
            TopicOverview topic,
            Map<String, DashboardTopicProgressRs> progressMap
    ) {
        DashboardTopicProgressRs progress = progressMap.get(topic.getTopicId());

        return TopicWithProgressRs.builder()
                .topicId(topic.getTopicId())
                .topicName(topic.getTopicName())
                .orderIndex(topic.getOrderIndex())
                .progress(progress != null ?
                        TopicProgressRs.builder()
                                .bestTestScorePercentage(progress.getBestTestScorePercentage())
                                .taskCompletionPercentage(progress.getTaskCompletionPercentage())
                                .status(progress.getStatus())
                                .build()
                        : null)
                .build();
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
