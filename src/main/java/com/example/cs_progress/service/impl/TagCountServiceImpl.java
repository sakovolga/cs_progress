package com.example.cs_progress.service.impl;

import com.example.cs_common.dto.event.TaskStatsChangedEvent;
import com.example.cs_common.util.BaseService;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagTopicCount;
import com.example.cs_progress.repository.TagCountRepository;
import com.example.cs_progress.service.TagCountService;
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
public class TagCountServiceImpl extends BaseService implements TagCountService {

    private final TagCountRepository tagCountRepository;

    @Override
    @Transactional
    public void update(@NonNull final TaskStatsChangedEvent event) {
        log.info("Updating tag counts: courseId={}, topicId={}, tagsAdded={}, tagsRemoved={}",
                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(), event.getTagNamesRemoved());

        List<String> allTags = new ArrayList<>();
        allTags.addAll(event.getTagNamesAdded());
        allTags.addAll(event.getTagNamesRemoved());

        List<TagCount> tagCounts =
                tagCountRepository.findByTagNameInAndCourseId(allTags, event.getCourseId());

        Map<String, TagCount> tagTaskCountMap = tagCounts.stream()
                .collect(Collectors.toMap(TagCount::getTagName, ttc -> ttc));

        List<TagCount> toSave = new ArrayList<>();

        for (String tag : allTags) {
            TagCount tagCount = tagTaskCountMap.computeIfAbsent(
                    tag,
                    t -> buildTagTaskCount(event.getCourseId(), t)
            );

            TagTopicCount tagTopicCount = tagCount.getTopicCounts().stream()
                    .filter(tttc -> tttc.getTopicId().equals(event.getTopicId()))
                    .findFirst()
                    .orElseGet(() -> buildTagTaskTopicCount(tagCount, event.getTopicId()));

            if (event.getTagNamesAdded().contains(tag)) {
                tagTopicCount.incrementCount();
            } else if (event.getTagNamesRemoved().contains(tag)) {
                tagTopicCount.decrementCount();
            }

            toSave.add(tagCount);
        }

        tagCountRepository.saveAll(toSave);
        log.info(
                "{} tags was increment, {} tags was decrement",
                event.getTagNamesAdded().size(), event.getTagNamesRemoved().size()
        );
    }

    private TagCount buildTagTaskCount(String courseId, String tagName) {
        return TagCount.builder()
                .courseId(courseId)
                .tagName(tagName)
                .build();
    }

    private TagTopicCount buildTagTaskTopicCount(TagCount tagCount, String topicId) {
        TagTopicCount tagTopicCount = TagTopicCount.builder()
                .tagCount(tagCount)
                .topicId(topicId)
                .count(0)
                .build();
        tagCount.getTopicCounts().add(tagTopicCount);
        return tagTopicCount;
    }

}