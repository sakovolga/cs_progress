package com.example.cs_progress.service;

import com.example.cs_common.dto.event.TagsUpdatedEvent;

public interface TagTaskCountService {

     void update(TagsUpdatedEvent event);
}
