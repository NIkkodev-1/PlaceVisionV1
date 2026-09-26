package com.nikko.backend.controller;

import com.nikko.backend.dto.report.ReportResponseDto;
import com.nikko.backend.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/quizzes/attempts")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping("/{attemptId}/report")
    public ResponseEntity<ReportResponseDto> generateReport(@PathVariable UUID attemptId) {
        return ResponseEntity.ok(reportService.generateReport(attemptId));
    }
}