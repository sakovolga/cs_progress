package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_common.dto.request.TestItemUserResolvedRq;
import com.example.cs_progress.mapper.TestItemResultMapper;
import com.example.cs_progress.model.entity.TestItemResult;
import com.example.cs_progress.model.entity.TestProgress;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestItemResultMapperImpl implements TestItemResultMapper {

    @Override
    public TestItemResult toTestItemResult(@NonNull final TestItemUserResolvedRq rq,
                                           @NonNull final TestProgress testProgress) {
        log.info("Mapping TestItemUserResolvedRq to TestItemResult: {}", rq);

        return TestItemResult.builder()
                .testItemId(rq.getTestItemResolvedRq().getTestItemId())
                .testProgress(testProgress)
                .score(rq.getTestItemResolvedRq().getTestItemScore())
                .build();
    }

    @Override
    public TestItemResult toTestItemResult(@NonNull final TestItemResolvedEvent event,
                                           @NonNull final TestProgress testProgress) {
        log.info("Mapping TestItemResolvedEvent to TestItemResult: {}", event);
        return TestItemResult.builder()
                .testItemId(event.getTestItemId())
                .testProgress(testProgress)
                .score(event.getTestItemScore())
                .build();
    }
}
