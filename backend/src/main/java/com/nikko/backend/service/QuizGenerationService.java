package com.nikko.backend.service;

import com.nikko.backend.dto.quiz.QuizAttemptResponseDto;
import com.nikko.backend.dto.quiz.QuizGenerationRequestDto;
import com.nikko.backend.dto.quiz.QuizQuestionResponseDto;
import com.nikko.backend.entities.*;
import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuestionType;
import com.nikko.backend.enums.QuizType;
import com.nikko.backend.exception.InvalidRequestException;
import com.nikko.backend.exception.ResourceNotFoundException;
import com.nikko.backend.repositories.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuizGenerationService {

    private final QuestionRepository questionRepository;
    private final SkillRepository skillRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final UserRepository userRepository;

    @Transactional
    public QuizAttemptResponseDto generateQuiz(QuizGenerationRequestDto request) {
        Set<UUID> skillIds = request.getSkillIds() != null ? request.getSkillIds(): Set.of();
        Set<AptitudeCategory> categories = request.getAptitudeCategories() != null
                ? request.getAptitudeCategories() : Set.of();

        if (skillIds.isEmpty() && categories.isEmpty()) {
            throw new InvalidRequestException(
                    "At least one skill or aptitude category must be selected");
        }

        User user = userRepository.findById(request.getId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getId()));

        Set<Skill> skills = skillIds.isEmpty() ? Set.of() : new HashSet<>(skillRepository.findAllById(skillIds));

        if (skills.size() != skillIds.size()) {
            Set<UUID> found = skills.stream().map(Skill::getId).collect(Collectors.toSet());
            Set<UUID> missing = new HashSet<>(skillIds);
            missing.removeAll(found);
            throw new ResourceNotFoundException("Skill(s) not found: " + missing);
        }

        QuizType quizType = !skillIds.isEmpty() && !categories.isEmpty()
                ? QuizType.COMBINED : (!skillIds.isEmpty() ? QuizType.SKILL : QuizType.APTITUDE);

        // One fetch-function per bucket, closed over its own skill/category key.
        List<Function<Set<UUID>, List<Question>>> bucketFetchers = new ArrayList<>();
        for (UUID skillId : skillIds) {
            bucketFetchers.add(excluded -> questionRepository
                    .findBySkills_IdAndQuestionTypeAndIdNotIn(skillId, QuestionType.SKILL, excluded));
        }
        for (AptitudeCategory category : categories) {
            bucketFetchers.add(excluded -> questionRepository
                    .findByAptitudeCategoryAndQuestionTypeAndIdNotIn(category, QuestionType.APTITUDE, excluded));
        }

        int totalBuckets = bucketFetchers.size();
        int total = request.getNumberOfQuestions();
        int base = total / totalBuckets;
        int remainder = total % totalBuckets;

        List<Question> selected = new ArrayList<>();
        Set<UUID> excludedIds = new HashSet<>();
        int shortFall = 0;

        for (int i = 0; i < totalBuckets; i++){
            int requestedCount = base + (i < remainder ? 1 : 0);
            List<Question> available = new ArrayList<>(bucketFetchers.get(i).apply(excludedIds));
            Collections.shuffle(available);

            int take = Math.min(requestedCount, available.size());
            List<Question> chosen = available.subList(0, take);
            selected.addAll(chosen);
            chosen.forEach(q -> excludedIds.add(q.getId()));

            shortFall += requestedCount - take;
        }

        if (shortFall > 0){
            List<Question> remaining = new ArrayList<>();
            for (Function<Set<UUID>, List<Question>> fetcher : bucketFetchers) {
                remaining.addAll(fetcher.apply(excludedIds));
            }

            Collections.shuffle(remaining);

            int fill = Math.min(shortFall, remaining.size());
            List<Question> filler = remaining.subList(0, fill);
            selected.addAll(filler);
            filler.forEach(q -> excludedIds.add((q.getId())));

        }


        if (selected.size() < total) {
            throw new InvalidRequestException(
                    "Not enough questions available: requested " + total
                            + " but only " + selected.size() + " match the selected skills/categories");
        }
        Collections.shuffle(selected);

        Quiz quiz = new Quiz();
        quiz.setSkills(skills);
        quiz.setAptitudeCategories(categories);
        quiz.setQuizType(quizType);

        List<QuizQuestion> quizQuestions = new ArrayList<>();
        for (int i = 0; i < selected.size(); i++){
            QuizQuestion qq = new QuizQuestion();
            qq.setQuiz(quiz);
            qq.setQuestion(selected.get(i));
            qq.setPosition(i + 1);
            quizQuestions.add(qq);
        }
        quiz.setQuizQuestions(quizQuestions);

        Quiz savedQuiz = quizRepository.save(quiz);

        QuizAttempt attempt = new QuizAttempt();
        attempt.setUser(user);
        attempt.setQuiz(savedQuiz);
        QuizAttempt savedAttempt = quizAttemptRepository.save(attempt);

        List<QuizQuestionResponseDto> questionDtos = savedQuiz.getQuizQuestions().stream()
                .map(qq -> new QuizQuestionResponseDto(
                        qq.getQuestion().getId(),
                        qq.getPosition(),
                        qq.getQuestion().getQuestionText(),
                        qq.getQuestion().getOptionA(),
                        qq.getQuestion().getOptionB(),
                        qq.getQuestion().getOptionC(),
                        qq.getQuestion().getOptionD()
                ))
                .collect(Collectors.toList());

        return new QuizAttemptResponseDto(
                savedAttempt.getId(),
                savedQuiz.getId(),
                savedQuiz.getQuizType(),
                savedAttempt.getStartedAt(),
                questionDtos
        );
    }

}
