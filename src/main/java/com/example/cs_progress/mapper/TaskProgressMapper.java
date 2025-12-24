package com.example.cs_progress.mapper;

import com.example.cs_common.dto.request.TaskProgressAutosaveRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_progress.model.entity.TaskProgress;

public interface TaskProgressMapper {
    TaskProgressDetailsRs toTaskProgressDetailsRs(TaskProgress taskProgress);

    TaskProgress toTaskProgress(TaskProgressAutosaveRq rq, TaskProgress taskProgress);

    TaskProgressAutosaveRs toTaskProgressAutosaveRs(TaskProgress taskProgress);
}
