package com.example.cs_progress.mapper.impl;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_common.dto.common.TagCountDto;
import com.example.cs_common.dto.common.TagTopicCountDto;
import com.example.cs_common.dto.common.TaskTopicCountDto;
import com.example.cs_common.util.BaseMapper;
import com.example.cs_progress.mapper.CourseOverviewMapper;
import com.example.cs_progress.model.entity.CourseOverview;
import com.example.cs_progress.model.entity.TagCount;
import com.example.cs_progress.model.entity.TagTopicCount;
import com.example.cs_progress.model.entity.TaskTopicCount;
import lombok.NonNull;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class CourseOverviewMapperImpl extends BaseMapper implements CourseOverviewMapper {

    @Override
    public CourseOverview toCourseOverview(@NonNull final CourseOverviewDto courseOverviewDto) {
        log.info("Mapping CourseOverviewDto with courseId: {} to CourseOverview", courseOverviewDto.getCourseId());

        CourseOverview courseOverview = CourseOverview.builder()
                .courseId(courseOverviewDto.getCourseId())
                .courseName(courseOverviewDto.getCourseName())
                .totalTopics(courseOverviewDto.getTotalTopics())
                .build();
        courseOverview.setTaskTopicCounts(
                courseOverviewDto.getTaskTopicCounts().stream()
                        .map(taskTopicCountDto -> toTaskTopicCount(taskTopicCountDto, courseOverview))
                        .toList());
        courseOverview.setTagCounts(
                courseOverviewDto.getTagCounts().stream()
                        .map(tagCountDto -> toTagCount(tagCountDto, courseOverview))
                        .collect(Collectors.toSet()));
        return courseOverview;
    }

    private TaskTopicCount toTaskTopicCount(TaskTopicCountDto dto, CourseOverview courseOverview) {
        return TaskTopicCount.builder()
                .topicId(dto.getTopicId())
                .count(dto.getCount())
                .courseOverview(courseOverview)
                .build();
    }

    private TagTopicCount toTagTopicCount(TagTopicCountDto tagTopicCountDto, TagCount tagCount) {
        return TagTopicCount.builder()
                .topicId(tagTopicCountDto.getTopicId())
                .count(tagTopicCountDto.getCount())
                .tagCount(tagCount)
                .build();
    }

    private TagCount toTagCount(TagCountDto tagCountDto, CourseOverview courseOverview) {
        TagCount tagCount = TagCount.builder()
                .tagName(tagCountDto.getTagName())
                .courseOverview(courseOverview)
                .build();
        tagCount.setTopicCounts(
                tagCountDto.getTopicCounts().stream()
                        .map(tagTopicCountDto -> toTagTopicCount(tagTopicCountDto, tagCount))
                        .collect(Collectors.toSet()));
        return tagCount;
    }
}
