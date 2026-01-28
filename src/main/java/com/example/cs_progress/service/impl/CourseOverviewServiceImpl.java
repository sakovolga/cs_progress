package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_common.dto.response.CourseOverviewSynchronizationRs;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.mapper.CourseOverviewMapper;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.service.CourseOverviewService;
import com.example.cs_progress.service.TagCountService;
import com.example.cs_progress.service.TaskTopicCountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseOverviewServiceImpl extends BaseService implements CourseOverviewService {

    private final CourseOverviewRepository courseOverviewRepository;
    private final TagCountService tagCountService;
    private final TaskTopicCountService taskTopicCountService;
    private final CourseOverviewMapper courseOverviewMapper;

    @Override
    @Transactional
    public void handleTaskStatsChangedEvent(@NonNull final TaskStatsChangedEvent event) {
        log.info(
                "Handling TaskStatsChangedEvent for courseId: {}, topicId: {}, tagNamesAdded: {}," +
                        "tagNamesRemoved: {}, isTaskCreated: {}, isTaskDeleted: {}",
                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(),
                event.getTagNamesRemoved(), event.getIsTaskCreated(), event.getIsTaskDeleted()
        );

        CourseOverview courseOverview = courseOverviewRepository.findByCourseId(event.getCourseId())
                .orElse(CourseOverview.builder()
                        .courseId(event.getCourseId())
                        .build());

        tagCountService.update(event, courseOverview);
        taskTopicCountService.updateTaskTopicCount(event, courseOverview);

        courseOverviewRepository.save(courseOverview);
    }

    @Override
    @Transactional
    public CourseOverviewSynchronizationRs synchronizeCourseOverview(@NonNull final CourseOverviewDto courseOverviewDto) {
        log.info("Synchronizing CourseOverview for courseId: {}", courseOverviewDto.getCourseId());

        courseOverviewRepository.deleteByCourseId(courseOverviewDto.getCourseId());

        CourseOverview courseOverview = courseOverviewMapper.toCourseOverview(courseOverviewDto);
        courseOverview = courseOverviewRepository.save(courseOverview);

        log.info("CourseOverview synchronized successfully for courseId: {}", courseOverview.getCourseId());

        return CourseOverviewSynchronizationRs.builder()
                .courseId(courseOverview.getCourseId())
                .lastUpdated(courseOverview.getUpdatedAt())
                .build();
    }

}
