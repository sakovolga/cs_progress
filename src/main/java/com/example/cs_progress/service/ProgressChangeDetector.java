package com.example.cs_progress.service;

import lombok.NonNull;

import java.time.LocalDateTime;

public interface ProgressChangeDetector {
    boolean hasChanges(@NonNull String userId, @NonNull LocalDateTime since);
}