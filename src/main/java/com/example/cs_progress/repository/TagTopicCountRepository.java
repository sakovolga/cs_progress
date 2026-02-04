package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagTopicCount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TagTopicCountRepository extends JpaRepository<TagTopicCount, String> {

    @Modifying
    @Query("DELETE FROM TagTopicCount ttc WHERE ttc.tagCount.courseOverview.courseId = :courseId")
    void deleteByTagCountCourseOverviewCourseId(@Param("courseId") String courseId);
}
