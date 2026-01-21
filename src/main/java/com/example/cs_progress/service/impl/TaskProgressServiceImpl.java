package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.SnapshotSentEvent;
import com.example.cs_common.dto.event.TaskCompletedEvent;
import com.example.cs_common.dto.request.CodeSnapshotRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_common.dto.response.TaskStatusRs;
import com.example.cs_common.enums.CodeQualityRating;
import com.example.cs_common.enums.TaskStatus;
import com.example.cs_common.exception.NotFoundException;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.mapper.TaskProgressMapper;
import com.example.cs_progress.model.entity.TaskProgress;
import com.example.cs_progress.repository.TaskProgressRepository;
import com.example.cs_progress.service.TagProgressService;
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
    private final TagProgressService tagProgressService;

    @Override
    @Transactional(readOnly = true)
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
    @Transactional
    public TaskProgressDetailsRs getTaskProgressDetails(@NonNull final String userId,
                                                        @NonNull final String taskId,
                                                        @NonNull final String topicId,
                                                        @NonNull final String courseId) {
        log.info("Attempting to get task progress details for userId: {} and taskId: {}", userId, taskId);

        TaskProgressDetailsRs taskProgressDetailsRs = taskProgressRepository.findByUserIdAndTaskId(userId, taskId);

        if (taskProgressDetailsRs == null) {
            log.warn("No task progress details found for userId: {} and taskId: {}", userId, taskId);
            TaskProgress taskProgress = TaskProgress.builder()
                    .taskId(taskId)
                    .userId(userId)
                    .topicId(topicId)
                    .courseId(courseId)
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
    public TaskProgressAutosaveRs autosave(final CodeSnapshotRq rq) {
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

    @Override
    @Transactional
    public void saveSnapshot(@NonNull final SnapshotSentEvent event){
        log.info("Attempting to save snapshot for task progress with id: {}",
                event.getTaskProgressId());

        TaskProgress taskProgress = taskProgressRepository.findById(event.getTaskProgressId()).orElseThrow(
                () -> new NotFoundException("TaskProgress not found with id: " + event.getTaskProgressId(),
                        ENTITY_NOT_FOUND_ERROR)
        );

        taskProgress.setLastSnapshot(event.getLastSnapshot());
        taskProgressRepository.save(taskProgress);

        log.info("Snapshot for task progress with id: {} successfully saved", taskProgress.getId());
    }

    @Override
    @Transactional
    public void processTaskCompletedEvent(@NonNull final TaskCompletedEvent event) {
        log.info("Attempting to update status and rating for task progress with id: {}",
                event.getTaskProgressId());

        TaskProgress taskProgress = taskProgressRepository.findById(event.getTaskProgressId()).orElseThrow(
                () -> new NotFoundException("TaskProgress not found with id: " + event.getTaskProgressId(),
                        ENTITY_NOT_FOUND_ERROR)
        );

        TaskStatus previousStatus = taskProgress.getTaskStatus();

        taskProgress.setTaskStatus(event.getTaskStatus());

        CodeQualityRating current = taskProgress.getCodeQualityRating();
        CodeQualityRating incoming = event.getCodeQualityRating();

        if (current == null || current == CodeQualityRating.NEEDS_IMPROVEMENT) {
            taskProgress.setCodeQualityRating(incoming);
        } else if (current == CodeQualityRating.GOOD &&
                incoming == CodeQualityRating.EXCELLENT) {
            taskProgress.setCodeQualityRating(incoming);
        }
        taskProgressRepository.save(taskProgress);

        if (!previousStatus.equals(TaskStatus.SOLVED)) {
            tagProgressService.processTagsFromCompletedTask(
                    taskProgress.getCourseId(), taskProgress.getTopicId(), taskProgress.getUserId(), event.getTagNames()
            );
        }

        log.info("Task progress with id: {} successfully updated to status: {} and rating: {}",
                event.getTaskProgressId(), event.getTaskStatus(), event.getCodeQualityRating());

    }

    @Override
    @Transactional(readOnly = true)
    public TaskStatusRs getTaskStatus(@NonNull final String taskProgressId) {
        log.info("Attempting to get task status for task progress id: {}", taskProgressId);

        TaskStatusRs taskStatusRs = taskProgressRepository.findStatusByTaskProgressId(taskProgressId)
                .orElseThrow(() -> new NotFoundException("TaskProgress not found with id: " + taskProgressId,
                        ENTITY_NOT_FOUND_ERROR));

        log.info("Task status: {} and codeQualityRating: {} received for taskProgressId: {}",
                taskStatusRs.taskStatus(), taskStatusRs.codeQualityRating(), taskProgressId);
        return taskStatusRs;
    }

}
