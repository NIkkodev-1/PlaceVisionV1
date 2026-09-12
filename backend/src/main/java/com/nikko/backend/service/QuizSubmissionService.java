package com.nikko.backend.service;

import com.nikko.backend.dto.aptitude.AptitudeCategoryResultDto;
import com.nikko.backend.dto.question.QuestionResultDto;
import com.nikko.backend.dto.quiz.QuizResultResponseDto;
import com.nikko.backend.dto.skill.SkillResultDto;
import com.nikko.backend.dto.submission.AnswerSubmissionDto;
import com.nikko.backend.dto.submission.SubmitQuizRequestDto;
import com.nikko.backend.entities.*;
import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuestionType;
import com.nikko.backend.exception.InvalidRequestException;
import com.nikko.backend.exception.ResourceNotFoundException;
import com.nikko.backend.repositories.QuizAttemptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizSubmissionService {

    private final QuizAttemptRepository quizAttemptRepository;

    @Transactional
    public QuizResultResponseDto submitQuiz(UUID attemptId, SubmitQuizRequestDto request) {

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Quiz attempt not found: " + attemptId));

        if (attempt.getCompletedAt() != null) {
            throw new InvalidRequestException("This quiz attempt has already been submitted");
        }

        List<QuizQuestion> quizQuestions = attempt.getQuiz().getQuizQuestions();

        rejectDuplicateAnswers(request.getAnswers());

        Map<UUID, String> submittedAnswers = new HashMap<>();
        for (AnswerSubmissionDto answer : request.getAnswers()) {
            submittedAnswers.put(answer.getQuestionId(), answer.getSelectedAnswer());
        }

        Set<UUID> quizQuestionIds = quizQuestions.stream()
                .map(qq -> qq.getQuestion().getId())
                .collect(Collectors.toSet());

        Set<UUID> missing = new HashSet<>(quizQuestionIds);
        missing.removeAll(submittedAnswers.keySet());
        if (!missing.isEmpty()) {
            throw new InvalidRequestException("Missing answers for question(s): " + missing);
        }

        Set<UUID> unexpected = new HashSet<>(submittedAnswers.keySet());
        unexpected.removeAll(quizQuestionIds);
        if (!unexpected.isEmpty()) {
            throw new InvalidRequestException(
                    "Answer(s) submitted for question(s) not in this quiz: " + unexpected);
        }

        List<UserAnswer> userAnswers = new ArrayList<>();
        List<QuestionResultDto> questionResults = new ArrayList<>();

        Map<UUID, ScoreAccumulator> skillScores = new LinkedHashMap<>();
        Map<UUID, String> skillNamesById = new LinkedHashMap<>();
        Map<AptitudeCategory, ScoreAccumulator> aptitudeScores = new LinkedHashMap<>();

        int correctCount = 0;

        for (QuizQuestion qq : quizQuestions) {
            Question question = qq.getQuestion();
            String selected = submittedAnswers.get(question.getId()).trim().toUpperCase();
            boolean isCorrect = selected.equalsIgnoreCase(question.getCorrectAnswer());

            if (isCorrect) {
                correctCount++;
            }

            UserAnswer userAnswer = new UserAnswer();
            userAnswer.setQuizAttempt(attempt);
            userAnswer.setQuestion(question);
            userAnswer.setSelectedAnswer(selected);
            userAnswer.setCorrect(isCorrect);
            userAnswers.add(userAnswer);

            Set<String> skillNames = question.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toCollection(LinkedHashSet::new));

            questionResults.add(new QuestionResultDto(
                    question.getId(),
                    question.getQuestionText(),
                    selected,
                    question.getCorrectAnswer(),
                    isCorrect,
                    question.getExplanation(),
                    skillNames,
                    question.getAptitudeCategory() != null
                            ? question.getAptitudeCategory().name() : null
            ));

            if (question.getQuestionType() == QuestionType.SKILL) {
                for (Skill skill : question.getSkills()) {
                    skillNamesById.putIfAbsent(skill.getId(), skill.getName());
                    skillScores
                            .computeIfAbsent(skill.getId(), id -> new ScoreAccumulator())
                            .record(isCorrect);
                }
            } else if (question.getQuestionType() == QuestionType.APTITUDE
                    && question.getAptitudeCategory() != null) {
                aptitudeScores
                        .computeIfAbsent(question.getAptitudeCategory(), c -> new ScoreAccumulator())
                        .record(isCorrect);
            }
        }

        attempt.getUserAnswers().addAll(userAnswers);
        attempt.setScore(correctCount);
        attempt.setCompletedAt(LocalDateTime.now());
        quizAttemptRepository.save(attempt); // cascades to UserAnswer

        int totalQuestions = quizQuestions.size();
        double overallPercentage = totalQuestions == 0 ? 0.0 : (correctCount * 100.0) / totalQuestions;

        List<SkillResultDto> skillResults = skillScores.entrySet().stream()
                .map(entry -> new SkillResultDto(
                        entry.getKey(),
                        skillNamesById.get(entry.getKey()),
                        entry.getValue().correct,
                        entry.getValue().total,
                        entry.getValue().percentage()))
                .collect(Collectors.toList());

        List<AptitudeCategoryResultDto> aptitudeResults = aptitudeScores.entrySet().stream()
                .map(entry -> new AptitudeCategoryResultDto(
                        entry.getKey(),
                        entry.getValue().correct,
                        entry.getValue().total,
                        entry.getValue().percentage()))
                .collect(Collectors.toList());

        return new QuizResultResponseDto(
                attempt.getId(),
                attempt.getQuiz().getQuizType(),
                correctCount,
                totalQuestions,
                overallPercentage,
                attempt.getCompletedAt(),
                questionResults,
                skillResults,
                aptitudeResults
        );
    }

    private void rejectDuplicateAnswers(List<AnswerSubmissionDto> answers) {
        Set<UUID> seen = new HashSet<>();
        Set<UUID> duplicates = new HashSet<>();

        for (AnswerSubmissionDto answer : answers) {
            if (!seen.add(answer.getQuestionId())) {
                duplicates.add(answer.getQuestionId());
            }
        }

        if (!duplicates.isEmpty()) {
            throw new InvalidRequestException(
                    "Duplicate answers submitted for question(s): " + duplicates);
        }
    }

    private static class ScoreAccumulator {
        private int correct;
        private int total;

        void record(boolean isCorrect) {
            total++;
            if (isCorrect) {
                correct++;
            }
        }

        double percentage() {
            return total == 0 ? 0.0 : (correct * 100.0) / total;
        }
    }
}