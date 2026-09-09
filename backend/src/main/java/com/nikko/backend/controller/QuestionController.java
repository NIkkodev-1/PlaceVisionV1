package com.nikko.backend.controller;

import com.nikko.backend.entities.Question;
import com.nikko.backend.service.QuestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/placevision")
@RequiredArgsConstructor
public class QuestionController {

    private final QuestionService questionService;

//    @PostMapping("/questions")
//    public Question createQuestion(@RequestBody Question question){
//        return questionService.createQuestion(question);
//    }
//
//    @GetMapping("/questions")
//    public List<Question> getAllQuestions() {
//        return questionService.getAllQuestions();
//    }
//
//    @GetMapping("/question/{id}")
//    public Question getQuestionById(@PathVariable UUID id){
//        return questionService.getQuestionById(id);
//    }
//
//    @DeleteMapping("/question/{id}")
//    public void deleteQuestionById(@PathVariable UUID id){
//        questionService.deleteQuestion(id);
//    }
}
