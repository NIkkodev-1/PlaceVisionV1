package com.nikko.backend.service;

import com.nikko.backend.dto.question.QuestionRequestDto;
import com.nikko.backend.dto.question.QuestionResponseDto;
import com.nikko.backend.entities.Question;
import com.nikko.backend.entities.Skill;
import com.nikko.backend.enums.QuestionType;
import com.nikko.backend.exception.InvalidRequestException;
import com.nikko.backend.exception.ResourceNotFoundException;
import com.nikko.backend.repositories.QuestionRepository;
import com.nikko.backend.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final SkillRepository skillRepository;

    public QuestionResponseDto createQuestion(QuestionRequestDto request){
        validateQuestionType(request);
        validateCorrectAnswer(request.getCorrectAnswer());

        Set<Skill> skills = Set.of();

        if (request.getQuestionType() == QuestionType.SKILL) {
            skills = getSkills(request.getSkillIds());
        }

        Question question = new Question();

        question.setQuestionType(request.getQuestionType());
        question.setAptitudeCategory(request.getAptitudeCategory());

        question.setQuestionText(request.getQuestionText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());

        question.setCorrectAnswer(request.getCorrectAnswer().toUpperCase());
        question.setExplanation(request.getExplanation());

        question.setSkills(skills);

        Question savedQuestion = questionRepository.save(question);
        return toResponse(savedQuestion);

    }

    public List<QuestionResponseDto> getAllQuestions() {
        return questionRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponseDto getQuestionById(UUID id){

        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Question not Found: " + id));

        return toResponse(question);
    }

    public QuestionResponseDto updateQuestion(UUID id, QuestionRequestDto request){
        Question question = questionRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Question not Found: " + id));

        validateQuestionType(request);
        validateCorrectAnswer(request.getCorrectAnswer());

        Set<Skill> skills = Set.of();

        if (request.getQuestionType() == QuestionType.SKILL) {
            skills = getSkills(request.getSkillIds());
        }

        question.setQuestionType(request.getQuestionType());
        question.setAptitudeCategory(request.getAptitudeCategory());

        question.setQuestionText(request.getQuestionText());

        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());

        question.setCorrectAnswer(request.getCorrectAnswer().toUpperCase());

        question.setExplanation(request.getExplanation());

        question.setSkills(skills);

        Question updatedQuestion = questionRepository.save(question);

        return toResponse(updatedQuestion);
    }

    public void deleteQuestion(UUID id) {

        if (!questionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Question not found: " + id);
        }
        questionRepository.deleteById(id);
    }

    private void validateQuestionType(QuestionRequestDto request) {

        if (request.getQuestionType() == QuestionType.SKILL) {

            if (request.getSkillIds() == null || request.getSkillIds().isEmpty()) {
                throw new InvalidRequestException(
                        "Skill IDs are required for a skill question"
                );
            }

            if (request.getAptitudeCategory() != null) {
                throw new InvalidRequestException(
                        "Aptitude category should not be provided for a skill question"
                );
            }

        } else if (request.getQuestionType() == QuestionType.APTITUDE) {

            if (request.getAptitudeCategory() == null) {
                throw new InvalidRequestException(
                        "Aptitude category is required for an aptitude question"
                );
            }

            if (request.getSkillIds() != null && !request.getSkillIds().isEmpty()) {
                throw new InvalidRequestException(
                        "Skill IDs should not be provided for an aptitude question"
                );
            }
        } else {
            throw new InvalidRequestException("questionType must be SKILL or APTITUDE");
        }

    }

    private void validateCorrectAnswer(String correctAnswer) {

        if (correctAnswer == null ||
                !(correctAnswer.equalsIgnoreCase("A") ||
                correctAnswer.equalsIgnoreCase("B") ||
                correctAnswer.equalsIgnoreCase("C") ||
                correctAnswer.equalsIgnoreCase("D"))) {
            throw new InvalidRequestException("Correct answer must be A, B, C, or D");
        }
    }

    private Set<Skill> getSkills(Set<UUID> skillIds) {

        return skillIds.stream()
                .map(skillId -> skillRepository.findById(skillId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException("Skill not found: " + skillId)))
                .collect(Collectors.toSet());
    }

    private QuestionResponseDto toResponse(Question question) {
        Set<UUID> skillIds = question.getSkills()
                .stream()
                .map(Skill::getId)
                .collect(Collectors.toSet());

        return new QuestionResponseDto(
                question.getId(),
                question.getQuestionType(),
                question.getAptitudeCategory(),
                question.getQuestionText(),
                question.getOptionA(),
                question.getOptionB(),
                question.getOptionC(),
                question.getOptionD(),
                question.getCorrectAnswer(),
                question.getExplanation(),
                skillIds
        );
    }
}
