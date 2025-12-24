package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.request.TaskProgressAutosaveRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_common.enums.TaskStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.mapper.TaskProgressMapper;
import com.example.cs_progress.mapper.TestProgressMapper;
import com.example.cs_progress.model.entity.TaskProgress;
import com.example.cs_progress.repository.TaskProgressRepository;
import com.example.cs_progress.service.TaskProgressService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.cs_common.exception.error.SystemError.ENTITY_NOT_FOUND_ERROR;

@Service
@RequiredArgsConstructor
public class TaskProgressServiceImpl extends BaseService implements TaskProgressService {

    private final TaskProgressRepository taskProgressRepository;
    private final TaskProgressMapper taskProgressMapper;

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

    @Override
    public TaskProgressDetailsRs getTaskProgressDetails(@NonNull final String userId,
                                                        @NonNull final String taskId,
                                                        @NonNull final String topicId) {
        log.info("Attempting to get task progress details for userId: {} and taskId: {}", userId, taskId);

        TaskProgressDetailsRs taskProgressDetailsRs = taskProgressRepository.findByUserIdAndTaskId(userId, taskId);

        if (taskProgressDetailsRs == null) {
            log.warn("No task progress details found for userId: {} and taskId: {}", userId, taskId);
            TaskProgress taskProgress = TaskProgress.builder()
                    .taskId(taskId)
                    .userId(userId)
                    .topicId(topicId)
                    .taskStatus(TaskStatus.NOT_STARTED)
                    .build();
            taskProgress = taskProgressRepository.saveAndFlush(taskProgress);
            taskProgressDetailsRs = taskProgressMapper.toTaskProgressDetailsRs(taskProgress);
        }

        log.info("Task progress details received for userId: {} and taskId: {}", userId, taskId);
        return taskProgressDetailsRs;
    }

    @Override
    @Transactional
    public TaskProgressAutosaveRs autosave(@NonNull final TaskProgressAutosaveRq rq) {
        log.info("Attempting to autosave task progress with id: {}", rq.getTaskProgressId());

        TaskProgress taskProgress = taskProgressRepository.findById(rq.getTaskProgressId()).orElseThrow(
                () -> new NotFoundException("TaskProgress not found with id: " + rq.getTaskProgressId(),
                        ENTITY_NOT_FOUND_ERROR)
        );
        taskProgress = taskProgressMapper.toTaskProgress(rq, taskProgress);
        taskProgress = taskProgressRepository.save(taskProgress);
        TaskProgressAutosaveRs rs = taskProgressMapper.toTaskProgressAutosaveRs(taskProgress);

        log.info("Task progress with id: {} successfully autosaved at {}", rs.taskProgressId(), rs.savedAt());
        return rs;
    }

}
