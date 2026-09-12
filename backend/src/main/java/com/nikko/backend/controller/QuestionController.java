package com.nikko.backend.controller;

import com.nikko.backend.dto.question.QuestionRequestDto;
import com.nikko.backend.dto.question.QuestionResponseDto;
import com.nikko.backend.service.QuestionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

    @PostMapping
    public ResponseEntity<QuestionResponseDto>  createQuestion(
            @Valid @RequestBody QuestionRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(questionService.createQuestion(request));
    }


    @GetMapping
    public ResponseEntity<List<QuestionResponseDto>> getAllQuestions() {
        return ResponseEntity.ok(questionService.getAllQuestions());
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestionById(@PathVariable UUID id){
        return ResponseEntity.ok(questionService.getQuestionById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable UUID id,
            @Valid @RequestBody QuestionRequestDto request) {

        return ResponseEntity.ok(
                questionService.updateQuestion(id, request)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteQuestionById(@PathVariable UUID id){
        questionService.deleteQuestion(id);

        return ResponseEntity.noContent().build();
    }
}
