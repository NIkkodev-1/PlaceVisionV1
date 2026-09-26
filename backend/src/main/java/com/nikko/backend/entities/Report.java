package com.nikko.backend.entities;

import com.nikko.backend.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "quiz_attempt_id", nullable = false, unique = true)
    private QuizAttempt quizAttempt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    private Double overallScore;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> skillBreakdown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Double> aptitudeBreakdown;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> strengths;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> weaknesses;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> topicsToImprove;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> recommendedSkills;

    private String recommendedDifficulty;

    @Column(columnDefinition = "TEXT")
    private String suggestedDirection;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    private LocalDateTime generatedAt;

    @PrePersist
    @PreUpdate
    public void setGeneratedAt() {
        generatedAt = LocalDateTime.now();
    }
}