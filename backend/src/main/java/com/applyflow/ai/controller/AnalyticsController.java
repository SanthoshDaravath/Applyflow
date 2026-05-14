package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<ApiDtos.DashboardResponse> dashboard() {
        return ResponseEntity.ok(analyticsService.dashboard());
    }

    @GetMapping("/insights")
    public ResponseEntity<?> insights() {
        return ResponseEntity.ok(analyticsService.listInsights());
    }

    @PostMapping("/insights/refresh")
    public ResponseEntity<?> refreshInsights() {
        return ResponseEntity.ok(analyticsService.refreshInsights());
    }
}
