package com.example.cs_progress.service

import com.example.cs_common.dto.event.TaskCompletedEvent
import com.example.cs_common.dto.request.CodeSnapshotRq
import com.example.cs_common.dto.response.TaskProgressAutosaveRs
import com.example.cs_common.dto.response.TaskProgressDetailsRs
import com.example.cs_common.dto.response.TaskProgressSummaryRs
import com.example.cs_common.enums.CodeQualityRating
import com.example.cs_common.enums.TaskStatus
import com.example.cs_common.exception.NotFoundException
import com.example.cs_progress.mapper.TaskProgressMapper
import com.example.cs_progress.model.entity.TaskProgress
import com.example.cs_progress.repository.TaskProgressRepository
import com.example.cs_progress.service.impl.TaskProgressServiceImpl
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDateTime

class TaskProgressServiceSpec extends Specification {

    TaskProgressRepository taskProgressRepository = Mock()
    TaskProgressMapper taskProgressMapper = Mock()
    TagProgressService tagProgressService = Mock()
    TopicProgressService topicProgressService = Mock()
    CacheEvictionService cacheEvictionService = Mock()

    @Subject
    TaskProgressServiceImpl service = new TaskProgressServiceImpl(
            taskProgressRepository,
            taskProgressMapper,
            tagProgressService,
            topicProgressService,
            cacheEvictionService
    )

    @Unroll
    def "code quality rating is updated correctly: current=#current, incoming=#incoming -> expected=#expected"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(current)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(incoming)
                .tagNames([])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        taskProgress.codeQualityRating == expected

