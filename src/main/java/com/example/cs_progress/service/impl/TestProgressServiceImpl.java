package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_common.dto.request.TestItemUserResolvedRq;
import com.example.cs_common.dto.response.CurrentTestInfoRs;
import com.example.cs_common.dto.response.TestResultRs;
import com.example.cs_common.enums.TestStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.mapper.TestItemResultMapper;
import com.example.cs_progress.mapper.TestProgressMapper;
import com.example.cs_progress.model.entity.TestItemResult;
import com.example.cs_progress.model.entity.TestProgress;
import com.example.cs_progress.model.entity.TestsResult;
import com.example.cs_progress.model.entity.TopicProgress;
import com.example.cs_progress.repository.TestsResultRepository;
import com.example.cs_progress.repository.TopicProgressRepository;
import com.example.cs_progress.service.CacheEvictionService;
import com.example.cs_progress.service.TestProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TestProgressServiceImpl extends BaseService implements TestProgressService {

    private final TestsResultRepository testsResultRepository;
    private final TestItemResultMapper testItemResultMapper;
    private final TestProgressMapper testProgressMapper;
    private final TopicProgressRepository topicProgressRepository;
    private final CacheEvictionService cacheEvictionService;

    private static final int MAX_NUMBER_OF_TEST = 3;
    private static final int MAX_TEST_ITEM_INDEX = 9;
    private static final int MAX_NUMBER_OF_TEST_ITEMS = 10;

    @Override
    @Transactional
    public CurrentTestInfoRs getCurrentTestInfo(@NonNull final String userId,
                                                @NonNull final String courseId, //не используется, но оставлен для возможного будущего использования
                                                @NonNull final String topicId) {
        log.info(
                "Attempting to get next step in test for user with id: {} in topic with id: {} in course with id: {}",
                userId, topicId, courseId
        );

        Optional<TestsResult> optionalTestsResult = testsResultRepository.findByUserIdAndTopicId(userId, topicId);
        if (optionalTestsResult.isEmpty()) {
            log.info("There is no started tests for user with id: {} in topic with id: {}", userId, topicId);

            return CurrentTestInfoRs.builder()
                    .isAllAttemptsUsed(false)
                    .isTestCompleted(false)
                    .build();
        }

        List<TestProgress> testProgressList = optionalTestsResult.get().getTestProgresses();

        if (testProgressList.size() == MAX_NUMBER_OF_TEST && allAttemptsUsed(testProgressList)) {
            log.info("All attempts used for user with id: {} in topic with id: {}, resetting for new cycle", userId, topicId);

            TestsResult testsResult = optionalTestsResult.get();
            testsResult.getTestProgresses().clear();
            testsResultRepository.save(testsResult);

            return CurrentTestInfoRs.builder()
                    .isAllAttemptsUsed(false)
                    .isTestCompleted(false)
                    .build();
        }

        Optional<TestProgress> activeProgressOpt = testProgressList.stream()
                .filter(tp -> tp.getStatus() != TestStatus.COMPLETED)
                .findFirst();

        if (activeProgressOpt.isPresent()) {
            TestProgress activeProgress = activeProgressOpt.get();
            int currentIndex = activeProgress.getTestItemResults().size() - 1;

            if (currentIndex >= MAX_TEST_ITEM_INDEX) {
                log.info("Test at/beyond max index for user id={} in topic id={}, treating as completed", userId, topicId);

                activeProgress.updateScore();
                activeProgress.setStatus(TestStatus.COMPLETED);
                testsResultRepository.save(optionalTestsResult.get());

                if (testProgressList.size() >= MAX_NUMBER_OF_TEST && allAttemptsUsed(testProgressList)) {
                    TestsResult testsResult = optionalTestsResult.get();
                    testsResult.getTestProgresses().clear();
                    testsResultRepository.save(testsResult);
                    return CurrentTestInfoRs.builder()
                            .isAllAttemptsUsed(false)
                            .isTestCompleted(false)
                            .build();
                }

                return CurrentTestInfoRs.builder()
                        .isTestCompleted(true)
                        .testScore(activeProgress.getScore())
                        .isAllAttemptsUsed(false)
                        .bestScore(optionalTestsResult.get().getBestScore())
                        .completedTestIds(getCompletedTestIds(testProgressList))
                        .build();
            }

            log.info(
                    "Returning current test item index: {} in test with id: {} in attempting: {} " +
                            "in topic with id: {} in course with id: {} for user with id: {} ",
                    currentIndex, activeProgress.getTestId(), testProgressList.size() - 1, topicId, courseId, userId
            );
            return CurrentTestInfoRs.builder()
                    .currentTestId(activeProgress.getTestId())
                    .currentTestItemIndex(currentIndex)
                    .build();
        }

        TestProgress lastCompleted = testProgressList.getLast();
        log.info("No active test progress for user id={} in topic id={}, returning completed state", userId, topicId);
        return CurrentTestInfoRs.builder()
                .isTestCompleted(true)
                .testScore(lastCompleted.getScore())
                .isAllAttemptsUsed(false)
                .bestScore(optionalTestsResult.get().getBestScore())
                .completedTestIds(getCompletedTestIds(testProgressList))
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "topic-progress", key = "#rq.userId")
    public TestResultRs finishTest(@NonNull final TestItemUserResolvedRq rq) {
        log.info("Finishing test for userId={}, testId={}",
                rq.getUserId(), rq.getTestItemResolvedRq().getTestId());

        TestsResult testsResult = testsResultRepository
                .findByUserIdAndTopicId(rq.getUserId(), rq.getTestItemResolvedRq().getTopicId())
                .orElseThrow(() -> new NotFoundException(
                        "TestsResult for userId: " + rq.getUserId() + " and topicId: "
                                + rq.getTestItemResolvedRq().getTopicId() + " not found",
                        ENTITY_NOT_FOUND_ERROR)
                );

        TestProgress testProgress = testsResult.getTestProgresses().stream()
                .filter(tp -> tp.getTestId().equals(rq.getTestItemResolvedRq().getTestId()))
                .findFirst()
                .orElse(null);

        TestItemResult testItemResult = testItemResultMapper.toTestItemResult(rq, testProgress);
        if (testProgress != null) {
            testProgress.getTestItemResults().add(testItemResult);
            testProgress.updateScore();
            testProgress.setStatus(TestStatus.COMPLETED);
        }

        Double bestScore = calculateBestScore(testsResult);
        testsResult.setBestScore(bestScore);
        saveBestScoreToTopicProgress(
                rq.getUserId(),
                rq.getTestItemResolvedRq().getCourseId(),
                rq.getTestItemResolvedRq().getTopicId(),
                bestScore);

        cacheEvictionService.evictAIInsights(rq.getUserId());
        cacheEvictionService.evictTopicProgress(rq.getUserId());

        TestResultRs rs = testProgressMapper.toTestResultRs(testProgress);

        log.info("Test finished: testId={}, score={}", rs.getTestId(), rs.getScore());
        return rs;
    }

    private Double calculateBestScore(TestsResult testsResult) {
        return testsResult.getTestProgresses().stream()
                .mapToDouble(TestProgress::getScore)
                .max()
                .orElse(0.0);
    }

    @Override
    @Transactional
    public void processResolvedTestItem(@NonNull final TestItemResolvedEvent event) {
        log.info("Processing TestItemResolvedEvent: {}", event);

        // Попытка найти существующий TestsResult
        Optional<TestsResult> optionalTestsResult = testsResultRepository
                .findByUserIdAndTopicId(event.getUserId(), event.getTopicId());

        final TestsResult testsResult;
        if (optionalTestsResult.isEmpty()) {
            testsResult = TestsResult.builder()
                    .userId(event.getUserId())
                    .topicId(event.getTopicId())
                    .courseId(event.getCourseId())
                    .build();
            testsResultRepository.save(testsResult);
            log.info("No existing TestsResult found, creating new TestsResult for userId={} and topicId={}",
                    event.getUserId(), event.getTopicId());

        } else {
            testsResult = optionalTestsResult.get();
            log.info("Found existing TestsResult with id={} for userId={} and topicId={}",
                    testsResult.getId(), event.getUserId(), event.getTopicId());
        }

        TestItemResult testItemResult = TestItemResult.builder()
                .testItemId(event.getTestItemId())
                .score(event.getTestItemScore())
                .build();

        // Если все попытки завершены — сбросить цикл (bestScore в TestsResult сохраняется)
        boolean allCompleted = !testsResult.getTestProgresses().isEmpty()
                && testsResult.getTestProgresses().stream()
                        .allMatch(tp -> tp.getStatus() == TestStatus.COMPLETED);
        if (allCompleted) {
            testsResult.getTestProgresses().clear();
            testsResultRepository.save(testsResult);
            log.info("All tests completed, resetting cycle for new round. bestScore preserved. userId={}", event.getUserId());
        }

        // Поиск TestProgress для текущего теста
        TestProgress testProgress = testsResult.getTestProgresses().stream()
                .filter(tp -> tp.getTestId().equals(event.getTestId()))
                .findFirst()
                .orElseGet(() -> {
                    TestProgress newProgress = getBuild(event, testsResult);
                    testsResult.getTestProgresses().add(newProgress);
                    log.info("No existing TestProgress found, creating new TestProgress for testId={}", event.getTestId());
                    return newProgress;
                });

        // Проверка на дубликаты TestItemResult
        boolean isNewTestItemResult = testProgress.getTestItemResults().stream()
                .noneMatch(r -> r.getTestItemId().equals(testItemResult.getTestItemId()));

        if (isNewTestItemResult) {
            updateTestProgress(testProgress, testItemResult, testsResult);
            log.info("Added new TestItemResult with testItemId={} to TestProgress testId={}",
                    testItemResult.getTestItemId(), testProgress.getTestId());
        } else {
            log.info("TestItemResult with testItemId={} already exists in TestProgress testId={}",
                    testItemResult.getTestItemId(), testProgress.getTestId());
        }

        log.info("TestItemResolvedEvent processed successfully: TestsResultId={}", testsResult.getId());
    }


    private static TestProgress getBuild(TestItemResolvedEvent event, TestsResult testsResult) {
        return TestProgress.builder()
                .testId(event.getTestId())
                .testsResult(testsResult)
                .build();
    }

    private static void updateTestProgress(TestProgress testProgress, TestItemResult testItemResult, TestsResult testsResult) {
        testProgress.getTestItemResults().add(testItemResult);
        testProgress.updateScore();
        testProgress.setTestsResult(testsResult);
        testItemResult.setTestProgress(testProgress);
    }

    private boolean allAttemptsUsed(@NonNull final List<TestProgress> progressList) {
        return progressList.stream()
                .allMatch(testProgress -> testProgress.getStatus().equals(TestStatus.COMPLETED));
    }

    private List<String> getCompletedTestIds(@NonNull final List<TestProgress> progressList) {
        return progressList.stream()
                .filter(testProgress -> testProgress.getStatus().equals(TestStatus.COMPLETED))
                .map(TestProgress::getTestId)
                .toList();
    }

    private int getNumberOfCompletedTests(@NonNull final List<TestProgress> progressList) {
        return (int) progressList.stream()
                .filter(testProgress -> testProgress.getStatus().equals(TestStatus.COMPLETED))
                .count();
    }

    private void saveBestScoreToTopicProgress(@NonNull final String userId,
                                              @NonNull final String courseId,
                                              @NonNull final String topicId,
                                              @NonNull final Double bestScore) {
        log.info(
                "Saving best score to TopicProgress for userId={}, courseId={}, topicId={}, bestScore={}",
                userId, courseId, topicId, bestScore
        );

        TopicProgress topicProgress = topicProgressRepository.findByUserIdAndTopicId(userId, topicId)
                .orElse(TopicProgress.builder()
                        .userId(userId)
                        .courseId(courseId)
                        .topicId(topicId)
                        .build());
        topicProgress.setBestTestScorePercentage(bestScore);
        topicProgress.updateStatus();
        topicProgressRepository.save(topicProgress);
    }
}
