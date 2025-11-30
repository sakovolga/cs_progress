package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.response.TestResultRs;
import com.example.cs_progress.mapper.TestProgressMapper;
import com.example.cs_progress.model.entity.TestProgress;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class TestProgressMapperImpl implements TestProgressMapper {

    @Override
    public TestResultRs toTestResultRs(@NonNull final TestProgress testProgress) {
        log.info("Mapping TestProgress with id: {} to TestResultRs", testProgress.getId());

        return TestResultRs.builder()
                .testId(testProgress.getTestId())
                .score(testProgress.getScore())
                .build();
    }
}
