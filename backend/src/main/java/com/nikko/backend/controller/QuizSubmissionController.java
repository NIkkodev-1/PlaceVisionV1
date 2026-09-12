package com.nikko.backend.controller;

import com.nikko.backend.dto.quiz.QuizResultResponseDto;
import com.nikko.backend.dto.submission.SubmitQuizRequestDto;
import com.nikko.backend.service.QuizSubmissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes/attempts")
@RequiredArgsConstructor
public class QuizSubmissionController {

    private final QuizSubmissionService quizSubmissionService;

    @PostMapping("/{attemptId}/submit")
    public ResponseEntity<QuizResultResponseDto> submitQuiz(
            @PathVariable UUID attemptId,
            @Valid @RequestBody SubmitQuizRequestDto request) {
        return ResponseEntity.ok(quizSubmissionService.submitQuiz(attemptId, request));
    }
}
