package com.example.cs_progress.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "topic_overviews")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TopicOverview extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_overview_id", nullable = false)
    private CourseOverview courseOverview;

    @Column(name = "topic_id")
    private String topicId;

    @Column(name = "topic_name")
    private String topicName;

    @Column(name = "count")
    @Builder.Default
    private Integer count = 0; //Number of tasks in the topic

    // ========== Иерархия (3 уровня) ==========

    // Дедушка (уровень 0)
    @Column(name = "grandparent_id")
    private String grandparentId;

    @Column(name = "grandparent_name")
    private String grandparentName;

    @Column(name = "grandparent_order")  // ← Порядок дедушки!
    private Integer grandparentOrder;

    // Родитель (уровень 1)
    @Column(name = "parent_id")
    private String parentId;

    @Column(name = "parent_name")
    private String parentName;

    @Column(name = "parent_order")  // ← Порядок родителя!
    private Integer parentOrder;

    // Сам топик (уровень 2 - листья)
    @Column(name = "order_index", nullable = false)
    @Builder.Default
    private Integer orderIndex = 0;

    public void incrementCount() {
        this.count += 1;
    }

    public void decrementCount() {
        if (this.count > 0) {
            this.count -= 1;
        }
    }

}
