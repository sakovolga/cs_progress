package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_progress.repository.TaskProgressRepository;
import com.example.cs_progress.service.TaskProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskProgressServiceImpl extends BaseService implements TaskProgressService {

    private final TaskProgressRepository taskProgressRepository;

    @Override
    public TaskProgressListRs getTaskProgressList(@NonNull final String userId,
                                                  @NonNull final String topicId) {
        log.info("Attempting to get task progress list for userId: {} and topicId: {}", userId, topicId);

        List<TaskProgressSummaryRs> taskProgressSummaryRsList = taskProgressRepository
                .findByUserIdAndTopicId(userId, topicId);
        TaskProgressListRs taskProgressListRs = TaskProgressListRs.builder()
                .taskProgressRsList(taskProgressSummaryRsList)
                .build();

        log.info(
                "{} task progresses received for userId: {} and topicId: {}",
                taskProgressListRs.getTaskProgressRsList().size(), userId, topicId
        );
        return taskProgressListRs;
    }
}
