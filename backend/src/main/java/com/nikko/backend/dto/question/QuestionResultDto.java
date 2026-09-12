package com.nikko.backend.dto.question;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuestionResultDto {

    private UUID questionId;
    private String questionText;
    private String selectedAnswer;
    private String correctAnswer;
    private boolean correct;
    private String explanation;
    private Set<String> skillNames;
    private String aptitudeCategory; // null for SKILL questions
}