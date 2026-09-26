package com.nikko.backend.service.support;

import com.nikko.backend.entities.Question;
import com.nikko.backend.entities.Skill;
import com.nikko.backend.entities.UserAnswer;
import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuestionType;

import java.util.*;

public final class ScoreBreakdownCalculator {

    private ScoreBreakdownCalculator() {}

    public record SkillScore(UUID skillId, String skillName, int correct, int total) {
        public double percentage() {
            return total == 0 ? 0.0 : (correct * 100.0) / total;
        }
    }

    public record AptitudeScore(AptitudeCategory category, int correct, int total) {
        public double percentage() {
            return total == 0 ? 0.0 : (correct * 100.0) / total;
        }
    }

    public static List<SkillScore> computeSkillScores(List<UserAnswer> answers) {
        Map<UUID, String> namesById = new LinkedHashMap<>();
        Map<UUID, int[]> countsById = new LinkedHashMap<>();

        for (UserAnswer answer : answers) {
            Question question = answer.getQuestion();
            if (question.getQuestionType() != QuestionType.SKILL) continue;

            for (Skill skill : question.getSkills()) {
                namesById.putIfAbsent(skill.getId(), skill.getName());
                int[] counts = countsById.computeIfAbsent(skill.getId(), id -> new int[2]);
                counts[1]++;
                if (answer.isCorrect()) counts[0]++;
            }
        }

        List<SkillScore> results = new ArrayList<>();
        for (Map.Entry<UUID, int[]> entry : countsById.entrySet()) {
            int[] c = entry.getValue();
            results.add(new SkillScore(entry.getKey(), namesById.get(entry.getKey()), c[0], c[1]));
        }
        return results;
    }

    public static List<AptitudeScore> computeAptitudeScores(List<UserAnswer> answers) {
        Map<AptitudeCategory, int[]> countsByCategory = new LinkedHashMap<>();

        for (UserAnswer answer : answers) {
            Question question = answer.getQuestion();
            if (question.getQuestionType() != QuestionType.APTITUDE
                    || question.getAptitudeCategory() == null) continue;

            int[] counts = countsByCategory.computeIfAbsent(question.getAptitudeCategory(), c -> new int[2]);
            counts[1]++;
            if (answer.isCorrect()) counts[0]++;
        }

        List<AptitudeScore> results = new ArrayList<>();
        for (Map.Entry<AptitudeCategory, int[]> entry : countsByCategory.entrySet()) {
            int[] c = entry.getValue();
            results.add(new AptitudeScore(entry.getKey(), c[0], c[1]));
        }
        return results;
    }
}