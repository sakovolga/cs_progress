package com.example.cs_progress.init;

import com.example.cs_common.enums.AchievementCategory;
import com.example.cs_common.enums.AchievementTier;
import com.example.cs_common.util.BaseInitializer;
import com.example.cs_progress.model.entity.Achievement;
import com.example.cs_progress.repository.AchievementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AchievementInitializer extends BaseInitializer implements ApplicationRunner {

    private final AchievementRepository achievementRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Checking achievements initialization...");

        List<Achievement> allAchievements = createAchievements();
        int expectedCount = allAchievements.size();

        long currentCount = achievementRepository.count();

        if (currentCount >= expectedCount) {
            log.info("All {} achievements already exist, skipping initialization", currentCount);
            return;
        }

        log.info("Found {} achievements, expected {}. Adding missing achievements...",
                currentCount, expectedCount);

        List<Achievement> existingAchievements = achievementRepository.findAll();
        Set<String> existingKeys = existingAchievements
                .stream()
                .map(Achievement::getKey)
                .collect(Collectors.toSet());

        List<Achievement> newAchievements = allAchievements.stream()
                .filter(achievement -> !existingKeys.contains(achievement.getKey()))
                .toList();

        if (!newAchievements.isEmpty()) {
            achievementRepository.saveAll(newAchievements);
            log.info("Successfully added {} new achievements", newAchievements.size());

            newAchievements.forEach(a ->
                    log.debug("Added achievement: {} - {}", a.getKey(), a.getTitle())
            );
        } else {
            log.info("No new achievements to add");
        }

        long finalCount = achievementRepository.count();
        log.info("Total achievements in database: {}", finalCount);
    }

    private List<Achievement> createAchievements() {
        return List.of(
                // LEARNING_PROGRESS
                createAchievement(
                        "first_lesson",
                        "First Steps",
                        "Completed your first lesson",
                        AchievementCategory.LEARNING_PROGRESS,
                        AchievementTier.BRONZE,
                        "🎓"
                ),

                createAchievement(
                        "lessons_5",
                        "Student",
                        "Completed 5 lessons",
                        AchievementCategory.LEARNING_PROGRESS,
                        AchievementTier.BRONZE,
                        "📚"
                ),

                createAchievement(
                        "lessons_10",
                        "Dedicated Learner",
                        "Completed 10 lessons",
                        AchievementCategory.LEARNING_PROGRESS,
                        AchievementTier.SILVER,
                        "📖"
                ),

                createAchievement(
                        "lessons_25",
                        "Knowledge Seeker",
                        "Completed 25 lessons",
                        AchievementCategory.LEARNING_PROGRESS,
                        AchievementTier.GOLD,
                        "🌟"
                ),

                createAchievement(
                        "course_complete",
                        "Course Master",
                        "Completed entire course with 100%",
                        AchievementCategory.LEARNING_PROGRESS,
                        AchievementTier.GOLD,
                        "🏆"
                ),

                // TASKS
                createAchievement(
                        "first_task",
                        "Hello World",
                        "Solved your first coding task",
                        AchievementCategory.TASKS,
                        AchievementTier.BRONZE,
                        "💻"
                ),

                createAchievement(
                        "tasks_10",
                        "Problem Solver",
                        "Solved 10 coding tasks",
                        AchievementCategory.TASKS,
                        AchievementTier.SILVER,
                        "⚡"
                ),

                createAchievement(
                        "tasks_25",
                        "Code Warrior",
                        "Solved 25 coding tasks",
                        AchievementCategory.TASKS,
                        AchievementTier.SILVER,
                        "🛡️"
                ),

                createAchievement(
                        "tasks_50",
                        "Coding Expert",
                        "Solved 50 coding tasks",
                        AchievementCategory.TASKS,
                        AchievementTier.GOLD,
                        "🎯"
                ),

                createAchievement(
                        "tasks_100",
                        "Coding Genius",
                        "Solved 100 coding tasks",
                        AchievementCategory.TASKS,
                        AchievementTier.PLATINUM,
                        "💎"
                ),

                // CODE_QUALITY
                createAchievement(
                        "first_excellent",
                        "Clean Code",
                        "Received first EXCELLENT code quality rating",
                        AchievementCategory.CODE_QUALITY,
                        AchievementTier.BRONZE,
                        "✨"
                ),

                createAchievement(
                        "excellent_10",
                        "Code Artist",
                        "Received 10 EXCELLENT code quality ratings",
                        AchievementCategory.CODE_QUALITY,
                        AchievementTier.GOLD,
                        "🎨"
                ),

                // TESTS
                createAchievement(
                        "first_test",
                        "Test Taker",
                        "Passed your first test",
                        AchievementCategory.TESTS,
                        AchievementTier.BRONZE,
                        "🧪"
                ),

                createAchievement(
                        "perfect_score",
                        "Perfect Score",
                        "Passed a test with 100% score",
                        AchievementCategory.TESTS,
                        AchievementTier.SILVER,
                        "💯"
                ),

                // SKILLS
                createAchievement(
                        "skill_mastery",
                        "Skill Master",
                        "Mastered your first skill (completed all tasks with a tag)",
                        AchievementCategory.SKILLS,
                        AchievementTier.GOLD,
                        "🏅"
                )
        );
    }

    private Achievement createAchievement(
            String key,
            String title,
            String description,
            AchievementCategory category,
            AchievementTier tier,
            String icon
    ) {
        return Achievement.builder()
                .key(key)
                .title(title)
                .description(description)
                .category(category)
                .tier(tier)
                .iconName(icon)
//                .requiredCount(requiredCount)
//                .displayOrder(displayOrder)
//                .isActive(true)
                .build();
    }
}
