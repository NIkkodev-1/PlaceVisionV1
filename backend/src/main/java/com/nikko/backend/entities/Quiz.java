package com.nikko.backend.entities;

import com.nikko.backend.enums.AptitudeCategory;
import com.nikko.backend.enums.QuizType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToMany
    @JoinTable(
            name = "quiz_skills",
            joinColumns = @JoinColumn(name = "quiz_id"),
            inverseJoinColumns = @JoinColumn(name = "skill_id")
    )
    private Set<Skill> skills = new HashSet<>();

    //Aptitude combination QUIZ
    //Test
    @ElementCollection(targetClass = AptitudeCategory.class)
    @CollectionTable(
            name = "quiz_aptitude_categories",
            joinColumns = @JoinColumn(name = "quiz_id")
    )
    @Enumerated(EnumType.STRING)
    @Column(name = "aptitude_category")
    private Set<AptitudeCategory> aptitudeCategories = new HashSet<>();

    @OneToMany(mappedBy = "quiz", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuizType quizType;

    @OneToMany(mappedBy = "quiz")
    private List<QuizAttempt> quizAttempts = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
