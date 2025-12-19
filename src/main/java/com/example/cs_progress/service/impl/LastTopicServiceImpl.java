package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.LessonViewedEvent;
import com.example.cs_common.dto.key.LastTopicId;
import com.example.cs_progress.mapper.LastTopicMapper;
import com.example.cs_progress.repository.LastTopicRepository;
import com.example.cs_progress.service.LastTopicService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LastTopicServiceImpl extends BaseService implements LastTopicService {

    private final LastTopicRepository lastTopicRepository;
    private final LastTopicMapper lastTopicMapper;

    @Override
    @Transactional(readOnly = true)
    public String get(@NonNull final LastTopicId id) {
        log.info("Attempting to get current topicId in course with id: {} for user with id: {}",
                id.getCourseId(), id.getUserId());

        String lastTopicId = lastTopicRepository.findTopicIdById(id).orElse(null);

        if (lastTopicId != null) {
            log.info("TopicId: {} was received", lastTopicId);
            return lastTopicId;
        } else {
            log.info("There is no current topic for the userId: {}", id.getUserId());
            return null;
        }
    }

    @Override
    @Transactional
    public void saveOrUpdateLastTopic(@NonNull final LessonViewedEvent event) {
        log.info(
                "Processing lesson viewed event for user: {}, course: {}, topic: {}",
                event.getLastTopicId().getUserId(), event.getLastTopicId().getCourseId(), event.getTopicId()
        );

        try {
            lastTopicRepository.save(lastTopicMapper.toLastTopic(event));

            log.info(
                    "Saved last topic for user: {}, course: {}, topic: {}",
                    event.getLastTopicId().getUserId(), event.getLastTopicId().getCourseId(), event.getTopicId()
            );

        } catch (Exception e) {
            log.error(
                    "Failed to save/update last topic for user: {}, course: {}, topic: {}",
                    event.getLastTopicId().getUserId(), event.getLastTopicId().getCourseId(), event.getTopicId(), e
            );
            throw e; // для retry
        }
    }

}
