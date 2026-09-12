package com.nikko.backend.dto.quiz;

import com.nikko.backend.dto.aptitude.AptitudeCategoryResultDto;
import com.nikko.backend.dto.question.QuestionResultDto;
import com.nikko.backend.dto.skill.SkillResultDto;
import com.nikko.backend.enums.QuizType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizResultResponseDto {

    private UUID quizAttemptId;
    private QuizType quizType;
    private Integer score;
    private Integer totalQuestions;
    private Double percentage;
    private LocalDateTime completedAt;
    private List<QuestionResultDto> questionResults;
    private List<SkillResultDto> skillResults;
    private List<AptitudeCategoryResultDto> aptitudeResults;
}