package com.example.cs_progress.repository;

import com.example.cs_common.dto.key.LastTopicId;
import com.example.cs_progress.model.entity.LastTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LastTopicRepository extends JpaRepository<LastTopic, LastTopicId> {

    @Query("select l.topicId from LastTopic l where l.id = :id")
    Optional<String> findTopicIdById(LastTopicId id);

//    Optional<LastTopic> getByCourseIdAndUserId(String courseId, String userId);

//    void deleteByUserIdAndCourseId(String userId, String courseId);

}
