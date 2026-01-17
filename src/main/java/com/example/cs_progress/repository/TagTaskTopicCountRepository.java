package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagTaskTopicCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TagTaskTopicCountRepository extends JpaRepository<TagTaskTopicCount, String> {
}
