package com.example.cs_progress.controller;

import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.TaskProgressService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/task-progress")
public class TaskProgressController extends BaseController {

    private final TaskProgressService taskProgressService;

    @GetMapping("/list")
    public TaskProgressListRs getTaskProgressList(@RequestParam @NotBlank String userId,
                                                    @RequestParam @NotBlank String topicId) {
        log.info("Request getTaskProgressListRs for userId: {} and topicId: {}", userId, topicId);

        return taskProgressService.getTaskProgressList(userId, topicId);
    }

    @GetMapping
    public ResponseEntity<TaskProgressDetailsRs> getTaskProgressDetails(@RequestParam @NotBlank String userId,
                                                                       @RequestParam @NotBlank String taskId) {
        log.info("Request getTaskProgressDetailsRs for userId: {} and taskId: {}", userId, taskId);

        TaskProgressDetailsRs rs = taskProgressService.getTaskProgressDetails(userId, taskId);
        if (rs == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(rs);
    }
}
