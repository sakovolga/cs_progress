package com.example.cs_progress.service;

import com.example.cs_common.dto.event.TestItemResolvedEvent;
import com.example.cs_common.dto.request.TestItemUserResolvedRq;
import com.example.cs_common.dto.response.CurrentTestInfoRs;
import com.example.cs_common.dto.response.TestResultRs;

public interface TestProgressService {

    CurrentTestInfoRs getCurrentTestInfo(String userId, String courseId, String topicId);

    TestResultRs finishTest(TestItemUserResolvedRq rq);

    void processResolvedTestItem(TestItemResolvedEvent event);
}
