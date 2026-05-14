package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.ResumeService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/resumes")
public class ResumeController {

    private final ResumeService resumeService;

    public ResumeController(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiDtos.ResumeResponse> upload(
            @RequestPart(required = false) MultipartFile file,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String jobDescription,
            @RequestParam(required = false) String extractedText,
            @RequestParam(required = false) UUID applicationId) {
        return ResponseEntity.ok(resumeService.upload(file, title, jobDescription, extractedText, applicationId));
    }

    @PostMapping("/analyze")
    public ResponseEntity<ApiDtos.ResumeAnalysisResponse> analyze(@Valid @RequestBody ApiDtos.ResumeUploadRequest request) {
        return ResponseEntity.ok(resumeService.analyze(request.resumeText(), request.jobDescription()));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine() {
        return ResponseEntity.ok(resumeService.listMine());
    }

    @GetMapping("/top")
    public ResponseEntity<?> top() {
        return ResponseEntity.ok(resumeService.topResumes());
    }
}
