package com.example.cs_progress.service.impl;

import com.example.cs_common.util.BaseCacheService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.service.CourseOverviewCacheService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CourseOverviewCacheServiceImpl extends BaseCacheService implements CourseOverviewCacheService {

    private final CourseOverviewRepository courseOverviewRepository;

    @Override
    @Cacheable(value = "course-overviews", key = "#courseId")
    @Transactional
    public Optional<CourseOverview> findByCourseId(@NonNull final String courseId) {
        log.info("Fetching CourseOverview from DB for courseId: {}", courseId);

        Optional<CourseOverview> courseOverview = courseOverviewRepository.findByCourseId(courseId);

        courseOverview.ifPresent(co -> {
            if (co.getTagCounts() != null) {
                Hibernate.initialize(co.getTagCounts());

                co.getTagCounts().forEach(tagCount -> {
                    if (tagCount.getTopicCounts() != null) {
                        Hibernate.initialize(tagCount.getTopicCounts());
                    }
                });
            }
        });

        return courseOverview;
    }
}
