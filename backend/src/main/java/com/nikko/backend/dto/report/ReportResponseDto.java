package com.nikko.backend.dto.report;

import com.nikko.backend.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDto {
    private UUID reportId;
    private UUID quizAttemptId;
    private ReportStatus status;
    private Double overallScore;
    private Map<String, Double> skillBreakdown;
    private Map<String, Double> aptitudeBreakdown;
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> topicsToImprove;
    private String recommendations;
    private List<String> recommendedSkills;
    private String recommendedDifficulty;
    private String suggestedDirection;
    private String errorMessage;
    private LocalDateTime generatedAt;
}