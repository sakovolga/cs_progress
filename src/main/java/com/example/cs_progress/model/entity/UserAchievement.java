//package com.example.cs_progress.model.entity;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.FetchType;
//import jakarta.persistence.JoinColumn;
//import jakarta.persistence.ManyToOne;
//import jakarta.persistence.PrePersist;
//import jakarta.persistence.Table;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.time.LocalDateTime;
//
//@Entity
//@Table(name = "user_achievements")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class UserAchievement extends IdentifiableEntity {
//
//    @Column(name = "user_id", nullable = false, length = 100)
//    private String userId;
//
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "achievement_id", nullable = false)
//    private Achievement achievement;
//
//    @Column(name = "progress")
//    @Builder.Default
//    private Integer progress = 0;
//
//    @Column(name = "unlocked_at")
//    private LocalDateTime unlockedAt;
//
//    /**
//     * Просмотрел ли пользователь это достижение
//     * Используется для бейджика "NEW!"
//     */
//    @Column(name = "is_viewed")
//    @Builder.Default
//    private Boolean isViewed = false;
//
//    @PrePersist
//    protected void onCreate() {
//        unlockedAt = LocalDateTime.now();
//    }
//
//
//    // ========== Бизнес-методы ==========
////
////    /**
////     * Проверить, разблокировано ли достижение
////     */
////    public boolean isUnlocked() {
////        return unlockedAt != null;
////    }
//
////    /**
////     * Разблокировать достижение
////     */
////    public void unlock() {
////        if (unlockedAt == null) {
////            unlockedAt = LocalDateTime.now();
////            isViewed = false; // Новое достижение ещё не просмотрено
////        }
////    }
////
////    /**
////     * Обновить прогресс
////     */
////    public void updateProgress(int newProgress) {
////        this.progress = newProgress;
////    }
////
////    /**
////     * Отметить как просмотренное
////     */
////    public void markAsViewed() {
////        this.isViewed = true;
////    }
//
//
//}
//
