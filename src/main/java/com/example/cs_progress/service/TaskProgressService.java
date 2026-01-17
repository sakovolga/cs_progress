package com.example.cs_progress.service;

import com.example.cs_common.dto.event.SnapshotSentEvent;
import com.example.cs_common.dto.event.TaskCompletedEvent;
import com.example.cs_common.dto.request.CodeSnapshotRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.dto.response.TaskStatusRs;

public interface TaskProgressService {

    TaskProgressListRs getTaskProgressList(String userId, String topicId);

    TaskProgressDetailsRs getTaskProgressDetails(String userId, String taskId, String topicId, String courseId);

    TaskProgressAutosaveRs autosave(CodeSnapshotRq rq);

    void saveSnapshot(SnapshotSentEvent event);

    void updateStatusAndRating(TaskCompletedEvent event);

    TaskStatusRs getTaskStatus(String taskProgressId);
}
