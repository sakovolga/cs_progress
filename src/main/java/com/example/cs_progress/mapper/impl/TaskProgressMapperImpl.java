package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.request.TaskProgressAutosaveRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.enums.TaskStatus;
import com.example.cs_common.util.BaseMapper;
import com.example.cs_progress.mapper.TaskProgressMapper;
import com.example.cs_progress.model.entity.TaskProgress;
import lombok.NonNull;
import org.springframework.stereotype.Component;

@Component
public class TaskProgressMapperImpl extends BaseMapper implements TaskProgressMapper {

    @Override
    public TaskProgressDetailsRs toTaskProgressDetailsRs(@NonNull final TaskProgress taskProgress) {
        log.info("Mapping TaskProgress with id: {} to TaskProgressDetailsRs", taskProgress.getId());

        return new TaskProgressDetailsRs(
                taskProgress.getId(),
                taskProgress.getUserId(),
                taskProgress.getTaskId(),
                taskProgress.getTaskStatus(),
                taskProgress.getScore(),
                taskProgress.getLastSnapshot(),
                taskProgress.getCodeQualityRating()
        );
    }

    @Override
    public TaskProgress toTaskProgress(@NonNull final TaskProgressAutosaveRq rq,
                                       @NonNull final TaskProgress taskProgress) {
        log.info("Mapping TaskProgressAutosaveRq to existing TaskProgress with id: {}", taskProgress.getId());

        taskProgress.setLastSnapshot(rq.getLastSnapshot());
        if (taskProgress.getTaskStatus() == TaskStatus.NOT_STARTED) {
            taskProgress.setTaskStatus(TaskStatus.IN_PROGRESS);
        }

        return taskProgress;
    }

    @Override
    public TaskProgressAutosaveRs toTaskProgressAutosaveRs(@NonNull final TaskProgress taskProgress) {
        log.info("Mapping TaskProgress with id: {} to TaskProgressAutosaveRs", taskProgress.getId());

        return new TaskProgressAutosaveRs(
                taskProgress.getId(),
                taskProgress.getUpdatedAt()
        );
    }
}
