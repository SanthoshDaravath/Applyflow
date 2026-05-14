package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.config.RabbitConfig;
import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.EmailEventEntity;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.exception.ApiException;
import com.applyflow.ai.repository.ApplicationRepository;
import com.applyflow.ai.repository.EmailEventRepository;
import com.applyflow.ai.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class EmailIngestionService {

    private final EmailEventRepository emailEventRepository;
    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final AiAnalysisService aiAnalysisService;
    private final ObjectMapper objectMapper;

    public EmailIngestionService(
            EmailEventRepository emailEventRepository,
            ApplicationRepository applicationRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            AiAnalysisService aiAnalysisService,
            ObjectMapper objectMapper) {
        this.emailEventRepository = emailEventRepository;
        this.applicationRepository = applicationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.aiAnalysisService = aiAnalysisService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public ApiDtos.EmailEventResponse ingest(ApiDtos.EmailEventRequest request) {
        if (!StringUtils.hasText(request.messageId())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_EMAIL_EVENT", "messageId is required");
        }
        if (emailEventRepository.existsByMessageId(request.messageId())) {
            return emailEventRepository.findByMessageId(request.messageId())
                    .map(this::toResponse)
                    .orElseThrow();
        }
        if (request.userId() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "USER_ID_REQUIRED", "userId is required for email ingestion");
        }
        UserEntity user = userRepository.findById(request.userId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found for email ingestion"));
        AiAnalysisService.EmailAnalysis analysis = aiAnalysisService.analyzeEmail(request.subject(), request.body());
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("company", analysis.company());
        metadata.put("role", analysis.role());
        metadata.put("dates", analysis.dates());
        metadata.put("links", analysis.links());
        metadata.put("sourceProvider", request.sourceProvider());
        JobApplicationEntity application = linkOrCreateApplication(user, analysis, request);
        EmailEventEntity entity = emailEventRepository.save(EmailEventEntity.builder()
                .user(user)
                .application(application)
                .messageId(request.messageId())
                .threadId(request.threadId())
                .fromAddress(request.fromAddress())
                .subject(request.subject())
                .body(request.body())
                .bodySnippet(analysis.summary())
                .classification(analysis.classification())
                .confidence(analysis.confidence())
                .metadataJson(writeJson(metadata))
                .receivedAt(Instant.now())
                .processedAt(Instant.now())
                .build());
        return toResponse(entity);
    }

    @RabbitListener(queues = RabbitConfig.EMAIL_QUEUE)
    @Transactional
    public void consume(ApiDtos.EmailEventRequest request) {
        ingest(request);
    }

    public List<ApiDtos.EmailEventResponse> listMine() {
        UserEntity user = currentUserService.currentUser();
        return emailEventRepository.findTop20ByUserOrderByReceivedAtDesc(user).stream().map(this::toResponse).toList();
    }

    private JobApplicationEntity linkOrCreateApplication(UserEntity user, AiAnalysisService.EmailAnalysis analysis, ApiDtos.EmailEventRequest request) {
        DomainEnums.ApplicationStatus mappedStatus = switch (analysis.classification()) {
            case INTERVIEW -> DomainEnums.ApplicationStatus.INTERVIEW;
            case OFFER -> DomainEnums.ApplicationStatus.OFFER;
            case REJECTED -> DomainEnums.ApplicationStatus.REJECTED;
            case ASSESSMENT -> DomainEnums.ApplicationStatus.ONLINE_ASSESSMENT;
            case APPLIED -> DomainEnums.ApplicationStatus.APPLIED;
            default -> null;
        };
        List<JobApplicationEntity> applications = applicationRepository.findByUserOrderByCreatedAtDesc(user);
        JobApplicationEntity match = applications.stream()
                .filter(app -> app.getCompany() != null && app.getCompany().equalsIgnoreCase(defaultCompany(analysis.company(), request.fromAddress())))
                .findFirst()
                .orElse(null);
        if (match == null && mappedStatus != null) {
            match = applicationRepository.save(JobApplicationEntity.builder()
                    .user(user)
                    .company(defaultCompany(analysis.company(), request.fromAddress()))
                    .roleName(defaultRole(analysis.role(), request.subject()))
                    .location("Remote")
                    .sourcePlatform(defaultIfBlank(request.sourceProvider(), "Gmail"))
                    .applicationDate(java.time.LocalDate.now())
                    .status(mappedStatus)
                    .notes(analysis.summary())
                    .jobUrl(extractFirstLink(analysis.links()))
                    .resumeSnapshot(null)
                    .build());
        } else if (match != null && mappedStatus != null && match.getStatus() != mappedStatus) {
            match.setStatus(mappedStatus);
            if (StringUtils.hasText(analysis.summary())) {
                match.setNotes(analysis.summary());
            }
            applicationRepository.save(match);
        }
        return match;
    }

    private String defaultCompany(String analysisCompany, String fromAddress) {
        if (StringUtils.hasText(analysisCompany) && !"Unknown company".equalsIgnoreCase(analysisCompany)) {
            return analysisCompany;
        }
        if (StringUtils.hasText(fromAddress) && fromAddress.contains("@")) {
            return fromAddress.substring(fromAddress.indexOf('@') + 1).replace(".", " ");
        }
        return "Unknown company";
    }

    private String defaultRole(String analysisRole, String subject) {
        return StringUtils.hasText(analysisRole) ? analysisRole : (StringUtils.hasText(subject) ? subject : "Job Application");
    }

    private String extractFirstLink(List<String> links) {
        return links.isEmpty() ? null : links.get(0);
    }

    private String defaultIfBlank(String value, String fallback) {
        return StringUtils.hasText(value) ? value : fallback;
    }

    private String writeJson(Map<String, Object> metadata) {
        try {
            return objectMapper.writeValueAsString(metadata);
        } catch (JsonProcessingException exception) {
            return "{}";
        }
    }

    private ApiDtos.EmailEventResponse toResponse(EmailEventEntity entity) {
        Map<String, Object> metadata;
        try {
            metadata = objectMapper.readValue(entity.getMetadataJson() == null ? "{}" : entity.getMetadataJson(), new com.fasterxml.jackson.core.type.TypeReference<>() {});
        } catch (Exception exception) {
            metadata = Map.of();
        }
        return new ApiDtos.EmailEventResponse(
                entity.getId(),
                entity.getMessageId(),
                entity.getFromAddress(),
                entity.getSubject(),
                entity.getBodySnippet(),
                entity.getClassification(),
                entity.getConfidence(),
                metadata,
                entity.getReceivedAt(),
                entity.getProcessedAt());
    }
}
