package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagTopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagTopicProgressRepository extends JpaRepository<TagTopicProgress, String> {
}
