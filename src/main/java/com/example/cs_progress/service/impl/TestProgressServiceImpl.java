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
import com.example.cs_progress.repository.TestsResultRepository;
import com.example.cs_progress.service.TestProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TestProgressServiceImpl extends BaseService implements TestProgressService  {

    private final TestsResultRepository testsResultRepository;
    private final TestItemResultMapper testItemResultMapper;
    private final TestProgressMapper testProgressMapper;
    private final AsyncTagProgressHandler asyncTagProgressHandler;

    private static final int MAX_NUMBER_OF_TEST = 3;
    private static final int MAX_TEST_ITEM_INDEX = 9;
    private static final int MAX_NUMBER_OF_TEST_ITEMS = 10;

    @Override
    @Transactional(readOnly = true)
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
        TestProgress lastTestProgress = testProgressList.getLast();
        int currentIndex = lastTestProgress.getTestItemResults().size() - 1;

        if (testProgressList.size() == MAX_NUMBER_OF_TEST && allAttemptsUsed(testProgressList)) {
            log.info("All attempts used for user with id: {} in topic with id: {}", userId, topicId);

            return CurrentTestInfoRs.builder()
                    .isAllAttemptsUsed(true)
                    .bestScore(optionalTestsResult.get().getBestScore())
                    .build();
        }

        if (currentIndex == MAX_TEST_ITEM_INDEX && getNumberOfCompletedTests(testProgressList) < MAX_NUMBER_OF_TEST) {
            log.info("Test attempt completed, user with id={} can start new attempt", userId);

            return CurrentTestInfoRs.builder()
                    .isTestCompleted(true)
                    .testScore(lastTestProgress.getScore())
                    .isAllAttemptsUsed(false)
                    .bestScore(optionalTestsResult.get().getBestScore())
                    .completedTestIds(getCompletedTestIds(testProgressList))
                    .build();
        }
        log.info(
                "Returning current test item index: {} in test with id: {} in attempting: {} " +
                        "in topic with id: {} in course with id: {} for user with id: {} ",
                currentIndex, lastTestProgress.getTestId(), testProgressList.size() - 1, topicId, courseId, userId
        );
        return CurrentTestInfoRs.builder()
                .currentTestId(lastTestProgress.getTestId())
                .currentTestItemIndex(currentIndex)
                .build();
    }

    @Override
    @Transactional
    public TestResultRs finishTest(@NonNull final TestItemUserResolvedRq rq) {
        log.info("Attempting to finish test with request: {}", rq);

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
            if (testProgress.getTestItemResults().size() != MAX_NUMBER_OF_TEST_ITEMS) {
                throw new IllegalStateException("Cannot finish test before answering all questions");
            }
            testProgress.updateScore();
            testProgress.setStatus(TestStatus.COMPLETED);
        }

        testsResult.setBestScore(calculateBestScore(testsResult));

        TestResultRs rs = testProgressMapper.toTestResultRs(testProgress);

        asyncTagProgressHandler.processTagProgressAsync(
                rq.getTestItemResolvedRq().getCourseId(),
                rq.getTestItemResolvedRq().getTopicId(),
                rq.getUserId(),
                rq.getTestItemResolvedRq().getTagNames(),
                rq.getTestItemResolvedRq().getTestItemScore()
        );

        log.info("Test with id: {} was finished with result: {}", rs.getTestId(), rs.getScore());
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
}
