package com.example.cs_progress.service;

import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;

public interface TaskProgressService {

    TaskProgressListRs getTaskProgressList(String userId, String topicId);

    TaskProgressDetailsRs getTaskProgressDetails(String userId, String taskId);
}
