package com.example.cs_progress.controller;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_common.dto.response.CourseOverviewSynchronizationRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.CourseOverviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-overview")
public class CourseOverviewController extends BaseController {

    private final CourseOverviewService courseOverviewService;

    @PostMapping
    public CourseOverviewSynchronizationRs updateCourseOverviews(@RequestBody CourseOverviewDto courseOverviewDto) {
        log.info("Request to update course overviews for courseId: {}", courseOverviewDto.getCourseId());

        return courseOverviewService.synchronizeCourseOverview(courseOverviewDto);
    }

}
