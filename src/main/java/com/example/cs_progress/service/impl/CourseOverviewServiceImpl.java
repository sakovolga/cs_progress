package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_common.dto.common.TagCountDto;
import com.example.cs_common.dto.common.TagTopicCountDto;
import com.example.cs_common.dto.common.TopicOverviewDto;
import com.example.cs_common.dto.response.CourseOverviewSynchronizationRs;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.repository.CourseOverviewRepository;
import com.example.cs_progress.repository.TagCountRepository;
import com.example.cs_progress.repository.TagTopicCountRepository;
import com.example.cs_progress.repository.TopicOverviewRepository;
import com.example.cs_progress.service.CacheEvictionService;
import com.example.cs_progress.service.CourseOverviewService;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseOverviewServiceImpl extends BaseService implements CourseOverviewService {

    private final CourseOverviewRepository courseOverviewRepository;
//    private final TagCountService tagCountService;
//    private final TopicOverviewService topicOverviewService;
    private final CacheEvictionService cacheEvictionService;
    private final TagTopicCountRepository tagTopicCountRepository;
    private final TagCountRepository tagCountRepository;
    private final TopicOverviewRepository topicOverviewRepository;
    private final EntityManager entityManager;
//    private final DataSource dataSource;

//    @Override
//    @Transactional
//    public void handleTaskStatsChangedEvent(@NonNull final TaskStatsChangedEvent event) {
//        log.info(
//                "Handling TaskStatsChangedEvent for courseId: {}, topicId: {}, tagNamesAdded: {}," +
//                        "tagNamesRemoved: {}, isTaskCreated: {}, isTaskDeleted: {}",
//                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(),
//                event.getTagNamesRemoved(), event.getIsTaskCreated(), event.getIsTaskDeleted()
//        );
//
//        CourseOverview courseOverview = courseOverviewRepository.findByCourseId(event.getCourseId())
//                .orElse(CourseOverview.builder()
//                        .courseId(event.getCourseId())
//                        .build());
//
//        tagCountService.update(event, courseOverview);
//        topicOverviewService.updateTaskTopicCount(event, courseOverview);
//
//        courseOverviewRepository.save(courseOverview);
//        cacheEvictionService.evictCourseOverview(event.getCourseId());
//    }

    @Override
    @Transactional
    public CourseOverviewSynchronizationRs synchronizeCourseOverview(@NonNull final CourseOverviewDto courseOverviewDto) {
        log.info("Synchronizing CourseOverview for courseId: {}", courseOverviewDto.getCourseId());

        String courseId = courseOverviewDto.getCourseId();

        // 1. Bulk DELETE
        tagTopicCountRepository.deleteByTagCountCourseOverviewCourseId(courseId);
        tagCountRepository.deleteByCourseOverviewCourseId(courseId);
        topicOverviewRepository.deleteByCourseOverviewCourseId(courseId);
        courseOverviewRepository.deleteByCourseId(courseId);
        entityManager.flush();

        cacheEvictionService.evictCourseOverview(courseId);

        // 2. Создаём CourseOverview
        CourseOverview course = CourseOverview.builder()
                .courseId(courseOverviewDto.getCourseId())
                .courseName(courseOverviewDto.getCourseName())
                .totalTopics(courseOverviewDto.getTotalTopics())
                .build();

        CourseOverview courseOverview = courseOverviewRepository.save(course);
        entityManager.flush();  // ← КРИТИЧНО: Flush чтобы CourseOverview попал в БД

        // 3. Получаем JDBC connection из того же EntityManager
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            // 4. JDBC Batch Insert в том же connection
            if (courseOverviewDto.getTagCounts() != null && !courseOverviewDto.getTagCounts().isEmpty()) {
                batchInsertTagCounts(connection, courseOverview, courseOverviewDto.getTagCounts());
            }

            if (courseOverviewDto.getTopicOverviewDtos() != null && !courseOverviewDto.getTopicOverviewDtos().isEmpty()) {
                batchInsertTopicOverviews(connection, courseOverview, courseOverviewDto.getTopicOverviewDtos());
            }
        });

        log.info("CourseOverview synchronized successfully for courseId: {}", courseOverview.getCourseId());

        return CourseOverviewSynchronizationRs.builder()
                .courseId(courseOverview.getCourseId())
                .lastUpdated(courseOverview.getUpdatedAt())
                .build();
    }

    private void batchInsertTagCounts(Connection conn, CourseOverview courseOverview, Set<TagCountDto> tagDtos) throws SQLException {
        List<TagCountDto> tagDtosList = new ArrayList<>(tagDtos);

        // 1. Batch insert TagCounts
        String tagCountSql = "INSERT INTO tag_counts (id, course_overview_id, tag_name) VALUES (?, ?, ?)";

        Map<String, String> tagIdMap = new HashMap<>();

        try (PreparedStatement ps = conn.prepareStatement(tagCountSql)) {
            for (TagCountDto dto : tagDtosList) {
                String id = UUID.randomUUID().toString();
                tagIdMap.put(dto.getTagName(), id);

                ps.setString(1, id);
                ps.setString(2, courseOverview.getId());
                ps.setString(3, dto.getTagName());
                ps.addBatch();
            }

            int[] result = ps.executeBatch();
            log.info("Batch inserted {} TagCounts", result.length);
        }

        // 2. Batch insert TagTopicCounts
        String topicCountSql = "INSERT INTO tag_topic_counts (id, tag_count_id, topic_id, count) VALUES (?, ?, ?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(topicCountSql)) {
            for (TagCountDto dto : tagDtosList) {
                String tagCountId = tagIdMap.get(dto.getTagName());
                Set<TagTopicCountDto> topicDtos = dto.getTopicCounts();

                if (topicDtos != null && !topicDtos.isEmpty()) {
                    for (TagTopicCountDto topicDto : topicDtos) {
                        ps.setString(1, UUID.randomUUID().toString());
                        ps.setString(2, tagCountId);
                        ps.setString(3, topicDto.getTopicId());
                        ps.setInt(4, topicDto.getCount());
                        ps.addBatch();
                    }
                }
            }

            int[] result = ps.executeBatch();
            log.info("Batch inserted {} TagTopicCounts", result.length);
        }
    }

    private void batchInsertTopicOverviews(Connection conn, CourseOverview courseOverview, List<TopicOverviewDto> topicDtos) throws SQLException {
        String sql = """
        INSERT INTO topic_overviews (
            id, course_overview_id, topic_id, topic_name,
            grandparent_id, grandparent_name, grandparent_order,
            parent_id, parent_name, parent_order,
            order_index, count
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (TopicOverviewDto dto : topicDtos) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, courseOverview.getId());
                ps.setString(3, dto.getTopicId());
                ps.setString(4, dto.getTopicName());
                ps.setString(5, dto.getGrandparentId());
                ps.setString(6, dto.getGrandparentName());
                ps.setObject(7, dto.getGrandparentOrder());
                ps.setString(8, dto.getParentId());
                ps.setString(9, dto.getParentName());
                ps.setObject(10, dto.getParentOrder());
                ps.setInt(11, dto.getOrderIndex());
                ps.setInt(12, dto.getCount());
                ps.addBatch();
            }

            int[] result = ps.executeBatch();
            log.info("Batch inserted {} TopicOverviews", result.length);
        }
    }
}
