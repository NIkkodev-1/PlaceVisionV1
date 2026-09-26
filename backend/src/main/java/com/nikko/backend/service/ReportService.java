package com.nikko.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nikko.backend.dto.report.AiReportContentDto;
import com.nikko.backend.dto.report.ReportResponseDto;
import com.nikko.backend.entities.QuizAttempt;
import com.nikko.backend.entities.Report;
import com.nikko.backend.entities.UserAnswer;
import com.nikko.backend.enums.ReportStatus;
import com.nikko.backend.exception.InvalidRequestException;
import com.nikko.backend.exception.ResourceNotFoundException;
import com.nikko.backend.repositories.QuizAttemptRepository;
import com.nikko.backend.repositories.ReportRepository;
import com.nikko.backend.service.support.ScoreBreakdownCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final QuizAttemptRepository quizAttemptRepository;
    private final ReportRepository reportRepository;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public ReportResponseDto generateReport(UUID attemptId) {

        QuizAttempt attempt = quizAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Quiz attempt not found: " + attemptId));

        if (attempt.getCompletedAt() == null) {
            throw new InvalidRequestException("This quiz attempt has not been submitted yet");
        }

        Report report = reportRepository.findByQuizAttempt_Id(attemptId).orElse(null);

        if (report != null && report.getStatus() == ReportStatus.COMPLETED) {
            return toDto(report); // idempotent — no repeat OpenAI call
        }

        if (report == null) {
            report = new Report();
            report.setQuizAttempt(attempt);
        }

        List<UserAnswer> answers = attempt.getUserAnswers();

        List<ScoreBreakdownCalculator.SkillScore> skillScores =
                ScoreBreakdownCalculator.computeSkillScores(answers);
        List<ScoreBreakdownCalculator.AptitudeScore> aptitudeScores =
                ScoreBreakdownCalculator.computeAptitudeScores(answers);

        int total = answers.size();
        long correct = answers.stream().filter(UserAnswer::isCorrect).count();
        double overallPercentage = total == 0 ? 0.0 : (correct * 100.0) / total;

        Map<String, Double> skillBreakdown = skillScores.stream()
                .collect(Collectors.toMap(
                        ScoreBreakdownCalculator.SkillScore::skillName,
                        ScoreBreakdownCalculator.SkillScore::percentage,
                        (a, b) -> a, LinkedHashMap::new));

        Map<String, Double> aptitudeBreakdown = aptitudeScores.stream()
                .collect(Collectors.toMap(
                        s -> s.category().name(),
                        ScoreBreakdownCalculator.AptitudeScore::percentage,
                        (a, b) -> a, LinkedHashMap::new));

        try {
            String performanceSummaryJson = buildPerformanceSummaryJson(
                    attempt, overallPercentage, skillBreakdown, aptitudeBreakdown, answers);

            AiReportContentDto aiContent = openAiClient.generateReportContent(performanceSummaryJson);

            report.setOverallScore(overallPercentage);
            report.setSkillBreakdown(skillBreakdown);
            report.setAptitudeBreakdown(aptitudeBreakdown);
            report.setStrengths(aiContent.getStrengths());
            report.setWeaknesses(aiContent.getWeaknesses());
            report.setTopicsToImprove(aiContent.getTopicsToImprove());
            report.setRecommendations(aiContent.getRecommendations());
            report.setRecommendedSkills(aiContent.getRecommendedSkills());
            report.setRecommendedDifficulty(aiContent.getRecommendedDifficulty());
            report.setSuggestedDirection(aiContent.getSuggestedDirection());
            report.setStatus(ReportStatus.COMPLETED);
            report.setErrorMessage(null);

        } catch (Exception e) {
            report.setStatus(ReportStatus.FAILED);
            report.setErrorMessage(e.getMessage());
            // deliberately not rethrown — see explanation below
        }

        Report saved = reportRepository.save(report);
        return toDto(saved);
    }

    private String buildPerformanceSummaryJson(
            QuizAttempt attempt,
            double overallPercentage,
            Map<String, Double> skillBreakdown,
            Map<String, Double> aptitudeBreakdown,
            List<UserAnswer> answers) throws Exception {

        List<Map<String, Object>> incorrectQuestions = answers.stream()
                .filter(a -> !a.isCorrect())
                .map(a -> {
                    Map<String, Object> q = new LinkedHashMap<>();
                    q.put("questionText", a.getQuestion().getQuestionText());
                    q.put("selectedAnswer", a.getSelectedAnswer());
                    q.put("correctAnswer", a.getQuestion().getCorrectAnswer());
                    q.put("explanation", a.getQuestion().getExplanation());
                    return q;
                })
                .collect(Collectors.toList());

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("quizType", attempt.getQuiz().getQuizType().name());
        summary.put("overallPercentage", overallPercentage);
        summary.put("skillBreakdown", skillBreakdown);
        summary.put("aptitudeBreakdown", aptitudeBreakdown);
        summary.put("incorrectQuestions", incorrectQuestions);

        return objectMapper.writeValueAsString(summary);
    }

    private ReportResponseDto toDto(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getQuizAttempt().getId(),
                report.getStatus(),
                report.getOverallScore(),
                report.getSkillBreakdown(),
                report.getAptitudeBreakdown(),
                report.getStrengths(),
                report.getWeaknesses(),
                report.getTopicsToImprove(),
                report.getRecommendations(),
                report.getRecommendedSkills(),
                report.getRecommendedDifficulty(),
                report.getSuggestedDirection(),
                report.getErrorMessage(),
                report.getGeneratedAt()
        );
    }
}