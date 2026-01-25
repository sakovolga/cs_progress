package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagCountRepository extends JpaRepository<TagCount, String> {

    @EntityGraph(attributePaths = "topicCounts")
    Optional<TagCount> findByTagName(String tagName);

    @EntityGraph(attributePaths = "topicCounts")
    List<TagCount> findByTagNameInAndCourseId(List<String> tagNames, String courseId);


}

