package com.example.cs_progress.controller;

import com.example.cs_common.dto.response.CourseCompletionRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.CourseCompletionService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/course-completion")
public class CourseCompletionController extends BaseController {

    private final CourseCompletionService courseCompletionService;

    @GetMapping
    public CourseCompletionRs getCourseCompletion(@RequestParam @NotBlank String userId,
                                                  @RequestParam @NotBlank String courseId) {
        log.info("Request getCourseCompletion for userId: {} and courseId: {}", userId, courseId);

        return courseCompletionService.getCourseCompletion(userId, courseId);
    }

}