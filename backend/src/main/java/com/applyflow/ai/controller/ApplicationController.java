package com.applyflow.ai.controller;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.ApplicationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping
    public ResponseEntity<ApiDtos.PageResponse<ApiDtos.ApplicationResponse>> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) DomainEnums.ApplicationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.list(q, status, page, size));
    }

    @GetMapping("/kanban")
    public ResponseEntity<?> kanban() {
        return ResponseEntity.ok(applicationService.kanban());
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiDtos.ApplicationStatsResponse> stats() {
        return ResponseEntity.ok(applicationService.stats());
    }

    @GetMapping("/timeline")
    public ResponseEntity<?> timeline() {
        return ResponseEntity.ok(applicationService.timeline());
    }

    @GetMapping("/platforms")
    public ResponseEntity<?> platforms() {
        return ResponseEntity.ok(applicationService.platformBreakdown());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDtos.ApplicationResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(applicationService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiDtos.ApplicationResponse> create(@Valid @RequestBody ApiDtos.ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiDtos.ApplicationResponse> update(@PathVariable UUID id, @Valid @RequestBody ApiDtos.ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiDtos.ApplicationResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody ApiDtos.StatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateStatus(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDtos.GenericMessageResponse> delete(@PathVariable UUID id) {
        applicationService.delete(id);
        return ResponseEntity.ok(new ApiDtos.GenericMessageResponse("Application deleted"));
    }
}
