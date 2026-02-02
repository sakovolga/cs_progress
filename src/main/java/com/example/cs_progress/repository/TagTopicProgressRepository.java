package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagTopicProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagTopicProgressRepository extends JpaRepository<TagTopicProgress, String> {

    @EntityGraph(attributePaths = "tagProgress")
    List<TagTopicProgress> findByTagProgress_UserId(String userId);
}
