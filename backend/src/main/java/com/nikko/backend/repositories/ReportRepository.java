package com.nikko.backend.repositories;

import com.nikko.backend.entities.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReportRepository extends JpaRepository<Report, UUID> {
    Optional<Report> findByQuizAttempt_Id(UUID quizAttemptId);
}