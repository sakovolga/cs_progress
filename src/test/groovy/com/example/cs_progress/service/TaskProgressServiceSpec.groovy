package com.example.cs_progress.service

import com.example.cs_common.dto.event.TaskCompletedEvent
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
}