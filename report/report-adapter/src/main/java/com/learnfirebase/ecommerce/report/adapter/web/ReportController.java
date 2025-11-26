package com.learnfirebase.ecommerce.report.adapter.web;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.learnfirebase.ecommerce.report.application.command.GenerateReportCommand;
import com.learnfirebase.ecommerce.report.application.dto.ReportDto;
import com.learnfirebase.ecommerce.report.application.port.in.GenerateDailyReportUseCase;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {
    private final GenerateDailyReportUseCase generateDailyReportUseCase;

    @GetMapping("/daily")
    public ResponseEntity<ReportDto> daily(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(generateDailyReportUseCase.execute(GenerateReportCommand.builder().date(date).build()));
    }
}
