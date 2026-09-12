package com.nikko.backend.dto.quiz;

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
public class QuizAttemptResponseDto {

    private UUID quizAttemptId;
    private UUID quizId;
    private QuizType quizType;
    private LocalDateTime startedAt;
    private List<QuizQuestionResponseDto> questions;
}