        where:
        current                             | incoming                            || expected
        null                                | CodeQualityRating.NEEDS_IMPROVEMENT || CodeQualityRating.NEEDS_IMPROVEMENT
        null                                | CodeQualityRating.GOOD              || CodeQualityRating.GOOD
        null                                | CodeQualityRating.EXCELLENT         || CodeQualityRating.EXCELLENT
        CodeQualityRating.NEEDS_IMPROVEMENT | CodeQualityRating.GOOD              || CodeQualityRating.GOOD
        CodeQualityRating.NEEDS_IMPROVEMENT | CodeQualityRating.EXCELLENT         || CodeQualityRating.EXCELLENT
        CodeQualityRating.NEEDS_IMPROVEMENT | CodeQualityRating.NEEDS_IMPROVEMENT || CodeQualityRating.NEEDS_IMPROVEMENT
        CodeQualityRating.GOOD              | CodeQualityRating.EXCELLENT         || CodeQualityRating.EXCELLENT
        CodeQualityRating.GOOD              | CodeQualityRating.NEEDS_IMPROVEMENT || CodeQualityRating.GOOD
        CodeQualityRating.GOOD              | CodeQualityRating.GOOD              || CodeQualityRating.GOOD
        CodeQualityRating.EXCELLENT         | CodeQualityRating.GOOD              || CodeQualityRating.EXCELLENT
        CodeQualityRating.EXCELLENT         | CodeQualityRating.NEEDS_IMPROVEMENT || CodeQualityRating.EXCELLENT
        CodeQualityRating.EXCELLENT         | CodeQualityRating.EXCELLENT         || CodeQualityRating.EXCELLENT
    }

    def "tag and topic progress are updated on first task solve"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.GOOD)
                .tagNames(["arrays", "sorting"])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        1 * tagProgressService.processTagsFromCompletedTask("course-1", "topic-1", "user-1", ["arrays", "sorting"])
        1 * topicProgressService.updateTaskStatsInTopicProgress("user-1", "course-1", "topic-1")
        1 * cacheEvictionService.evictTopicProgress("user-1", "course-1")
    }

    def "tag and topic progress are not updated when task was already solved"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.SOLVED)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.EXCELLENT)
                .tagNames(["arrays"])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        0 * tagProgressService.processTagsFromCompletedTask(*_)
        0 * topicProgressService.updateTaskStatsInTopicProgress(*_)
    }

    def "throws NotFoundException when TaskProgress is not found"() {
        given:
        taskProgressRepository.findById("unknown-id") >> Optional.empty()

        def event = TaskCompletedEvent.builder()
                .taskProgressId("unknown-id")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.GOOD)
                .tagNames([])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        thrown(NotFoundException)
    }

    def "task progress list is retrieved correctly"() {
        given:
        def rs1 = new TaskProgressSummaryRs(
                "user-1",
                "task-1",
                "topic-1",
                TaskStatus.SOLVED,
                null,
                null
        )

        def rs2 = new TaskProgressSummaryRs(
                "user-1",
                "task-2",
                "topic-1",
                TaskStatus.IN_PROGRESS,
                null,
                null
        )

        taskProgressRepository.findByUserIdAndTopicId("user-1", "topic-1") >> [rs1, rs2]

        when:
        def result = service.getTaskProgressList("user-1", "topic-1")

        then:
        result.taskProgressRsList.size() == 2
        result.taskProgressRsList[0].taskId() == "task-1"
        result.taskProgressRsList[0].taskStatus() == TaskStatus.SOLVED
        result.taskProgressRsList[1].taskId() == "task-2"
        result.taskProgressRsList[1].taskStatus() == TaskStatus.IN_PROGRESS
    }

    def "task progress list is empty when no tasks found for user and topic"() {
        given:
        taskProgressRepository.findByUserIdAndTopicId("user-1", "topic-1") >> []

        when:
        def result = service.getTaskProgressList("user-1", "topic-1")

        then:
        result.taskProgressRsList.isEmpty()
    }

    def "task status is updated to the status from the event"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build()

        taskProgressRepository.findById("tp-1") >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.GOOD)
                .tagNames([])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        taskProgress.taskStatus == TaskStatus.SOLVED
    }

    def "evictAIInsights is called on first task solve"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.IN_PROGRESS)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.GOOD)
                .tagNames([])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        1 * cacheEvictionService.evictAIInsights("user-1")
    }

    def "both cache evictions are called even when task was already solved"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.SOLVED)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.EXCELLENT)
                .tagNames([])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        1 * cacheEvictionService.evictTopicProgress("user-1", "course-1")
        1 * cacheEvictionService.evictAIInsights("user-1")
    }

    def "getTaskProgressDetails returns existing record when found"() {
        given:
        def existing = new TaskProgressDetailsRs("tp-1", "user-1", "task-1", TaskStatus.IN_PROGRESS, null, null, null)
        taskProgressRepository.findByUserIdAndTaskId("user-1", "task-1") >> existing

        when:
        def result = service.getTaskProgressDetails("user-1", "task-1", "topic-1", "course-1")

        then:
        result == existing
        0 * taskProgressRepository.saveAndFlush(_)
        0 * taskProgressMapper.toTaskProgressDetailsRs(_)
    }

    def "getTaskProgressDetails creates NOT_STARTED record when none exists"() {
        given:
        taskProgressRepository.findByUserIdAndTaskId("user-1", "task-1") >> null

        def savedEntity = TaskProgress.builder()
                .userId("user-1")
                .taskId("task-1")
                .topicId("topic-1")
                .courseId("course-1")
                .taskStatus(TaskStatus.NOT_STARTED)
                .build()
        taskProgressRepository.saveAndFlush(_) >> savedEntity

        def mapped = new TaskProgressDetailsRs(null, "user-1", "task-1", TaskStatus.NOT_STARTED, null, null, null)
        taskProgressMapper.toTaskProgressDetailsRs(_) >> mapped

        when:
        def result = service.getTaskProgressDetails("user-1", "task-1", "topic-1", "course-1")

        then:
        result == mapped
        1 * taskProgressRepository.saveAndFlush({ TaskProgress tp ->
            tp.userId == "user-1" &&
            tp.taskId == "task-1" &&
            tp.topicId == "topic-1" &&
            tp.courseId == "course-1" &&
            tp.taskStatus == TaskStatus.NOT_STARTED
        })
    }

    def "getTaskProgressListByTaskIds returns empty list without hitting repository when taskIds is empty"() {
        when:
        def result = service.getTaskProgressListByTaskIds("user-1", [])

        then:
        result.taskProgressRsList.isEmpty()
        0 * taskProgressRepository.findByUserIdAndTaskIdIn(*_)
    }

    def "getTaskProgressListByTaskIds returns results from repository for non-empty taskIds"() {
        given:
        def rs1 = new TaskProgressSummaryRs("user-1", "task-1", "topic-1", TaskStatus.SOLVED, null, null)
        def rs2 = new TaskProgressSummaryRs("user-1", "task-2", "topic-1", TaskStatus.IN_PROGRESS, null, null)

        taskProgressRepository.findByUserIdAndTaskIdIn("user-1", ["task-1", "task-2"]) >> [rs1, rs2]

        when:
        def result = service.getTaskProgressListByTaskIds("user-1", ["task-1", "task-2"])

        then:
        result.taskProgressRsList.size() == 2
        result.taskProgressRsList[0].taskId() == "task-1"
        result.taskProgressRsList[0].taskStatus() == TaskStatus.SOLVED
        result.taskProgressRsList[1].taskId() == "task-2"
        result.taskProgressRsList[1].taskStatus() == TaskStatus.IN_PROGRESS
    }

    def "tag and topic progress are updated when task transitions from NOT_STARTED to SOLVED"() {
        given:
        def taskProgress = TaskProgress.builder()
                .userId("user-1")
                .courseId("course-1")
                .topicId("topic-1")
                .taskId("task-1")
                .taskStatus(TaskStatus.NOT_STARTED)
                .build()

        taskProgressRepository.findById(_) >> Optional.of(taskProgress)

        def event = TaskCompletedEvent.builder()
                .taskProgressId("tp-1")
                .taskStatus(TaskStatus.SOLVED)
                .codeQualityRating(CodeQualityRating.GOOD)
                .tagNames(["loops"])
                .build()

        when:
        service.processTaskCompletedEvent(event)

        then:
        1 * tagProgressService.processTagsFromCompletedTask("course-1", "topic-1", "user-1", ["loops"])
        1 * topicProgressService.updateTaskStatsInTopicProgress("user-1", "course-1", "topic-1")
    }

    def "autosave returns TaskProgressAutosaveRs from mapper"() {
        given:
        def rq = new CodeSnapshotRq(taskProgressId: "tp-1", lastSnapshot: "def solution(): pass")

        def existing = TaskProgress.builder()
                .userId("user-1").taskId("task-1").topicId("topic-1").courseId("course-1")
                .taskStatus(TaskStatus.IN_PROGRESS).build()
        def mapped = TaskProgress.builder().userId("user-1").taskId("task-1").lastSnapshot("def solution(): pass").build()
        def saved = TaskProgress.builder().userId("user-1").taskId("task-1").build()
        def expectedRs = new TaskProgressAutosaveRs("tp-1", LocalDateTime.now())

        taskProgressRepository.findById("tp-1") >> Optional.of(existing)
        taskProgressMapper.toTaskProgress(rq, existing) >> mapped
        taskProgressRepository.save(mapped) >> saved
        taskProgressMapper.toTaskProgressAutosaveRs(saved) >> expectedRs

        when:
        def result = service.autosave(rq)

        then:
        result == expectedRs
    }

    def "autosave throws NotFoundException when task progress is not found"() {
        given:
        def rq = new CodeSnapshotRq(taskProgressId: "unknown-id", lastSnapshot: "code")

        taskProgressRepository.findById("unknown-id") >> Optional.empty()

        when:
        service.autosave(rq)

        then:
        thrown(NotFoundException)
    }

    def "autosave calls mapper with the request and existing entity"() {
        given:
        def rq = new CodeSnapshotRq(taskProgressId: "tp-1", lastSnapshot: "def foo(): pass")

        def existing = TaskProgress.builder()
                .userId("user-1").taskId("task-1").topicId("topic-1").courseId("course-1")
                .taskStatus(TaskStatus.IN_PROGRESS).build()
        def mapped = TaskProgress.builder().userId("user-1").taskId("task-1").build()
        def saved = TaskProgress.builder().userId("user-1").taskId("task-1").build()

        taskProgressRepository.findById("tp-1") >> Optional.of(existing)
        taskProgressRepository.save(_) >> saved
        taskProgressMapper.toTaskProgressAutosaveRs(_) >> new TaskProgressAutosaveRs("tp-1", LocalDateTime.now())

        when:
        service.autosave(rq)

        then:
        1 * taskProgressMapper.toTaskProgress(rq, existing) >> mapped
    }

    def "autosave passes entity from repository save to toTaskProgressAutosaveRs"() {
        given:
        def rq = new CodeSnapshotRq(taskProgressId: "tp-1", lastSnapshot: "code")

        def existing = TaskProgress.builder()
                .userId("user-1").taskId("task-1").topicId("topic-1").courseId("course-1")
                .taskStatus(TaskStatus.IN_PROGRESS).build()
        def mapped = TaskProgress.builder().userId("user-1").taskId("task-1").lastSnapshot("code").build()
        def saved = TaskProgress.builder().userId("user-1").taskId("task-1").updatedAt(LocalDateTime.now()).build()

        taskProgressRepository.findById("tp-1") >> Optional.of(existing)
        taskProgressMapper.toTaskProgress(rq, existing) >> mapped
        taskProgressRepository.save(mapped) >> saved

        when:
        service.autosave(rq)

        then:
        1 * taskProgressMapper.toTaskProgressAutosaveRs(saved) >> new TaskProgressAutosaveRs("tp-1", LocalDateTime.now())
    }

    def "autosave works when lastSnapshot is null"() {
        given:
        def rq = new CodeSnapshotRq(taskProgressId: "tp-1", lastSnapshot: null)

        def existing = TaskProgress.builder()
                .userId("user-1").taskId("task-1").topicId("topic-1").courseId("course-1")
                .taskStatus(TaskStatus.IN_PROGRESS).build()
        def mapped = TaskProgress.builder().userId("user-1").taskId("task-1").lastSnapshot(null).build()
        def saved = TaskProgress.builder().userId("user-1").taskId("task-1").build()

        taskProgressRepository.findById("tp-1") >> Optional.of(existing)
        taskProgressMapper.toTaskProgress(rq, existing) >> mapped
        taskProgressRepository.save(mapped) >> saved
        taskProgressMapper.toTaskProgressAutosaveRs(saved) >> new TaskProgressAutosaveRs("tp-1", LocalDateTime.now())

        when:
        def result = service.autosave(rq)

        then:
        noExceptionThrown()
        result.taskProgressId() == "tp-1"
    }
}