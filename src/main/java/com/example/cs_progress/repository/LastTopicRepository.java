package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.LastTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LastTopicRepository extends JpaRepository<LastTopic, String> {

    Optional<LastTopic> getByCourseIdAndUserId(String courseId, String userId);

//    void deleteByUserIdAndCourseId(String userId, String courseId);

}
