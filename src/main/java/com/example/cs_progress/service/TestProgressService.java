package com.example.cs_progress.service;

import com.example.cs_common.dto.response.CurrentTestInfoRs;

public interface TestProgressService {

    CurrentTestInfoRs getCurrentTestInfo(String userId, String courseId, String topicId);
}
