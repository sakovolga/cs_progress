package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TaskTopicCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskTopicCountRepository extends JpaRepository<TaskTopicCount, String> {

    Optional<TaskTopicCount> findByTopicId(String topicId);
}
