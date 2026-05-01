package com.example.cs_progress.service;

import java.util.List;

public interface TagProgressService {

    void processTagsFromCompletedTask(String courseId,
                                      String topicId,
                                      String userId,
                                      List<String> tagNames);
}
