package com.example.cs_progress.service.impl;

import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.PromptData;
import com.example.cs_progress.model.SkillSummary;
import com.example.cs_progress.model.TopicSummary;
import com.example.cs_progress.service.PromptGeneratorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PromptGeneratorServiceImpl extends BaseService implements PromptGeneratorService {

    /**
     * Генерация промпта для ИИ-анализа
     */
    public String generatePrompt(PromptData data) {
        log.debug("Generating AI prompt for user: {}", data.getUserId());

        StringBuilder prompt = new StringBuilder();

        // Заголовок
        prompt.append("Проанализируй прогресс пользователя и дай персонализированные рекомендации.\n\n");

        // Секция 1: Лучшие топики по задачам
        appendTopicSection(
                prompt,
                "ТОПИКИ С ЛУЧШИМИ РЕЗУЛЬТАТАМИ ПО ЗАДАЧАМ:",
                data.getBestTopicsByTasks()
        );

        // Секция 2: Худшие топики по задачам
        appendTopicSection(
                prompt,
                "ТОПИКИ ТРЕБУЮЩИЕ ПРАКТИКИ (низкий % задач):",
                data.getWorstTopicsByTasks()
        );

        // Секция 3: Лучшие топики по тестам
        appendTopicSection(
                prompt,
                "ТОПИКИ С ЛУЧШИМИ РЕЗУЛЬТАТАМИ ПО ТЕСТАМ:",
                data.getBestTopicsByTests()
        );

        // Секция 4: Худшие топики по тестам
        appendTopicSection(
                prompt,
                "ТОПИКИ ТРЕБУЮЩИЕ ПОВТОРЕНИЯ ТЕОРИИ (низкий % теста):",
                data.getWorstTopicsByTests()
        );

        // Секция 5: Лучшие навыки
        appendSkillSection(
                prompt,
                "ЛУЧШИЕ НАВЫКИ (в контексте топиков):",
                data.getBestSkills()
        );

        // Секция 6: Слабые навыки
        appendSkillSection(
                prompt,
                "СЛАБЫЕ НАВЫКИ (в контексте топиков):",
                data.getWeakSkills()
        );

        // Секция 7: Активность
        appendActivitySection(prompt, data);

        // Секция 8: Задание для ИИ
        appendTaskSection(prompt);

        log.debug("Prompt generated, length: {} characters", prompt.length());

        return prompt.toString();
    }

    /**
     * Добавить секцию с топиками
     */
    private void appendTopicSection(StringBuilder prompt, String title, List<TopicSummary> topics) {
        prompt.append(title).append("\n");

        if (topics == null || topics.isEmpty()) {
            prompt.append("(нет данных)\n\n");
            return;
        }

        for (int i = 0; i < topics.size(); i++) {
            TopicSummary topic = topics.get(i);
            prompt.append(String.format("%d. \"%s\" (%s): ",
                    i + 1,
                    topic.getTopicTitle(),
                    topic.getCourseTitle()
            ));

            // Добавляем метрики в зависимости от типа секции
            if (title.contains("ЗАДАЧАМ")) {
                prompt.append(String.format("%d%% задач (%d/%d)",
                        topic.getTaskCompletionPercentage().intValue(),
                        topic.getCompletedTasks(),
                        topic.getTotalTasks()
                ));
            } else if (title.contains("ТЕСТАМ")) {
                prompt.append(String.format("%d%% тест",
                        topic.getBestTestScorePercentage().intValue()
                ));
            }

            prompt.append(String.format(", последняя активность: %s\n",
                    formatLastActivity(topic.getLastActivity())
            ));
        }

        prompt.append("\n");
    }

    /**
     * Добавить секцию с навыками
     */
    private void appendSkillSection(StringBuilder prompt, String title, List<SkillSummary> skills) {
        prompt.append(title).append("\n");

        if (skills == null || skills.isEmpty()) {
            prompt.append("(нет данных)\n\n");
            return;
        }

        for (int i = 0; i < skills.size(); i++) {
            SkillSummary skill = skills.get(i);
            prompt.append(String.format("%d. %s (в топике \"%s\"): %d%% прогресс, последняя активность: %s\n",
                    i + 1,
                    skill.getSkillName(),
                    skill.getTopicTitle(),
                    skill.getProgressInTopic().intValue(),
                    formatLastActivity(skill.getTopicLastActivity())
            ));
        }

        prompt.append("\n");
    }

    /**
     * Добавить секцию активности
     */
    private void appendActivitySection(StringBuilder prompt, PromptData data) {
        prompt.append("АКТИВНОСТЬ:\n");
        prompt.append(String.format("Дней без активности: %d\n", data.getDaysSinceLastActivity()));

        if (data.getCurrentStreak() != null && data.getCurrentStreak() > 0) {
            prompt.append(String.format("Текущий streak: %d дней подряд\n", data.getCurrentStreak()));
        }

        prompt.append("\n");
    }

    /**
     * Добавить секцию с заданием для ИИ
     */
    private void appendTaskSection(StringBuilder prompt) {
        prompt.append("ЗАДАНИЕ:\n");
        prompt.append("1. Похвали пользователя за топики с хорошими результатами по задачам и тестам.\n");
        prompt.append("2. Дай 3-5 конкретных рекомендаций:\n");
        prompt.append("   - Какие топики требуют дополнительной практики задач\n");
        prompt.append("   - Какие топики требуют повторения теории (низкий балл теста)\n");
        prompt.append("   - Какие слабые навыки нужно практиковать\n");
        prompt.append("3. Мотивируй в зависимости от активности:\n");
        prompt.append("   - Если 0-2 дня без активности: похвали за регулярность\n");
        prompt.append("   - Если 3-7 дней: мягко напомни о продолжении\n");
        prompt.append("   - Если 7+ дней: мотивируй вернуться к обучению\n\n");
        prompt.append("Тон: дружелюбный, поддерживающий, конкретный.\n");
        prompt.append("Формат ответа: JSON\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"краткий обзор 2-3 предложения\",\n");
        prompt.append("  \"strengths\": [\"сильная сторона 1\", \"сильная сторона 2\"],\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"заголовок рекомендации\",\n");
        prompt.append("      \"description\": \"описание\",\n");
        prompt.append("      \"priority\": \"high|medium|low\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
    }

    /**
     * Форматирование времени последней активности
     */
    private String formatLastActivity(LocalDateTime lastActivity) {
        if (lastActivity == null) {
            return "давно";
        }

        long days = ChronoUnit.DAYS.between(lastActivity, LocalDateTime.now());

        if (days == 0) return "сегодня";
        if (days == 1) return "1 день назад";
        if (days < 7) return days + " дней назад";
        if (days < 14) return "1 неделя назад";
        if (days < 30) return (days / 7) + " недель назад";

        return "более месяца назад";
    }
}
