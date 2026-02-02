package com.example.cs_progress.service.impl;

import com.example.cs_common.enums.TopicStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.PromptData;
import com.example.cs_progress.model.SkillSummary;
import com.example.cs_progress.model.TopicSummary;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TagTopicProgress;
import com.example.cs_progress.model.entity.TopicOverview;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TagTopicProgressRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.CourseOverviewCacheService;
import com.example.cs_progress.service.PromptDataCollectorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class PromptDataCollectorServiceImpl extends BaseService implements PromptDataCollectorService {

    private final TopicProgressRepository topicProgressRepository;
    private final TagProgressRepository tagProgressRepository;
    private final TagTopicProgressRepository tagTopicProgressRepository;
    private final CourseOverviewCacheService courseOverviewCacheService;

    /**
     * Собрать все данные для формирования промпта
     */
    public PromptData collectData(String userId) {
        log.info("Collecting prompt data for user: {}", userId);

        // 1. Получаем все топики пользователя (с прогрессом)
        List<TopicProgress> allTopics = topicProgressRepository.findByUserId(userId)
                .stream()
                .filter(this::hasProgress)
                .toList();

        // 2. Отбираем топ-3 по задачам
        List<TopicSummary> bestByTasks = getBestTopicsByTasks(allTopics);
        List<TopicSummary> worstByTasks = getWorstTopicsByTasks(allTopics);

        // 3. Отбираем топ-3 по тестам
        List<TopicSummary> bestByTests = getBestTopicsByTests(allTopics);
        List<TopicSummary> worstByTests = getWorstTopicsByTests(allTopics);

        // 4. Получаем навыки
        List<TagTopicProgress> allTagTopicProgresses = tagTopicProgressRepository
                .findByTagProgress_UserId(userId);
        List<SkillSummary> bestSkills = getBestSkills(allTagTopicProgresses);
        List<SkillSummary> weakSkills = getWeakSkills(allTagTopicProgresses);

        // 5. Вычисляем активность
        Integer daysSinceLastActivity = calculateDaysSinceLastActivity(allTopics);
        Integer currentStreak = calculateStreak(userId);

        PromptData promptData = PromptData.builder()
                .userId(userId)
                .bestTopicsByTasks(bestByTasks)
                .worstTopicsByTasks(worstByTasks)
                .bestTopicsByTests(bestByTests)
                .worstTopicsByTests(worstByTests)
                .bestSkills(bestSkills)
                .weakSkills(weakSkills)
                .daysSinceLastActivity(daysSinceLastActivity)
                .currentStreak(currentStreak)
                .build();

        log.info("Collected prompt data for user: {}: {}", userId, promptData);
        return promptData;
    }

    /**
     * Проверка что топик имеет прогресс
     */
    private boolean hasProgress(TopicProgress topic) {
        return topic.getStatus() == TopicStatus.IN_PROGRESS ||
                topic.getStatus() == TopicStatus.COMPLETED;
    }

    /**
     * Топ-3 топика с лучшими результатами по задачам
     * Сортировка: taskCompletionPercentage DESC, lastActivity DESC
     */
    private List<TopicSummary> getBestTopicsByTasks(List<TopicProgress> topics) {
        return topics.stream()
                .filter(t -> t.getTaskCompletionPercentage() != null)
                .sorted(Comparator
                        .comparing(TopicProgress::getTaskCompletionPercentage).reversed()
                        .thenComparing(TopicProgress::getLastActivity,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toTopicSummary)
                .toList();
    }

    /**
     * Топ-3 топика с худшими результатами по задачам
     * Сортировка: taskCompletionPercentage ASC, lastActivity DESC
     * Фильтр: прогресс >= 30% (чтобы не брать только начатые)
     */
    private List<TopicSummary> getWorstTopicsByTasks(List<TopicProgress> topics) {
        return topics.stream()
                .filter(t -> t.getTaskCompletionPercentage() != null)
                .filter(t -> t.getTaskCompletionPercentage() >= 30.0) // минимальный порог
                .sorted(Comparator
                        .comparing(TopicProgress::getTaskCompletionPercentage)
                        .thenComparing(TopicProgress::getLastActivity,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toTopicSummary)
                .toList();
    }

    /**
     * Топ-3 топика с лучшими результатами по тестам
     * Сортировка: bestTestScorePercentage DESC, lastActivity DESC
     */
    private List<TopicSummary> getBestTopicsByTests(List<TopicProgress> topics) {
        return topics.stream()
                .filter(t -> t.getBestTestScorePercentage() != null)
                .filter(t -> t.getBestTestScorePercentage() > 0)
                .sorted(Comparator
                        .comparing(TopicProgress::getBestTestScorePercentage).reversed()
                        .thenComparing(TopicProgress::getLastActivity,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toTopicSummary)
                .toList();
    }

    /**
     * Топ-3 топика с худшими результатами по тестам
     * Сортировка: bestTestScorePercentage ASC, lastActivity DESC
     * Фильтр: тест пройден хотя бы раз
     */
    private List<TopicSummary> getWorstTopicsByTests(List<TopicProgress> topics) {
        return topics.stream()
                .filter(t -> t.getBestTestScorePercentage() != null)
                .filter(t -> t.getBestTestScorePercentage() > 0)
                .filter(t -> t.getBestTestScorePercentage() < 70.0) // только те, что ниже порога
                .sorted(Comparator
                        .comparing(TopicProgress::getBestTestScorePercentage)
                        .thenComparing(TopicProgress::getLastActivity,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toTopicSummary)
                .toList();
    }

    /**
     * Топ-3 лучших навыка в контексте топиков
     * Сортировка: progressInTopic DESC, topicLastActivity DESC
     */
    private List<SkillSummary> getBestSkills(List<TagTopicProgress> allTagTopicProgresses) {

        return allTagTopicProgresses.stream()
                .filter(ttp -> ttp.getProgressInTopic() != null)
                .filter(TagTopicProgress::hasActivity)
                .sorted(Comparator
                        .comparing(TagTopicProgress::getProgressInTopic).reversed()
                        .thenComparing(TagTopicProgress::getUpdatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toSkillSummary)
                .toList();
    }

    /**
     * Топ-3 слабых навыка в контексте топиков
     * Сортировка: progressInTopic ASC, topicLastActivity DESC
     */
    private List<SkillSummary> getWeakSkills(List<TagTopicProgress> allTagTopicProgresses) {

        return allTagTopicProgresses.stream()
                .filter(ttp -> ttp.getProgressInTopic() != null)
                .filter(TagTopicProgress::hasActivity)
                .filter(ttp -> ttp.getProgressInTopic() < 60.0) // только слабые
                .sorted(Comparator
                        .comparing(TagTopicProgress::getProgressInTopic)
                        .thenComparing(TagTopicProgress::getUpdatedAt,
                                Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(this::toSkillSummary)
                .toList();
    }

    /**
     * Вычислить дни с последней активности
     */
    private Integer calculateDaysSinceLastActivity(List<TopicProgress> topics) {
        LocalDateTime lastActivity = topics.stream()
                .map(TopicProgress::getLastActivity)
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        if (lastActivity == null) {
            return 999; // нет активности вообще
        }

        return (int) ChronoUnit.DAYS.between(lastActivity, LocalDateTime.now());
    }

    /**
     * Вычислить текущий streak
     */
    private Integer calculateStreak(String userId) {
        // Получаем уникальные даты активности
        Set<LocalDate> activityDates = topicProgressRepository
                .findByUserId(userId)
                .stream()
                .map(TopicProgress::getUpdatedAt)
                .filter(Objects::nonNull)
                .map(LocalDateTime::toLocalDate)
                .collect(Collectors.toSet());

        if (activityDates.isEmpty()) return 0;

        // Считаем streak с сегодня назад
        LocalDate today = LocalDate.now();
        int streak = 0;

        for (int i = 0; i < 365; i++) {
            LocalDate checkDate = today.minusDays(i);
            if (activityDates.contains(checkDate)) {
                streak++;
            } else if (i > 0) {
                // Пропуск = конец streak
                break;
            }
        }

        return streak;
    }

    /**
     * Преобразование TopicProgress в TopicSummary
     */
    private TopicSummary toTopicSummary(TopicProgress topic) {
        CourseOverview courseOverview = courseOverviewCacheService.findByCourseId(topic.getCourseId())
                .orElseThrow(() -> new NotFoundException(
                        "CourseOverview not found for courseId: " + topic.getCourseId() + ", synchronization needed",
                        ENTITY_NOT_FOUND_ERROR));

        return TopicSummary.builder()
                .topicId(topic.getTopicId())
                .topicTitle(getTopicTitle(courseOverview, topic.getTopicId()))
                .courseId(topic.getCourseId())
                .courseTitle(courseOverview.getCourseName())
                .completedTasks(topic.getCompletedTasks())
                .totalTasks(topic.getTotalTasks())
                .taskCompletionPercentage(topic.getTaskCompletionPercentage())
                .bestTestScorePercentage(topic.getBestTestScorePercentage())
                .lastActivity(topic.getLastActivity())
                .build();
    }

    private String getTopicTitle(CourseOverview courseOverview, String topicId) {
        return courseOverview.getTopicOverviews().stream()
                .filter(topicOverview -> topicOverview.getTopicId().equals(topicId))
                .map(TopicOverview::getTopicName)
                .findFirst()
                .orElseThrow(() -> new NotFoundException(
                        "TopicOverview with topicId: " + topicId + " not found for courseId: "
                                + courseOverview.getCourseId() + ", synchronization needed",
                        ENTITY_NOT_FOUND_ERROR));
    }

    /**
     * Преобразование TagTopicProgress в SkillSummary
     */
    private SkillSummary toSkillSummary(TagTopicProgress ttp) {
        CourseOverview courseOverview = courseOverviewCacheService.findByCourseId(ttp.getTagProgress().getCourseId())
                .orElseThrow(() -> new NotFoundException(
                        "CourseOverview not found for courseId: " + ttp.getTagProgress().getCourseId() + ", " +
                                "synchronization needed", ENTITY_NOT_FOUND_ERROR));
        return SkillSummary.builder()
                .skillName(ttp.getTagProgress().getTagName())
                .topicId(ttp.getTopicId())
                .topicTitle(getTopicTitle(courseOverview, ttp.getTopicId()))
                .progressInTopic(ttp.getProgressInTopic())
                .topicLastActivity(ttp.getUpdatedAt())
                .build();
    }
}
