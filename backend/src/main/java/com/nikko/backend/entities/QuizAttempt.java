package com.nikko.backend.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @OneToMany(
            mappedBy = "quizAttempt",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<UserAnswer> userAnswers = new ArrayList<>();

    @OneToOne(fetch = LAZY, mappedBy = "quizAttempt" )
    private Report report;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    private Integer score;

    @PrePersist
    public void prePersist() {
        startedAt = LocalDateTime.now();
    }
}
