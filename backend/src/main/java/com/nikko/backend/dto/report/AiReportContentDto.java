package com.nikko.backend.dto.report;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiReportContentDto {
    private List<String> strengths;
    private List<String> weaknesses;
    private List<String> topicsToImprove;
    private String recommendations;
    private List<String> recommendedSkills;
    private String recommendedDifficulty;
    private String suggestedDirection;
}