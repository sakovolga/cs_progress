package com.example.cs_progress.repository;

import com.example.cs_progress.model.entity.TagProgress;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface TagProgressRepository extends JpaRepository<TagProgress, String> {

    @EntityGraph(attributePaths = "topicProgresses")
    List<TagProgress> findByTagNameInAndUserIdAndCourseId(Collection<String> tagNames,
                                                          String userId,
                                                          String courseId);

//    List<TagProgress> findByUserId(String userId);

    // Для детектора изменений
    long countByUserIdAndLastActivityAfter(String userId, LocalDateTime after);

    List<TagProgress> findByUserIdAndCourseId(String userId, String courseId);
}
