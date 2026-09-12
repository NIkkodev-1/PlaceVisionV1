package com.nikko.backend.dto.aptitude;

import com.nikko.backend.enums.AptitudeCategory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AptitudeCategoryResultDto {

    private AptitudeCategory aptitudeCategory;
    private int correctAnswers;
    private int totalQuestions;
    private double percentage;
}