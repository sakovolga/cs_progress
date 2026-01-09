package com.example.cs_progress.repository;

import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_common.dto.response.TaskStatusRs;
import com.example.cs_progress.model.entity.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, String> {

    List<TaskProgressSummaryRs> findByUserIdAndTopicId(String userId, String topicId);

    TaskProgressDetailsRs findByUserIdAndTaskId(String userId, String taskId);

    @Query("""
        SELECT new com.example.cs_common.dto.response.TaskStatusRs(
            tp.taskStatus,
            tp.codeQualityRating
        )
        FROM TaskProgress tp
        WHERE tp.id = :taskProgressId
    """)
    Optional<TaskStatusRs> findStatusByTaskProgressId(@Param("taskProgressId") String taskProgressId);

}
