package com.nikko.backend.dto.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponseDto {

    private UUID questionId;
    private Integer position;
    private String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
}