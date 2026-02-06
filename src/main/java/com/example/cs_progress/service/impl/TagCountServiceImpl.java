//package com.example.cs_progress.service.impl;
//
//import com.example.cs_common.dto.event.TaskStatsChangedEvent;
//import com.example.cs_common.util.BaseService;
//import com.example.cs_progress.model.entity.CourseOverview;
//import com.example.cs_progress.model.entity.TagCount;
//import com.example.cs_progress.model.entity.TagTopicCount;
//import com.example.cs_progress.service.TagCountService;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.HashSet;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//public class TagCountServiceImpl extends BaseService implements TagCountService {
//
////    private final TagCountRepository tagCountRepository;
//
//    @Override
//    @Transactional
//    public void update(@NonNull final TaskStatsChangedEvent event,
//                       @NonNull CourseOverview courseOverview) {
//        log.info("Updating tag counts: courseId={}, topicId={}, tagsAdded={}, tagsRemoved={}",
//                event.getCourseId(), event.getTopicId(), event.getTagNamesAdded(), event.getTagNamesRemoved());
//
//        Set<String> allTags = new HashSet<>();
//        allTags.addAll(event.getTagNamesAdded());
//        allTags.addAll(event.getTagNamesRemoved());
//
////        List<TagCount> tagCounts =
////                tagCountRepository.findByTagNameInAndCourseId(allTags, event.getCourseId());
//
//        List<TagCount> tagCounts = courseOverview.getTagCounts().stream()
//                .filter(tagCount -> allTags.contains(tagCount.getTagName()))
//                .toList();
//
//        Map<String, TagCount> tagTaskCountMap = tagCounts.stream()
//                .collect(Collectors.toMap(TagCount::getTagName, ttc -> ttc));
//
////        List<TagCount> toSave = new ArrayList<>();
//
//        for (String tag : allTags) {
//            TagCount tagCount = tagTaskCountMap.computeIfAbsent(
//                    tag,
//                    this::buildTagTaskCount
//            );
//
//            TagTopicCount tagTopicCount = tagCount.getTopicCounts().stream()
//                    .filter(tttc -> tttc.getTopicId().equals(event.getTopicId()))
//                    .findFirst()
//                    .orElseGet(() -> buildTagTaskTopicCount(tagCount, event.getTopicId()));
//
//            if (event.getTagNamesAdded().contains(tag)) {
//                tagTopicCount.incrementCount();
//            } else if (event.getTagNamesRemoved().contains(tag)) {
//                tagTopicCount.decrementCount();
//            }
//
//            if (tagCount.getCourseOverview() == null) {
//                tagCount.setCourseOverview(courseOverview);
//                courseOverview.getTagCounts().add(tagCount);
//            }
////            toSave.add(tagCount);
//        }
//
////        tagCountRepository.saveAll(toSave);
//        log.info(
//                "{} tags was increment, {} tags was decrement",
//                event.getTagNamesAdded().size(), event.getTagNamesRemoved().size()
//        );
//    }
//
//    private TagCount buildTagTaskCount(String tagName) {
//        return TagCount.builder()
//                .tagName(tagName)
//                .build();
//    }
//
//    private TagTopicCount buildTagTaskTopicCount(TagCount tagCount, String topicId) {
//        TagTopicCount tagTopicCount = TagTopicCount.builder()
//                .tagCount(tagCount)
//                .topicId(topicId)
//                .count(0)
//                .build();
//        tagCount.getTopicCounts().add(tagTopicCount);
//        return tagTopicCount;
//    }
//
//}