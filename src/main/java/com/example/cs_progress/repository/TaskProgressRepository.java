package com.example.cs_progress.repository;

import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_common.enums.TaskStatus;
import com.example.cs_progress.model.entity.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, String> {

    List<TaskProgressSummaryRs> findByUserIdAndTopicId(String userId, String topicId);

    List<TaskProgressSummaryRs> findByUserIdAndTaskIdIn(String userId, List<String> taskIds);

    TaskProgressDetailsRs findByUserIdAndTaskId(String userId, String taskId);

    long countByUserIdAndTaskStatusAndUpdatedAtAfter(
            String userId,
            TaskStatus status,
            LocalDateTime after
    );

}
