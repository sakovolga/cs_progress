package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TopicOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicOverviewRepository extends JpaRepository<TopicOverview, String> {

    Optional<TopicOverview> findByTopicId(String topicId);

    @Modifying
    @Query("DELETE FROM TopicOverview to WHERE to.courseOverview.courseId = :courseId")
    void deleteByCourseOverviewCourseId(@Param("courseId") String courseId);
}
