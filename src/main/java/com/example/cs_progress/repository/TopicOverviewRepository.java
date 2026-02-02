package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TopicOverview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TopicOverviewRepository extends JpaRepository<TopicOverview, String> {

    Optional<TopicOverview> findByTopicId(String topicId);
}
