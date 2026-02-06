package com.example.cs_progress.service.impl;

import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TagProgress;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagTopicCount;
import com.example.cs_progress.model.entity.TagTopicProgress;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TagCountRepository;
import com.example.cs_progress.service.CacheEvictionService;
import com.example.cs_progress.service.TagProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagProgressServiceImpl extends BaseService implements TagProgressService {

    private final TagProgressRepository tagProgressRepository;
    private final TagCountRepository tagCountRepository;
    private final CacheEvictionService cacheEvictionService;

    private static final Double MAX_TEST_ITEM_SCORE = 10.0;

    @Override
    @Transactional
    public void processTagsFromResolvedTestItem(@NonNull final String courseId,
                                                @NonNull final String topicId,
                                                @NonNull final String userId,
                                                final List<String> tagNames,
                                                final Double score) {
        log.info(
                "Processing tag progress after resolving testItem " +
                        "for userId: {}, courseId: {}, topicId: {}, tags: {}, score: {}",
                userId, courseId, topicId, tagNames, score
        );

        if (isEmptyTagList(tagNames)) {
            log.info("No tags to process for the resolved test item");
            return;
        }

        Boolean isCorrect = isFullyCorrectAnswer(score);
        processTagsForActivity(courseId, topicId, userId, tagNames, isCorrect, false);

        cacheEvictionService.evictAIInsights(userId);

        log.info("Completed processing tag progress for resolved test item for userId: {}", userId);
    }

    @Override
    @Transactional
    public void processTagsFromCompletedTask(@NonNull final String courseId,
                                             @NonNull final String topicId,
                                             @NonNull final String userId,
                                             final List<String> tagNames) {
        log.info(
                "Processing tag progress after completing task " +
                        "for userId: {}, courseId: {}, topicId: {}, tags: {}",
                userId, courseId, topicId, tagNames
        );

        if (isEmptyTagList(tagNames)) {
            log.info("No tags to process for the completed task");
            return;
        }

        processTagsForActivity(courseId, topicId, userId, tagNames, null, true);

        log.info("Completed processing tag progress for completed task for userId: {}", userId);
    }

    /**
     * Общая логика обработки тегов для любой активности (тест или таска)
     */
    private void processTagsForActivity(@NonNull final String courseId,
                                        @NonNull final String topicId,
                                        @NonNull final String userId,
                                        @NonNull final List<String> tagNames,
                                        final Boolean isCorrect,
                                        final Boolean isTaskCompletion) {
        Map<String, TagProgress> existingTagProgressMap = loadExistingTagProgresses(tagNames, userId, courseId);

        for (String tagName : tagNames) {
            TagProgress tagProgress = existingTagProgressMap.get(tagName);

            if (tagProgress != null) {
                updateExistingTagProgress(courseId, tagProgress, topicId, isTaskCompletion);
            } else {
                createNewTagProgress(courseId, topicId, userId, tagName, isCorrect, isTaskCompletion);
            }
        }
    }

    /**
     * Загрузить существующие TagProgress в виде Map для быстрого доступа
     */
    private Map<String, TagProgress> loadExistingTagProgresses(@NonNull final List<String> tagNames,
                                                               @NonNull final String userId,
                                                               @NonNull final String courseId) {
        return tagProgressRepository
                .findByTagNameInAndUserIdAndCourseId(tagNames, userId, courseId)
                .stream()
                .collect(Collectors.toMap(TagProgress::getTagName, tp -> tp));
    }

    /**
     * Обновить существующий TagProgress
     */
    private void updateExistingTagProgress(@NonNull final String courseId,
                                           @NonNull final TagProgress tagProgress,
                                           @NonNull final String topicId,
//                                           final Boolean isCorrect,
                                           final Boolean isTaskCompletion) {
        TagTopicProgress tagTopicProgress = findOrCreateTagTopicProgress(courseId, tagProgress, topicId);

        // Обработка теста
//        if (isCorrect != null) {
//            updateTestMetrics(tagTopicProgress, tagProgress, isCorrect);
//        }

        // Обработка таски
        if (isTaskCompletion != null && isTaskCompletion) {
            updateTaskMetrics(tagTopicProgress, tagProgress);
        }
    }

    /**
     * Найти или создать TagTopicProgress для конкретного топика
     */
    private TagTopicProgress findOrCreateTagTopicProgress(@NonNull final String courseId,
                                                          @NonNull final TagProgress tagProgress,
                                                          @NonNull final String topicId) {
        return tagProgress.getTopicProgresses().stream()
                .filter(tp -> tp.getTopicId().equals(topicId))
                .findFirst()
                .orElseGet(() -> {
                    TagTopicProgress tp = createMissingTagTopicProgress(courseId, tagProgress, topicId);
                    tagProgress.getTopicProgresses().add(tp);
                    return tp;
                });

    }

    /**
     * Создать отсутствующий TagTopicProgress (когда топик не был изначально проинициализирован)
     */
    private TagTopicProgress createMissingTagTopicProgress(@NonNull final String courseId,
                                                           @NonNull final TagProgress tagProgress,
                                                           @NonNull final String topicId) {
        log.warn("TagTopicProgress not found for tag: {} in topic: {}. Creating new one.",
                tagProgress.getTagName(), topicId);

        Integer expectedTasks = findExpectedTasksForTopic(courseId, tagProgress.getTagName(), topicId);

        return TagTopicProgress.builder()
                .tagProgress(tagProgress)
                .topicId(topicId)
                .expectedTasks(expectedTasks)
                .build();
    }

    /**
     * Обновить метрики теста
     */
//    private void updateTestMetrics(@NonNull final TagTopicProgress tagTopicProgress,
//                                   @NonNull final TagProgress tagProgress,
//                                   final boolean isCorrect) {
//        log.debug("Updating test metrics for tag: {} in topic: {}, isCorrect: {}",
//                tagProgress.getTagName(), tagTopicProgress.getTopicId(), isCorrect);
//
//        if (isCorrect) {
//            tagTopicProgress.incrementCorrectTestAnswers();
//            tagProgress.recalculateCorrectTestAnswers();
//        } else {
//            tagTopicProgress.incrementQuestionsAnswered();
//            tagProgress.recalculateAnsweredTestQuestions();
//        }
//    }

    /**
     * Обновить метрики таски
     */
    private void updateTaskMetrics(@NonNull final TagTopicProgress tagTopicProgress,
                                   @NonNull final TagProgress tagProgress) {
        log.debug("Updating task metrics for tag: {} in topic: {}",
                tagProgress.getTagName(), tagTopicProgress.getTopicId());

        tagTopicProgress.incrementTaskCompleted();
        tagProgress.recalculateCompletedTasks();
    }

    /**
     * Создать новый TagProgress с полной инициализацией всех топиков
     */
    private void createNewTagProgress(@NonNull final String courseId,
                                      @NonNull final String topicId,
                                      @NonNull final String userId,
                                      @NonNull final String tagName,
                                      final Boolean isCorrect,
                                      final Boolean isTaskCompletion) {
        log.info("Building new TagProgress for tag: {} in topicId: {} in courseId: {}",
                tagName, topicId, courseId);

        TagCount expectedTaskCount = loadOrFailTagTaskCount(tagName, courseId);

        TagProgress tagProgress = TagProgress.builder()
                .tagName(tagName)
                .userId(userId)
                .courseId(courseId)
                .totalTasks(expectedTaskCount.getCount())
                .build();

        initializeAllTopicProgresses(tagProgress, expectedTaskCount, topicId, isCorrect, isTaskCompletion);

        tagProgressRepository.save(tagProgress);
    }

    /**
     * Загрузить или создать TagTaskCount (если тег новый и еще не посчитан)
     */
    private TagCount loadOrFailTagTaskCount(@NonNull final String tagName,
                                            @NonNull final String courseId) {
        return tagCountRepository.findByCourseOverview_CourseIdAndTagName(courseId, tagName)
                .orElseThrow(() -> new IllegalStateException(
                        "TagCount not found for tag=" + tagName + ", courseId=" + courseId
                ));
    }

    /**
     * Инициализировать все TagTopicProgress для всех топиков, где встречается тег
     */
    private void initializeAllTopicProgresses(@NonNull final TagProgress tagProgress,
                                              @NonNull final TagCount expectedTaskCount,
                                              @NonNull final String activeTopicId,
                                              final Boolean isCorrect,
                                              final Boolean isTaskCompletion) {
        if (expectedTaskCount.getCount() == 0) {
            log.warn("No topic counts found for tag: {}. Skipping topic initialization.",
                    tagProgress.getTagName());
            return;
        }

        Set<String> existingTopicIds = tagProgress.getTopicProgresses().stream()
                .map(TagTopicProgress::getTopicId)
                .collect(Collectors.toSet());

        for (TagTopicCount topicCount : expectedTaskCount.getTopicCounts()) {
            if (existingTopicIds.contains(topicCount.getTopicId())) {
                continue;
            }

            TagTopicProgress tagTopicProgress = createTagTopicProgress(tagProgress, topicCount);

            if (activeTopicId.equals(topicCount.getTopicId())) {
                applyActivityToTopicProgress(tagTopicProgress, tagProgress, isTaskCompletion);
            }

            tagProgress.getTopicProgresses().add(tagTopicProgress);
        }
    }

    /**
     * Создать TagTopicProgress для конкретного топика
     */
    private TagTopicProgress createTagTopicProgress(@NonNull final TagProgress tagProgress,
                                                    @NonNull final TagTopicCount topicCount) {
        return TagTopicProgress.builder()
                .topicId(topicCount.getTopicId())
                .expectedTasks(topicCount.getCount())
                .tagProgress(tagProgress)
                .build();
    }

    /**
     * Применить активность (тест/таска) к TagTopicProgress
     */
    private void applyActivityToTopicProgress(@NonNull final TagTopicProgress tagTopicProgress,
                                              @NonNull final TagProgress tagProgress,
//                                              final Boolean isCorrect,
                                              final Boolean isTaskCompletion) {
        // Обработка теста
//        if (isCorrect != null) {
//            updateTestMetrics(tagTopicProgress, tagProgress, isCorrect);
//        }

        // Обработка таски
        if (isTaskCompletion != null && isTaskCompletion) {
            updateTaskMetrics(tagTopicProgress, tagProgress);
        }
    }

    /**
     * Найти expectedTasks для конкретного топика
     */
    private Integer findExpectedTasksForTopic(@NonNull final String courseId,
                                              @NonNull final String tagName,
                                              @NonNull final String topicId) {
        return tagCountRepository.findByCourseOverview_CourseIdAndTagName(courseId, tagName)
                .flatMap(ttc -> ttc.getTopicCounts().stream()
                        .filter(tc -> tc.getTopicId().equals(topicId))
                        .findFirst())
                .map(TagTopicCount::getCount)
                .orElse(0);
    }

    /**
     * Проверить, является ли ответ полностью правильным
     */
    private Boolean isFullyCorrectAnswer(final Double score) {
        return score != null && score.equals(MAX_TEST_ITEM_SCORE);
    }

    /**
     * Проверить, пуст ли список тегов
     */
    private boolean isEmptyTagList(final List<String> tagNames) {
        return tagNames == null || tagNames.isEmpty();
    }
}