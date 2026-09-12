package com.nikko.backend.dto.question;

import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuestionType;
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
public class QuestionResponseDto {

    private UUID id;
    private QuestionType questionType;
    private AptitudeCategory aptitudeCategory;
    private  String questionText;
    private String optionA;
    private String optionB;
    private String optionC;
    private String optionD;
    private String correctAnswer;
    private String explanation;
    private Set<UUID> skillIds;

}
