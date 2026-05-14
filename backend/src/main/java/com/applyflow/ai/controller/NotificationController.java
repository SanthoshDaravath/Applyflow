package com.applyflow.ai.controller;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.service.NotificationService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/mine")
    public ResponseEntity<?> mine() {
        return ResponseEntity.ok(notificationService.listMine());
    }

    @PostMapping
    public ResponseEntity<ApiDtos.NotificationResponse> create(@Valid @RequestBody ApiDtos.NotificationRequest request) {
        return ResponseEntity.ok(notificationService.create(request));
    }

    @PatchMapping("/{id}/sent")
    public ResponseEntity<ApiDtos.GenericMessageResponse> markSent(@PathVariable UUID id) {
        notificationService.markSent(id);
        return ResponseEntity.ok(new ApiDtos.GenericMessageResponse("Notification marked sent"));
    }
}
