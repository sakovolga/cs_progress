package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.UserCourseCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserCourseCompletionRepository extends JpaRepository<UserCourseCompletion, String> {

    boolean existsByUserIdAndCourseId(String userId, String courseId);

    Optional<UserCourseCompletion> findByUserIdAndCourseId(String userId, String courseId);

    @Modifying
    @Query("UPDATE UserCourseCompletion u SET u.celebrationShown = true WHERE u.userId = :userId AND u.courseId IN :courseIds")
    void markCelebrationShown(@Param("userId") String userId, @Param("courseIds") List<String> courseIds);

}