package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.CourseOverview;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CourseOverviewRepository extends JpaRepository<CourseOverview, String> {

    @EntityGraph(attributePaths = {"topicOverviews", "tagCounts", "tagCounts.topicCounts"})
    Optional<CourseOverview> findByCourseId(String courseId);

    void deleteByCourseId(String courseId);

}
