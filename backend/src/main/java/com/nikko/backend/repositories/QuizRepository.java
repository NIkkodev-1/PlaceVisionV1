package com.nikko.backend.repositories;

import com.nikko.backend.entities.Quiz;
import com.nikko.backend.entities.QuizAttempt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, UUID> {
}
