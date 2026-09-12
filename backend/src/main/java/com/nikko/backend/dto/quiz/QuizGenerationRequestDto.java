package com.nikko.backend.dto.quiz;

import com.nikko.backend.enums.AptitudeCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
public class QuizGenerationRequestDto {

    @NotNull(message = "userId is required")
    private UUID id;

    private Set<UUID> skillIds;

    private Set<AptitudeCategory> aptitudeCategories;

    @NotNull(message = "numberOfQuestions is required")
    @Min(value = 1, message = "numberOfQuestions must be at least 1")
    private Integer numberOfQuestions;
}
