package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagCountRepository extends JpaRepository<TagCount, String> {

    @EntityGraph(attributePaths = {"topicCounts"})
    Optional<TagCount> findByCourseOverview_CourseIdAndTagName(String courseId, String tagName);

}
