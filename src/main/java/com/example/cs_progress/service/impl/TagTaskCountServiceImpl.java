package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TagsUpdatedEvent;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TagTaskCount;
import com.example.cs_progress.model.entity.TagTaskTopicCount;
import com.example.cs_progress.repository.TagTaskCountRepository;
import com.example.cs_progress.service.TagTaskCountService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagTaskCountServiceImpl extends BaseService implements TagTaskCountService {

    private final TagTaskCountRepository tagTaskCountRepository;

    @Override
    @Transactional
    public void update(@NonNull final TagsUpdatedEvent event) {
        log.info("Updating tag counts: courseId={}, topicId={}, tagsAdded={}, tagsRemoved={}",
                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(), event.getTagNamesRemoved());

        List<String> allTags = new ArrayList<>();
        allTags.addAll(event.getTagNamesAdded());
        allTags.addAll(event.getTagNamesRemoved());

        List<TagTaskCount> tagTaskCounts =
                tagTaskCountRepository.findByTagNameInAndCourseId(allTags, event.getCourseId());

        Map<String, TagTaskCount> tagTaskCountMap = tagTaskCounts.stream()
                .collect(Collectors.toMap(TagTaskCount::getTagName, ttc -> ttc));

        List<TagTaskCount> toSave = new ArrayList<>();

        for (String tag : allTags) {
            TagTaskCount tagTaskCount = tagTaskCountMap.computeIfAbsent(
                    tag,
                    t -> buildTagTaskCount(event.getCourseId(), t)
            );

            TagTaskTopicCount tagTaskTopicCount = tagTaskCount.getTopicCounts().stream()
                    .filter(tttc -> tttc.getTopicId().equals(event.getTopicId()))
                    .findFirst()
                    .orElseGet(() -> buildTagTaskTopicCount(tagTaskCount, event.getTopicId()));

            if (event.getTagNamesAdded().contains(tag)) {
                tagTaskTopicCount.incrementCount();
            } else if (event.getTagNamesRemoved().contains(tag)) {
                tagTaskTopicCount.decrementCount();
            }

            toSave.add(tagTaskCount);
        }

        tagTaskCountRepository.saveAll(toSave);
        log.info(
                "{} tags was increment, {} tags was decrement",
                event.getTagNamesAdded().size(), event.getTagNamesRemoved().size()
        );
    }

    private TagTaskCount buildTagTaskCount(String courseId, String tagName) {
        return TagTaskCount.builder()
                .courseId(courseId)
                .tagName(tagName)
                .build();
    }

    private TagTaskTopicCount buildTagTaskTopicCount(TagTaskCount tagTaskCount, String topicId) {
        TagTaskTopicCount tagTaskTopicCount = TagTaskTopicCount.builder()
                .tagTaskCount(tagTaskCount)
                .topicId(topicId)
                .count(0)
                .build();
        tagTaskCount.getTopicCounts().add(tagTaskTopicCount);
        return tagTaskTopicCount;
    }

}