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
     * Generate AI analysis prompt
     */
    public String generatePrompt(PromptData data) {
        log.debug("Generating AI prompt for user: {}", data.getUserId());

        StringBuilder prompt = new StringBuilder();

        // Header
        prompt.append("Analyze the user's progress and provide personalized recommendations.\n\n");

        // Section 1: Best topics by tasks
        appendTopicSection(
                prompt,
                "BEST PERFORMING TOPICS BY TASKS:",
                data.getBestTopicsByTasks()
        );

        // Section 2: Worst topics by tasks
        appendTopicSection(
                prompt,
                "TOPICS REQUIRING PRACTICE (low task completion %):",
                data.getWorstTopicsByTasks()
        );

        // Section 3: Best topics by tests
        appendTopicSection(
                prompt,
                "BEST PERFORMING TOPICS BY TESTS:",
                data.getBestTopicsByTests()
        );

        // Section 4: Worst topics by tests
        appendTopicSection(
                prompt,
                "TOPICS REQUIRING THEORY REVIEW (low test score %):",
                data.getWorstTopicsByTests()
        );

        // Section 5: Best skills
        appendSkillSection(
                prompt,
                "STRONGEST SKILLS (in context of topics):",
                data.getBestSkills()
        );

        // Section 6: Weak skills
        appendSkillSection(
                prompt,
                "WEAK SKILLS (in context of topics):",
                data.getWeakSkills()
        );

        // Section 7: Activity
        appendActivitySection(prompt, data);

        // Section 8: AI task
        appendTaskSection(prompt);

        log.debug("Prompt generated, length: {} characters", prompt.length());

        return prompt.toString();
    }

    /**
     * Append topic section
     */
    private void appendTopicSection(StringBuilder prompt, String title, List<TopicSummary> topics) {
        prompt.append(title).append("\n");

        if (topics == null || topics.isEmpty()) {
            prompt.append("(no data)\n\n");
            return;
        }

        for (int i = 0; i < topics.size(); i++) {
            TopicSummary topic = topics.get(i);
            prompt.append(String.format("%d. \"%s\" (%s): ",
                    i + 1,
                    topic.getTopicTitle(),
                    topic.getCourseTitle()
            ));

            // Add metrics depending on section type
            if (title.contains("TASKS")) {
                prompt.append(String.format("%d%% tasks completed (%d/%d)",
                        topic.getTaskCompletionPercentage().intValue(),
                        topic.getCompletedTasks(),
                        topic.getTotalTasks()
                ));
            } else if (title.contains("TESTS")) {
                prompt.append(String.format("%d%% test score",
                        topic.getBestTestScorePercentage().intValue()
                ));
            }

            prompt.append(String.format(", last activity: %s\n",
                    formatLastActivity(topic.getLastActivity())
            ));
        }

        prompt.append("\n");
    }

    /**
     * Append skill section
     */
    private void appendSkillSection(StringBuilder prompt, String title, List<SkillSummary> skills) {
        prompt.append(title).append("\n");

        if (skills == null || skills.isEmpty()) {
            prompt.append("(no data)\n\n");
            return;
        }

        for (int i = 0; i < skills.size(); i++) {
            SkillSummary skill = skills.get(i);
            prompt.append(String.format("%d. %s (in topic \"%s\"): %d%% progress, last activity: %s\n",
                    i + 1,
                    skill.getSkillName(),
                    skill.getTopicTitle(),
                    skill.getTaskCompletionRate().intValue(),
                    formatLastActivity(skill.getTopicLastActivity())
            ));
        }

        prompt.append("\n");
    }

    /**
     * Append activity section
     */
    private void appendActivitySection(StringBuilder prompt, PromptData data) {
        prompt.append("ACTIVITY:\n");
        prompt.append(String.format("Days since last activity: %d\n", data.getDaysSinceLastActivity()));

        if (data.getCurrentStreak() != null && data.getCurrentStreak() > 0) {
            prompt.append(String.format("Current streak: %d days in a row\n", data.getCurrentStreak()));
        }

        prompt.append("\n");
    }

    /**
     * Append AI task section
     */
    private void appendTaskSection(StringBuilder prompt) {
        prompt.append("TASK:\n");
        prompt.append("1. Praise the user for topics with good task and test results.\n");
        prompt.append("2. Provide 3-5 specific recommendations:\n");
        prompt.append("   - Which topics require additional task practice\n");
        prompt.append("   - Which topics require theory review (low test score)\n");
        prompt.append("   - Which weak skills need to be practiced\n");
        prompt.append("3. Motivate based on activity:\n");
        prompt.append("   - If 0-2 days without activity: praise for consistency\n");
        prompt.append("   - If 3-7 days: gently remind to continue\n");
        prompt.append("   - If 7+ days: motivate to return to learning\n\n");
        prompt.append("Tone: friendly, supportive, specific.\n");
        prompt.append("Response format: JSON\n");
        prompt.append("{\n");
        prompt.append("  \"summary\": \"brief overview in 2-3 sentences\",\n");
        prompt.append("  \"strengths\": [\"strength 1\", \"strength 2\"],\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"title\": \"recommendation title\",\n");
        prompt.append("      \"description\": \"description\",\n");
        prompt.append("      \"priority\": \"high|medium|low\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
    }

    /**
     * Format last activity time
     */
    private String formatLastActivity(LocalDateTime lastActivity) {
        if (lastActivity == null) {
            return "long ago";
        }

        long days = ChronoUnit.DAYS.between(lastActivity, LocalDateTime.now());

        if (days == 0) return "today";
        if (days == 1) return "1 day ago";
        if (days < 7) return days + " days ago";
        if (days < 14) return "1 week ago";
        if (days < 30) return (days / 7) + " weeks ago";

        return "over a month ago";
    }
}