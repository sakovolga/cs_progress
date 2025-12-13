package com.example.cs_progress.model.entity;

import com.example.cs_common.enums.TestStatus;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
import java.util.Objects;

@Entity
@Table(name = "tests_progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestProgress extends IdentifiableEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tests_result_id", referencedColumnName = "id")
    @JsonBackReference
    private TestsResult testsResult;

    @Column(name = "test_id", nullable = false)
    private String testId;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TestStatus status = TestStatus.ACTIVE;
//
//    @Column(name = "current_test_item_index")
//    private Integer currentTestItemIndex;

    @Column(name = "score")
    @Builder.Default
    private Double score = 0.0;

    @OneToMany(mappedBy = "testProgress", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<TestItemResult> testItemResults = new ArrayList<>();

//    private void addTestItemResult(TestItemResult testItemResult) {
//        testItemResults.add(testItemResult);
//        testItemResult.setTestProgress(this);
//    }

    public void updateScore() {
        this.score = testItemResults == null ? 0.0 :
                testItemResults.stream()
                        .map(TestItemResult::getScore)
                        .filter(Objects::nonNull)
                        .mapToDouble(Double::doubleValue)
                        .sum();
    }

}
