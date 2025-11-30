package com.example.cs_progress.mapper;

import com.example.cs_common.dto.response.TestResultRs;
import com.example.cs_progress.model.entity.TestProgress;

public interface TestProgressMapper {
    TestResultRs toTestResultRs(TestProgress testProgress);
}
