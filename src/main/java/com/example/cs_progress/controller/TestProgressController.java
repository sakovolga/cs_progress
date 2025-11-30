package com.example.cs_progress.controller;

import com.example.cs_common.dto.request.TestItemUserResolvedRq;
import com.example.cs_common.dto.response.CurrentTestInfoRs;
import com.example.cs_common.dto.response.TestResultRs;
import com.example.cs_progress.service.TestProgressService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test-progress")
@Slf4j
public class TestProgressController {

    private final TestProgressService testProgressService;

    @GetMapping
    public CurrentTestInfoRs getCurrentTestInfo(@RequestParam String userId,
                                                @RequestParam String courseId,
                                                @RequestParam String topicId) {
        log.info("== REQUEST getCurrentTestInfo for the userId: {} in the course with id: {} and topic id: {} ==",
                userId, courseId, topicId);

        return testProgressService.getCurrentTestInfo(userId, courseId, topicId);
    }

    @PostMapping("/finish")
    public TestResultRs finishTest(@RequestBody TestItemUserResolvedRq rq) {
        log.info("== REQUEST finishTest with request: {} ==", rq);

        return testProgressService.finishTest(rq);
    }


}
