package com.nikko.backend.controller;

import com.nikko.backend.dto.quiz.QuizAttemptResponseDto;
import com.nikko.backend.dto.quiz.QuizGenerationRequestDto;
import com.nikko.backend.service.QuizGenerationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/quizzes")
@RequiredArgsConstructor
public class QuizController {

    private final QuizGenerationService quizGenerationService;

    @PostMapping("/generate")
    public ResponseEntity<QuizAttemptResponseDto> generateQuiz(
            @Valid @RequestBody QuizGenerationRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(quizGenerationService.generateQuiz(request));
    }
}