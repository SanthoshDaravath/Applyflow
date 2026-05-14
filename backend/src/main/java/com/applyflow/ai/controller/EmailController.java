package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.CurrentUserService;
import com.applyflow.ai.service.EmailIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emails")
public class EmailController {

    private final EmailIngestionService emailIngestionService;
    private final CurrentUserService currentUserService;

    public EmailController(EmailIngestionService emailIngestionService, CurrentUserService currentUserService) {
        this.emailIngestionService = emailIngestionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<ApiDtos.EmailEventResponse> ingest(@Valid @RequestBody ApiDtos.EmailEventRequest request) {
        ApiDtos.EmailEventRequest enriched = request.userId() == null
                ? new ApiDtos.EmailEventRequest(currentUserService.currentUser().getId(), request.messageId(), request.fromAddress(), request.subject(), request.body(), request.threadId(), request.sourceProvider())
                : request;
        return ResponseEntity.ok(emailIngestionService.ingest(enriched));
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine() {
        return ResponseEntity.ok(emailIngestionService.listMine());
    }
}
