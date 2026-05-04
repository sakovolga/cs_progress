package com.example.cs_progress.service

import com.example.cs_common.dto.event.TaskCompletedEvent
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
}