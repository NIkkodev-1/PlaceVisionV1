package com.nikko.backend.service;

import com.nikko.backend.dto.SkillRequestDto;
import com.nikko.backend.dto.SkillResponseDto;
import com.nikko.backend.entities.Skill;
import com.nikko.backend.repositories.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillRepository skillRepository;

    public SkillResponseDto createSkill(SkillRequestDto request) {
        if (skillRepository.existsByName(request.getSkill())) {
            throw new IllegalArgumentException("Skill already exists " + request.getSkill());
        }

        Skill skill = new Skill();
        skill.setName(request.getSkill());

        return toResponse(skillRepository.save(skill));
    }

    public List<SkillResponseDto> getAllSkills() {
        return skillRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

    }

    public SkillResponseDto getSkillById(UUID id) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(("Skill not found: " + id)));
        return toResponse(skill);
    }

    public void deleteSkill(UUID id) {
        if (!skillRepository.existsById(id)) {
            throw new IllegalArgumentException("Skill not found: " + id);
        }
        skillRepository.deleteById(id);
    }

    public SkillResponseDto updateSkill(UUID id, SkillRequestDto request) {
        Skill skill = skillRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(("Skill not found: " + id)));

        skillRepository.findByName(request.getSkill())
                .ifPresent(existingSkill -> {
                    if (!existingSkill.getId().equals(id)) {
                        throw new IllegalArgumentException(
                                "Skill already exists: " + request.getSkill()
                        );
                    }
                });
        skill.setName(request.getSkill());

        return toResponse(skillRepository.save(skill));
    }

    private SkillResponseDto toResponse(Skill skill) {
        return new SkillResponseDto(skill.getId(), skill.getName());
    }
}