package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TestProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TestProgressRepository extends JpaRepository<TestProgress, String> {
}
