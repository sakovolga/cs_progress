package com.example.cs_progress.repository;

import com.example.cs_common.dto.response.TaskProgressSummaryRs;
import com.example.cs_progress.model.entity.TaskProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskProgressRepository extends JpaRepository<TaskProgress, String> {

    List<TaskProgressSummaryRs> findByUserIdAndTopicId(String userId, String topicId);
}
