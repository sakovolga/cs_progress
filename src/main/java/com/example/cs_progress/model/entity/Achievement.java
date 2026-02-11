//package com.example.cs_progress.model.entity;
//
//import com.example.cs_common.enums.AchievementCategory;
//import com.example.cs_common.enums.AchievementTier;
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.EnumType;
//import jakarta.persistence.Enumerated;
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
//@Table(name = "achievements")
//@Getter
//@Setter
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Achievement extends IdentifiableEntity {
//
//    /**
//     * Уникальный ключ достижения (например: "first_lesson", "problem_solver_25")
//     */
//    @Column(name = "achievement_key", nullable = false, unique = true, length = 100)
//    private String key;
//
//    /**
//     * Название достижения
//     */
//    @Column(name = "title", nullable = false, length = 100)
//    private String title;
//
//    /**
//     * Описание достижения
//     */
//    @Column(name = "description", length = 500)
//    private String description;
//
//    /**
//     * Категория достижения
//     */
//    @Enumerated(EnumType.STRING)
//    @Column(name = "category", nullable = false, length = 30)
//    private AchievementCategory category;
//
//    /**
//     * Уровень (редкость) достижения
//     */
//    @Enumerated(EnumType.STRING)
//    @Column(name = "tier", nullable = false, length = 20)
//    private AchievementTier tier;
//
//    /**
//     * Название иконки (emoji или название SVG)
//     */
//    @Column(name = "icon_name", length = 50)
//    private String iconName;
//
////    /**
////     * Требуемое количество для разблокировки (для прогрессивных достижений)
////     * Например: 25 для "Решил 25 задач"
////     */
////    @Column(name = "required_count")
////    private Integer requiredCount;
////
////    /**
////     * Порядок отображения
////     */
////    @Column(name = "display_order")
////    @Builder.Default
////    private Integer displayOrder = 0;
//
//
//    @Column(name = "created_at", nullable = false, updatable = false)
//    private LocalDateTime createdAt;
//
//    @PrePersist
//    protected void onCreate() {
//        createdAt = LocalDateTime.now();
//    }
//
//}
