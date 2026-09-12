package com.nikko.backend.dto.skill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SkillResultDto {

    private UUID skillId;
    private String skillName;
    private int correctAnswers;
    private int totalQuestions;
    private double percentage;
}