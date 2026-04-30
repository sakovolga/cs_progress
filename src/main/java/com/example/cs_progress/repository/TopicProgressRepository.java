package com.example.cs_progress.repository;

import com.example.cs_common.dto.response.DashboardCourseInfoRs;
import com.example.cs_common.dto.response.DashboardTopicProgressRs;
import com.example.cs_common.enums.TopicStatus;
import com.example.cs_progress.model.entity.TopicProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TopicProgressRepository extends JpaRepository<TopicProgress, String> {

    Optional<TopicProgress> findByUserIdAndTopicId(String userId, String topicId);

    List<TopicProgress> findByUserId(String userId);

    @Query("SELECT tp.topicId FROM TopicProgress tp " +
            "WHERE tp.userId = :userId AND tp.courseId = :courseId " +
            "AND tp.status = com.example.cs_common.enums.TopicStatus.COMPLETED")
    Set<String> findCompletedTopicIdsByUserIdAndCourseId(
            @Param("userId") String userId,
            @Param("courseId") String courseId
    );

    long countByUserIdAndStatusAndUpdatedAtAfter(
            String userId,
            TopicStatus status,
            LocalDateTime after
    );

    long countByUserIdAndCourseIdAndStatus(String userId, String courseId, TopicStatus status);

    @Query("""
        SELECT new com.example.cs_common.dto.response.DashboardCourseInfoRs(
            tp.courseId,
            co.courseName,
            co.totalTopics,
            CAST(SUM(CASE WHEN tp.status = com.example.cs_common.enums.TopicStatus.COMPLETED THEN 1 ELSE 0 END) AS integer),
            ucc.completedAt,
            COALESCE(ucc.celebrationShown, false)
        )
        FROM TopicProgress tp
        JOIN CourseOverview co ON co.courseId = tp.courseId
        LEFT JOIN UserCourseCompletion ucc ON ucc.userId = tp.userId AND ucc.courseId = tp.courseId
        WHERE tp.userId = :userId
        GROUP BY tp.courseId, co.courseName, co.totalTopics, ucc.completedAt, ucc.celebrationShown
    """)
    List<DashboardCourseInfoRs> findDashboardByUserId(@Param("userId") String userId);

    @Query("""
        SELECT new com.example.cs_common.dto.response.DashboardTopicProgressRs(
            tp.topicId,
            tp.bestTestScorePercentage,
            tp.taskCompletionPercentage,
            tp.status,
            tp.practiceAbsent
        )
        FROM TopicProgress tp
        WHERE tp.userId = :userId
        AND tp.courseId = :courseId
    """)
    List<DashboardTopicProgressRs> findByUserIdAndCourseId(
            @Param("userId") String userId,
            @Param("courseId") String courseId
    );
    // ========== Для дашбордов ==========

//    @Query("SELECT tp FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId " +
//            "ORDER BY tp.courseId, tp.lastActivity DESC")
//    List<TopicProgress> findAllByUserIdOrderedByActivity(@Param("userId") String userId);

//    @Query("SELECT tp FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId AND tp.courseId = :courseId " +
//            "ORDER BY tp.topicId")
//    List<TopicProgress> findByUserIdAndCourseIdOrdered(
//            @Param("userId") String userId,
//            @Param("courseId") String courseId
//    );

    // ========== Статистика ==========

//    @Query("SELECT COUNT(tp) FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId AND tp.courseId = :courseId AND tp.status = :status")
//    long countByUserIdAndCourseIdAndStatus(
//            @Param("userId") String userId,
//            @Param("courseId") String courseId,
//            @Param("status") TopicStatus status
//    );
//
//    @Query("SELECT COUNT(tp) FROM TopicProgress tp " +
//            "WHERE tp.userId = :userId AND tp.status = 'COMPLETED'")
//    long countCompletedTopics(@Param("userId") String userId);

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