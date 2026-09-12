package com.nikko.backend.repositories;

import com.nikko.backend.entities.Question;
import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuestionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface QuestionRepository extends JpaRepository<Question, UUID> {

    List<Question> findBySkills_IdAndQuestionTypeAndIdNotIn(
            UUID skillId, QuestionType questionType, Set<UUID> excludedIds);

    List<Question> findByAptitudeCategoryAndQuestionTypeAndIdNotIn(
            AptitudeCategory aptitudeCategory, QuestionType questionType, Set<UUID> excludedIds);
}