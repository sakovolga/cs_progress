package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.CurrentTestInfoRs;
import com.example.cs_common.enums.TestStatus;
import com.example.cs_progress.model.entity.TestProgress;
import com.example.cs_progress.model.entity.TestsResult;
import com.example.cs_progress.repository.TestsResultRepository;
import com.example.cs_progress.service.TestProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TestProgressServiceImpl implements TestProgressService {

    private final TestsResultRepository testsResultRepository;

    private static final int MAX_NUMBER_OF_TEST = 3;
    private static final int MAX_TEST_ITEM_INDEX = 9;

    @Override
    public CurrentTestInfoRs getCurrentTestInfo(@NonNull final String userId,
                                                @NonNull final String courseId,
                                                @NonNull final String topicId) {
        log.info(
                "Attempting to get next step in test for user with id: {} in topic with id: {} in course with id: {}",
                userId, topicId, courseId
        );

        Optional<TestsResult> optionalTestsResult = testsResultRepository.findByUserIdAndTopicId(userId, topicId);
        if (optionalTestsResult.isEmpty()) {
            log.info("There is no started tests for user with id: {} in topic with id: {}", userId, topicId);

            return CurrentTestInfoRs.builder()
                    .currentTestItemIndex(-1)
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
