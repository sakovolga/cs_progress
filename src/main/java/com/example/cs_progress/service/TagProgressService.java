package com.example.cs_progress.service;

import java.util.List;

public interface TagProgressService {

    void processTagsFromResolvedTestItem(String courseId,
                                         String topicId,
                                         String userId,
                                         List<String> tagNames,
                                         boolean isCorrect);
}
