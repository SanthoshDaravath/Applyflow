package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import java.time.Instant;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class HealthController {

    @Value("${spring.application.name:applyflow-backend}")
    private String applicationName;

    @GetMapping("/health")
    public ResponseEntity<ApiDtos.HealthResponse> health() {
        return ResponseEntity.ok(new ApiDtos.HealthResponse("UP", applicationName, Instant.now()));
    }
}
