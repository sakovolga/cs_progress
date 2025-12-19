package com.example.cs_progress.controller;

import com.example.cs_common.dto.key.LastTopicId;
import com.example.cs_progress.service.LastTopicService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/topic")
public class LastTopicController extends BaseController{

    private final LastTopicService lastTopicService;

    @GetMapping
    public String get(@ModelAttribute LastTopicId id) {
        log.info("== REQUEST getCurrentTopic for the userId: {} in the course with id: {} ==",
                id.getUserId(), id.getCourseId());

        return lastTopicService.get(id);
    }

}
