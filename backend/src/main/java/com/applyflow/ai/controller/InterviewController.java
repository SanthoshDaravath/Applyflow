package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.InterviewService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/interviews")
public class InterviewController {

    private final InterviewService interviewService;

    public InterviewController(InterviewService interviewService) {
        this.interviewService = interviewService;
    }

    @GetMapping("/upcoming")
    public ResponseEntity<?> upcoming() {
        return ResponseEntity.ok(interviewService.upcoming());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiDtos.InterviewResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(interviewService.getById(id));
    }

    @PostMapping
    public ResponseEntity<ApiDtos.InterviewResponse> create(@Valid @RequestBody ApiDtos.InterviewRequest request) {
        return ResponseEntity.ok(interviewService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiDtos.InterviewResponse> update(@PathVariable UUID id, @Valid @RequestBody ApiDtos.InterviewRequest request) {
        return ResponseEntity.ok(interviewService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiDtos.GenericMessageResponse> delete(@PathVariable UUID id) {
        interviewService.delete(id);
        return ResponseEntity.ok(new ApiDtos.GenericMessageResponse("Interview deleted"));
    }
}
