package com.example.cs_progress.repository;

import com.example.cs_common.enums.TopicStatus;
import com.example.cs_progress.model.entity.TopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TopicProgressRepository extends JpaRepository<TopicProgress, String> {

    // ========== Основные запросы ==========

    Optional<TopicProgress> findByUserIdAndTopicId(String userId, String topicId);

    List<TopicProgress> findByUserId(String userId);

    List<TopicProgress> findByUserIdAndCourseId(String userId, String courseId);

    // ========== Для дашбордов ==========

    @Query("SELECT tp FROM TopicProgress tp " +
            "WHERE tp.userId = :userId " +
            "ORDER BY tp.courseId, tp.lastActivity DESC")
    List<TopicProgress> findAllByUserIdOrderedByActivity(@Param("userId") String userId);

    @Query("SELECT tp FROM TopicProgress tp " +
            "WHERE tp.userId = :userId AND tp.courseId = :courseId " +
            "ORDER BY tp.topicId")
    List<TopicProgress> findByUserIdAndCourseIdOrdered(
            @Param("userId") String userId,
            @Param("courseId") String courseId
    );

    // ========== Статистика ==========

    @Query("SELECT COUNT(tp) FROM TopicProgress tp " +
            "WHERE tp.userId = :userId AND tp.courseId = :courseId AND tp.status = :status")
    long countByUserIdAndCourseIdAndStatus(
            @Param("userId") String userId,
            @Param("courseId") String courseId,
            @Param("status") TopicStatus status
    );

    @Query("SELECT COUNT(tp) FROM TopicProgress tp " +
            "WHERE tp.userId = :userId AND tp.status = 'COMPLETED'")
    long countCompletedTopics(@Param("userId") String userId);

    // ========== Для рекомендаций ==========

//    @Query("SELECT tp FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId " +
//            "AND tp.status = 'IN_PROGRESS' " +
//            "AND tp.progressPercentage < 80 " +
//            "AND DATEDIFF(CURRENT_DATE, tp.lastActivity) > 7 " +
//            "ORDER BY tp.lastActivity ASC")
//    List<TopicProgress> findStuckTopics(@Param("userId") String userId);
//
//    @Query("SELECT tp FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId " +
//            "AND tp.courseId = :courseId " +
//            "AND tp.status = 'IN_PROGRESS' " +
//            "ORDER BY tp.progressPercentage DESC")
//    List<TopicProgress> findInProgressTopics(
//            @Param("userId") String userId,
//            @Param("courseId") String courseId
//    );
}