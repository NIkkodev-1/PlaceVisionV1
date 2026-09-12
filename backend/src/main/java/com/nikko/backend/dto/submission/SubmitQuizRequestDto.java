package com.nikko.backend.dto.submission;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubmitQuizRequestDto {

    @NotEmpty(message = "At least one answer is required")
    @Valid
    private List<AnswerSubmissionDto> answers;
}