package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_common.util.BaseService;
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
}
