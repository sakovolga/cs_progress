package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TestsResult;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TestsResultRepository extends JpaRepository<TestsResult, String> {

    @EntityGraph(attributePaths = {
            "testProgresses"
    })
    Optional<TestsResult> findByUserIdAndTopicId(String userId, String topicId);
}
