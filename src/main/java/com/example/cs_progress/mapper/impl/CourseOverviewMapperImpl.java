//package com.example.cs_progress.mapper.impl;
//
//import com.example.cs_common.dto.common.CourseOverviewDto;
//import com.example.cs_common.dto.common.TagCountDto;
//import com.example.cs_common.dto.common.TagTopicCountDto;
//import com.example.cs_common.dto.common.TopicOverviewDto;
//import com.example.cs_common.dto.response.TopicOverviewRs;
//import com.example.cs_common.util.BaseMapper;
//import com.example.cs_progress.mapper.CourseOverviewMapper;
//import com.example.cs_progress.model.entity.CourseOverview;
//import com.example.cs_progress.model.entity.TagCount;
//import com.example.cs_progress.model.entity.TagTopicCount;
//import com.example.cs_progress.model.entity.TopicOverview;
//import lombok.NonNull;
//import org.springframework.stereotype.Component;
//
//import java.util.stream.Collectors;
//
//@Component
//public class CourseOverviewMapperImpl extends BaseMapper implements CourseOverviewMapper {
//
//    @Override
//    public CourseOverview toCourseOverview(@NonNull final CourseOverviewDto courseOverviewDto) {
//        log.info("Mapping CourseOverviewDto with courseId: {} to CourseOverview", courseOverviewDto.getCourseId());
//
//        CourseOverview courseOverview = CourseOverview.builder()
//                .courseId(courseOverviewDto.getCourseId())
//                .courseName(courseOverviewDto.getCourseName())
//                .totalTopics(courseOverviewDto.getTotalTopics())
//                .build();
//        courseOverview.setTopicOverviews(
//                courseOverviewDto.getTopicOverviewDtos().stream()
//                        .map(topicOverviewDto -> toTopicOverview(topicOverviewDto, courseOverview))
//                        .toList());
//        courseOverview.setTagCounts(
//                courseOverviewDto.getTagCounts().stream()
//                        .map(tagCountDto -> toTagCount(tagCountDto, courseOverview))
//                        .collect(Collectors.toSet()));
//        return courseOverview;
//    }
//
//    @Override
//    public TopicOverviewRs toTopicOverviewRs(@NonNull final TopicOverview topicOverview) {
//        log.info("Mapping TopicOverview with topicId: {} to TopicOverviewRs", topicOverview.getTopicId());
//
//        return TopicOverviewRs.builder()
//                .topicId(topicOverview.getTopicId())
//                .topicName(topicOverview.getTopicName())
//                .orderIndex(topicOverview.getOrderIndex())
//                .grandparentId(topicOverview.getGrandparentId())
//                .grandparentName(topicOverview.getGrandparentName())
//                .grandparentOrder(topicOverview.getGrandparentOrder())
//                .parentId(topicOverview.getParentId())
//                .parentName(topicOverview.getParentName())
//                .parentOrder(topicOverview.getParentOrder())
//                .build();
//    }
//
//    private TopicOverview toTopicOverview(TopicOverviewDto dto, CourseOverview courseOverview) {
//        return TopicOverview.builder()
//                .topicId(dto.getTopicId())
//                .topicName(dto.getTopicName())
//                .count(dto.getCount())
//                .orderIndex(dto.getOrderIndex())
//                .parentId(dto.getParentId())
//                .parentName(dto.getParentName())
//                .parentOrder(dto.getParentOrder())
//                .grandparentId(dto.getGrandparentId())
//                .grandparentName(dto.getGrandparentName())
//                .grandparentOrder(dto.getParentOrder())
//                .courseOverview(courseOverview)
//                .build();
//    }
//
//    private TagTopicCount toTagTopicCount(TagTopicCountDto tagTopicCountDto, TagCount tagCount) {
//        return TagTopicCount.builder()
//                .topicId(tagTopicCountDto.getTopicId())
//                .count(tagTopicCountDto.getCount())
//                .tagCount(tagCount)
//                .build();
//    }
//
//    private TagCount toTagCount(TagCountDto tagCountDto, CourseOverview courseOverview) {
//        TagCount tagCount = TagCount.builder()
//                .tagName(tagCountDto.getTagName())
//                .courseOverview(courseOverview)
//                .build();
//        tagCount.setTopicCounts(
//                tagCountDto.getTopicCounts().stream()
//                        .map(tagTopicCountDto -> toTagTopicCount(tagTopicCountDto, tagCount))
//                        .collect(Collectors.toSet()));
//        return tagCount;
//    }
//}
