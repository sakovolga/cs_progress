package com.example.cs_progress.controller;

import com.example.cs_progress.service.LastTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topic")
@Slf4j
public class LastTopicController {

    private final LastTopicService lastTopicService;

    @GetMapping
    public String get(@RequestParam String courseId, @RequestParam String userId) {
        log.info("== REQUEST getCurrentTopic for the userId: {} in the course with id: {} ==", userId, courseId);

        return lastTopicService.get(courseId, userId);
    }
}
