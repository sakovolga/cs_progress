package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.QuizStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "quizzes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz extends IdentifiableEntity{

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quizzes_result_id", referencedColumnName = "id")
    @JsonBackReference
    private QuizzesResult quizzesResult;

//    @Column(name = "user_id", nullable = false)
//    private String userId;

    @Column(name = "is_active", nullable = false)
    private QuizStatus status;

    @Column(name = "number_of_completed_tests")
    private Integer numberOfCompletedTests;

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestResult> testResults = new ArrayList<>();
}
