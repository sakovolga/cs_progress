package com.example.cs_progress.controller;

import com.example.cs_common.dto.request.CodeSnapshotRq;
import com.example.cs_common.dto.response.TaskProgressAutosaveRs;
import com.example.cs_common.dto.response.TaskProgressDetailsRs;
import com.example.cs_common.dto.response.TaskProgressListRs;
import com.example.cs_common.dto.response.TaskStatusRs;
import com.example.cs_common.util.BaseController;
import com.example.cs_progress.service.TaskProgressService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    public TaskProgressDetailsRs getTaskProgressDetails(@RequestParam @NotBlank String userId,
                                                        @RequestParam @NotBlank String taskId,
                                                        @RequestParam @NotBlank String topicId) {
        log.info("Request getTaskProgressDetailsRs for userId: {} and taskId: {}", userId, taskId);

        return taskProgressService.getTaskProgressDetails(userId, taskId, topicId);
    }

    @PostMapping("/autosave")
    public TaskProgressAutosaveRs autosaveTaskProgress(@RequestBody CodeSnapshotRq rq) {
        log.info("Request autosaveTaskProgress: {}", rq.getTaskProgressId());

        return taskProgressService.autosave(rq);
    }

    @GetMapping("/{taskProgressId}/status")
    public TaskStatusRs getTaskStatus(@PathVariable("taskProgressId") String taskProgressId) {
        log.info("Request getTaskStatus for taskProgressId: {}", taskProgressId);

        return taskProgressService.getTaskStatus(taskProgressId);
    }

}
