package com.example.cs_progress.mapper;

import com.example.cs_common.dto.common.CourseOverviewDto;
import com.example.cs_progress.model.entity.CourseOverview;

public interface CourseOverviewMapper {

    CourseOverview toCourseOverview(CourseOverviewDto courseOverviewDto);
}
