package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagTaskCount;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TagTaskCountRepository extends JpaRepository<TagTaskCount, String> {

    @EntityGraph(attributePaths = "topicCounts")
    Optional<TagTaskCount> findByTagName(String tagName);
}

