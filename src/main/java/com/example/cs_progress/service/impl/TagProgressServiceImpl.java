package com.example.cs_progress.service.impl;

import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TagProgress;
import com.example.cs_progress.model.entity.TagTaskCount;
import com.example.cs_progress.model.entity.TagTaskTopicCount;
import com.example.cs_progress.model.entity.TagTopicProgress;
import com.example.cs_progress.repository.TagProgressRepository;
import com.example.cs_progress.repository.TagTaskCountRepository;
import com.example.cs_progress.service.TagProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TagProgressServiceImpl extends BaseService implements TagProgressService {

    private final TagProgressRepository tagProgressRepository;
    private final TagTaskCountRepository tagTaskCountRepository;

    private static final Double MAX_TEST_ITEM_SCORE = 10.0;

    @Override
    @Transactional
    public void processTagsFromResolvedTestItem(@NonNull final String courseId,
                                                @NonNull final String topicId,
                                                @NonNull final String userId,
                                                List<String> tagNames,
                                                final Double score) {
        log.info(
                "Processing tag progress after resolving testItem " +
                        "for userId: {}, courseId: {}, topicId: {}, tags: {}, score: {}",
                userId, courseId, topicId, tagNames, score
        );

        if (tagNames == null || tagNames.isEmpty()) {
            log.info("No tags to process for the resolved test item.");
            return;
        }

        boolean isCorrect = isFullyCorrectAnswer(score);

        List<TagProgress> existingTagProgresses = tagProgressRepository
                .findByTagNameInAndUserIdAndCourseId(tagNames, userId, courseId);
        Map<String, TagProgress> tagProgressMap = existingTagProgresses.stream()
                .collect(Collectors.toMap(TagProgress::getTagName, tp -> tp));

        for (String tagName : tagNames) {
            if (tagProgressMap.containsKey(tagName)) {
                updateTagProgress(tagProgressMap.get(tagName), topicId, isCorrect);
            } else {
                buildTagProgress(courseId, topicId, userId, tagName, isCorrect);
            }
        }

        log.info("Completed processing tag progress for resolved test item for userId: {}", userId);
    }

    private boolean isFullyCorrectAnswer(Double score) {
        return score != null && score.equals(MAX_TEST_ITEM_SCORE);
    }

    private void updateTagProgress(@NonNull final TagProgress tagProgress,
                                   @NonNull final String topicId,
                                   final boolean isCorrect) {
        log.info("Updating TagProgress for tag: {} in topicId: {}", tagProgress.getTagName(), topicId);

        List<TagTopicProgress> tagTopicProgresses = tagProgress.getTopicProgresses();

        TagTopicProgress tagTopicProgress = tagTopicProgresses.stream()
                .filter(tp -> tp.getTopicId().equals(topicId))
                .findFirst()
                .orElseGet(() -> createMissingTagTopicProgress(tagProgress, topicId));

        if (isCorrect) {
            tagTopicProgress.incrementCorrectTestAnswers();
            tagProgress.recalculateCorrectTestAnswers();
        } else {
            tagTopicProgress.incrementQuestionsAnswered();
            tagProgress.recalculateAnsweredTestQuestions();
        }
    }

    private TagTopicProgress createMissingTagTopicProgress(TagProgress tagProgress, String topicId) {
        log.warn("TagTopicProgress not found for tag: {} in topic: {}. Creating new one.",
                tagProgress.getTagName(), topicId);

        Integer expectedTasks = tagTaskCountRepository
                .findByTagName(tagProgress.getTagName())
                .flatMap(ttc -> ttc.getTopicCounts().stream()
                        .filter(tc -> tc.getTopicId().equals(topicId))
                        .findFirst())
                .map(TagTaskTopicCount::getCount)
                .orElse(0);

        TagTopicProgress newTopicProgress = TagTopicProgress.builder()
                .tagProgress(tagProgress)
                .topicId(topicId)
                .expectedTasks(expectedTasks)
                .build();

        tagProgress.getTopicProgresses().add(newTopicProgress);

        return newTopicProgress;
    }

    private void buildTagProgress(@NonNull final String courseId,
                                  @NonNull final String topicId,
                                  @NonNull final String userId,
                                  @NonNull final String tagName,
                                  final boolean isCorrect) {
        log.info("Building new TagProgress for tag: {} in topicId: {} in courseId: {}",
                tagName, topicId, courseId);

        TagTaskCount expectedTaskCount = tagTaskCountRepository.findByTagName(tagName)
                .orElseThrow(() -> new NotFoundException(
                        "Expected task count not found for tag: " + tagName, ENTITY_NOT_FOUND_ERROR
                ));

        TagProgress tagProgress = TagProgress.builder()
                .tagName(tagName)
                .userId(userId)
                .courseId(courseId)
                .totalTasks(expectedTaskCount.getCount())
                .build();

        for (TagTaskTopicCount tagTaskTopicCount : expectedTaskCount.getTopicCounts()) {
            TagTopicProgress tagTopicProgress = TagTopicProgress.builder()
                    .topicId(tagTaskTopicCount.getTopicId())
                    .expectedTasks(tagTaskTopicCount.getCount())
                    .tagProgress(tagProgress)
                    .build();
            if (topicId.equals(tagTaskTopicCount.getTopicId())) {
                if (isCorrect) {
                    tagTopicProgress.incrementCorrectTestAnswers();
                } else {
                    tagTopicProgress.incrementQuestionsAnswered();
                }
            }
            tagProgress.getTopicProgresses().add(tagTopicProgress);
        }

        if (isCorrect) {
            tagProgress.recalculateCorrectTestAnswers();
        } else {
            tagProgress.recalculateAnsweredTestQuestions();
        }

        tagProgressRepository.save(tagProgress);
    }
}
