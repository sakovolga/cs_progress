package com.example.cs_progress.service.impl;

import com.example.cs_common.util.BaseService;
import com.example.cs_progress.service.TagProgressService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AsyncTagProgressHandler extends BaseService {

    private final TagProgressService tagProgressService;

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processTagProgressAsync(String courseId,
                                        String topicId,
                                        String userId,
                                        List<String> tagNames,
                                        Double testItemScore) {
        if (tagNames == null || tagNames.isEmpty()) {
            log.debug("No tags to process for userId={}, topicId={}", userId, topicId);
            return;
        }

        try {
            log.debug("Starting async TagProgress processing for userId={}, topicId={}, tags={}",
                    userId, topicId, tagNames);

            tagProgressService.processTagsFromResolvedTestItem(
                    courseId,
                    topicId,
                    userId,
                    tagNames,
                    testItemScore
            );

            log.debug("Completed async TagProgress processing for userId={}", userId);

        } catch (Exception e) {
            log.error("Failed to process TagProgress asynchronously for userId={}, topicId={}, tags={}",
                    userId, topicId, tagNames, e);
        }
    }
}
