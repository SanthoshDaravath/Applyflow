package com.applyflow.ai.service;

import com.applyflow.ai.dto.ApiDtos;
import com.applyflow.ai.entity.UserEntity;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GmailSyncService {

    private static final Logger log = LoggerFactory.getLogger(GmailSyncService.class);
    private static final int MAX_MESSAGES = 50;
    private static final String GMAIL_API_BASE = "https://gmail.googleapis.com";

    private final EmailIngestionService emailIngestionService;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    public GmailSyncService(EmailIngestionService emailIngestionService, ObjectMapper objectMapper) {
        this.emailIngestionService = emailIngestionService;
        this.objectMapper = objectMapper;
        this.webClient = WebClient.builder().baseUrl(GMAIL_API_BASE).build();
    }

    public void syncRecentJobEmails(UserEntity user, String accessToken) {
        try {
            JsonNode listResponse = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/gmail/v1/users/me/messages")
                            .queryParam("q", "subject:(application OR interview OR offer OR rejected OR assessment OR recruiter)")
                            .queryParam("maxResults", MAX_MESSAGES)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .bodyToMono(String.class)
                    .map(this::readTree)
                    .block();

            if (listResponse == null || !listResponse.has("messages")) {
                return;
            }

            Iterator<JsonNode> iterator = listResponse.get("messages").elements();
            while (iterator.hasNext()) {
                JsonNode item = iterator.next();
                String messageId = item.path("id").asText("");
                if (messageId.isBlank()) {
                    continue;
                }

                JsonNode message = webClient.get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/gmail/v1/users/me/messages/{id}")
                                .queryParam("format", "metadata")
                                .queryParam("metadataHeaders", "Subject")
                                .queryParam("metadataHeaders", "From")
                                .build(messageId))
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(String.class)
                        .map(this::readTree)
                        .block();

                if (message == null) {
                    continue;
                }

                String subject = headerValue(message, "Subject");
                String from = headerValue(message, "From");
                String snippet = message.path("snippet").asText("");
                String threadId = message.path("threadId").asText("");

                if (subject.isBlank()) {
                    subject = "Gmail Message";
                }
                if (snippet.isBlank()) {
                    snippet = subject;
                }

                ApiDtos.EmailEventRequest request = new ApiDtos.EmailEventRequest(
                        user.getId(),
                        messageId,
                        from,
                        subject,
                        snippet,
                        threadId,
                        "Gmail");
                emailIngestionService.ingest(request);
            }
        } catch (Exception exception) {
            log.warn("Gmail auto-sync skipped for user {}: {}", user.getEmail(), exception.getMessage());
        }
    }

    private String headerValue(JsonNode message, String headerName) {
        JsonNode headers = message.path("payload").path("headers");
        if (!headers.isArray()) {
            return "";
        }
        for (JsonNode header : headers) {
            if (headerName.equalsIgnoreCase(header.path("name").asText())) {
                return header.path("value").asText("");
            }
        }
        return "";
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception exception) {
            return objectMapper.createObjectNode();
        }
    }
}

