package com.example.cs_progress.mapper;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_common.dto.request.TestItemUserResolvedRq;
import com.example.cs_progress.model.entity.TestItemResult;
import com.example.cs_progress.model.entity.TestProgress;

public interface TestItemResultMapper {

    TestItemResult toTestItemResult(TestItemUserResolvedRq rq, TestProgress testProgress);

    TestItemResult toTestItemResult(TestItemResolvedEvent event, TestProgress testProgress);
}
