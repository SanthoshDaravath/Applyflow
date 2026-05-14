package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class AiAnalysisService {

    private static final Set<String> STOP_WORDS = Set.of("the", "and", "for", "with", "from", "that", "this", "your", "are", "you", "our", "job", "role", "position", "company", "at", "to", "of", "in", "on", "a", "an", "by", "be", "as", "is", "it", "we", "will", "has", "have", "can", "or");
    private static final Pattern DATE_PATTERN = Pattern.compile("(?i)(?:\\b(?:mon|tue|wed|thu|fri|sat|sun)[a-z]*\\b,?\\s*)?(?:jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)[a-z]*\\s+\\d{1,2}(?:,?\\s*\\d{4})?|\\d{1,2}/\\d{1,2}/\\d{2,4}");
    private static final Pattern LINK_PATTERN = Pattern.compile("https?://[^\\s)]+", Pattern.CASE_INSENSITIVE);

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;

    public AiAnalysisService(
            ObjectMapper objectMapper,
            @Value("${app.ai.api-key:}") String apiKey,
            @Value("${app.ai.model:gpt-4o-mini}") String model,
            @Value("${app.ai.base-url:https://api.openai.com/v1}") String baseUrl) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
        this.webClient = WebClient.builder().baseUrl(baseUrl).build();
    }

    public EmailAnalysis analyzeEmail(String subject, String body) {
        String text = safeText(subject) + "\n\n" + safeText(body);
        if (StringUtils.hasText(apiKey)) {
            EmailAnalysis aiResult = tryAiEmailAnalysis(text);
            if (aiResult != null) {
                return aiResult;
            }
        }
        return fallbackEmailAnalysis(subject, body);
    }

    public ResumeAnalysis analyzeResume(String resumeText, String jobDescription) {
        String normalizedResume = safeText(resumeText).toLowerCase(Locale.ROOT);
        String normalizedJob = safeText(jobDescription).toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(normalizedResume) || !StringUtils.hasText(normalizedJob)) {
            return new ResumeAnalysis(0, List.of(), List.of(), List.of("Provide both resume text and a job description for an accurate ATS score."), "Insufficient input for analysis.");
        }
        List<String> resumeKeywords = extractKeywords(normalizedResume);
        List<String> jobKeywords = extractKeywords(normalizedJob);
        Set<String> matched = new LinkedHashSet<>();
        Set<String> missing = new LinkedHashSet<>();
        for (String keyword : jobKeywords) {
            if (containsKeyword(normalizedResume, keyword)) {
                matched.add(keyword);
            } else {
                missing.add(keyword);
            }
        }
        int score = Math.min(100, Math.max(25, (int) Math.round((matched.size() * 100.0) / Math.max(1, jobKeywords.size())) + 10));
        List<String> suggestions = new ArrayList<>();
        if (!missing.isEmpty()) {
            suggestions.add("Add more evidence of: " + String.join(", ", missing.stream().limit(6).toList()));
        }
        if (score < 70) {
            suggestions.add("Mirror the job description language more closely in your summary and achievements.");
            suggestions.add("Quantify impact with metrics, revenue, or scale.");
        }
        if (score >= 80) {
            suggestions.add("Your resume aligns well. Tailor the top third with the target role title and company context.");
        }
        return new ResumeAnalysis(score, List.copyOf(matched), List.copyOf(missing), suggestions, summarizeResume(resumeText, jobDescription, score));
    }

    public InsightDraft generateInsightDraft(List<String> observations, String context) {
        String joined = String.join("; ", observations);
        if (StringUtils.hasText(apiKey)) {
            try {
                Map<String, Object> payload = Map.of(
                        "model", model,
                        "temperature", 0.2,
                        "messages", List.of(
                                Map.of("role", "system", "content", "You are ApplyFlow AI's career insights engine. Return compact JSON with title, summary, recommendations, confidence."),
                                Map.of("role", "user", "content", "Context: " + safeText(context) + "\nObservations: " + joined)));
                String response = webClient.post()
                        .uri("/chat/completions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer " + apiKey)
                        .bodyValue(payload)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block(Duration.ofSeconds(30));
                if (StringUtils.hasText(response)) {
                    Map<String, Object> parsed = objectMapper.readValue(response, new TypeReference<>() {});
                    List<Map<String, Object>> choices = (List<Map<String, Object>>) parsed.get("choices");
                    if (choices != null && !choices.isEmpty()) {
                        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                        if (message != null) {
                            Object content = message.get("content");
                            if (content != null) {
                                String json = extractJson(String.valueOf(content));
                                Map<String, Object> draft = objectMapper.readValue(json, new TypeReference<>() {});
                                return insightDraftFromMap(draft);
                            }
                        }
                    }
                }
            } catch (Exception ignored) {
                // Fallback below.
            }
        }
        return new InsightDraft(
                "Career pattern detected",
                "We found a useful trend in your recent application data.",
                List.of("Apply to the strongest platforms earlier in the week.", "Tailor resumes to each high-value role.", "Follow up 5-7 days after applications."),
                0.72);
    }

    private EmailAnalysis tryAiEmailAnalysis(String text) {
        try {
            Map<String, Object> payload = Map.of(
                    "model", model,
                    "temperature", 0.1,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You classify job application emails. Return compact JSON with classification, summary, company, role, confidence, dates, links."),
                            Map.of("role", "user", "content", text)));
            String response = webClient.post()
                    .uri("/chat/completions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .bodyValue(payload)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(Duration.ofSeconds(30));
            if (!StringUtils.hasText(response)) {
                return null;
            }
            Map<String, Object> parsed = objectMapper.readValue(response, new TypeReference<>() {});
            List<Map<String, Object>> choices = (List<Map<String, Object>>) parsed.get("choices");
            if (choices == null || choices.isEmpty()) {
                return null;
            }
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            if (message == null) {
                return null;
            }
            String content = extractJson(String.valueOf(message.get("content")));
            Map<String, Object> json = objectMapper.readValue(content, new TypeReference<>() {});
            return analysisFromMap(json);
        } catch (Exception exception) {
            return null;
        }
    }

    private EmailAnalysis fallbackEmailAnalysis(String subject, String body) {
        String combined = (safeText(subject) + " " + safeText(body)).toLowerCase(Locale.ROOT);
        DomainEnums.EmailClassification classification = DomainEnums.EmailClassification.UNKNOWN;
        if (containsAny(combined, "interview", "available slot", "schedule", "meet the team", "screening")) {
            classification = DomainEnums.EmailClassification.INTERVIEW;
        } else if (containsAny(combined, "offer", "congratulations", "compensation", "package")) {
            classification = DomainEnums.EmailClassification.OFFER;
        } else if (containsAny(combined, "rejected", "unfortunately", "not moving forward", "not selected")) {
            classification = DomainEnums.EmailClassification.REJECTED;
        } else if (containsAny(combined, "assessment", "assignment", "coding test", "take-home")) {
            classification = DomainEnums.EmailClassification.ASSESSMENT;
        } else if (containsAny(combined, "follow up", "next step", "next steps", "applied", "received your application")) {
            classification = DomainEnums.EmailClassification.APPLIED;
        }
        String company = extractCompany(subject, body);
        String role = extractRole(subject, body);
        List<String> dates = extractMatches(DATE_PATTERN, body);
        List<String> links = extractMatches(LINK_PATTERN, body);
        double confidence = classification == DomainEnums.EmailClassification.UNKNOWN ? 0.42 : 0.76;
        if (!StringUtils.hasText(company)) {
            company = "Unknown company";
        }
        if (!StringUtils.hasText(role)) {
            role = "Unknown role";
        }
        return new EmailAnalysis(classification, summarizeText(body, 240), company, role, dates, links, confidence);
    }

    private ResumeAnalysis fallbackResumeAnalysis(String resumeText, String jobDescription) {
        return analyzeResume(resumeText, jobDescription);
    }

    private EmailAnalysis analysisFromMap(Map<String, Object> map) {
        DomainEnums.EmailClassification classification = parseClassification(String.valueOf(map.getOrDefault("classification", "UNKNOWN")));
        String summary = safeText(String.valueOf(map.getOrDefault("summary", "")));
        String company = safeText(String.valueOf(map.getOrDefault("company", "Unknown company")));
        String role = safeText(String.valueOf(map.getOrDefault("role", "Unknown role")));
        double confidence = parseDouble(map.get("confidence"), classification == DomainEnums.EmailClassification.UNKNOWN ? 0.5 : 0.8);
        List<String> dates = parseStringList(map.get("dates"));
        List<String> links = parseStringList(map.get("links"));
        return new EmailAnalysis(classification, summary, company, role, dates, links, confidence);
    }

    private InsightDraft insightDraftFromMap(Map<String, Object> map) {
        return new InsightDraft(
                safeText(String.valueOf(map.getOrDefault("title", "Career insight"))),
                safeText(String.valueOf(map.getOrDefault("summary", ""))),
                parseStringList(map.get("recommendations")),
                parseDouble(map.get("confidence"), 0.7));
    }

    private String summarizeResume(String resumeText, String jobDescription, int score) {
        String summary = "ATS score: " + score + ". " + (score >= 80 ? "Strong fit." : score >= 65 ? "Moderate fit." : "Needs tailoring.");
        String jobHighlights = extractKeywords(safeText(jobDescription).toLowerCase(Locale.ROOT)).stream().limit(5).collect(Collectors.joining(", "));
        return summary + (StringUtils.hasText(jobHighlights) ? " Target keywords: " + jobHighlights + "." : "");
    }

    private List<String> extractKeywords(String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        return Arrays.stream(text.replaceAll("[^a-zA-Z0-9+.#\\s]", " ").split("\\s+"))
                .map(String::trim)
                .filter(token -> token.length() >= 3 && !STOP_WORDS.contains(token))
                .distinct()
                .limit(25)
                .toList();
    }

    private boolean containsKeyword(String text, String keyword) {
        return text.contains(keyword.toLowerCase(Locale.ROOT));
    }

    private boolean containsAny(String text, String... values) {
        for (String value : values) {
            if (text.contains(value)) {
                return true;
            }
        }
        return false;
    }

    private String extractCompany(String subject, String body) {
        String combined = safeText(subject) + " " + safeText(body);
        Matcher matcher = Pattern.compile("(?i)(?:at|from|with)\\s+([A-Z][A-Za-z0-9&.,' -]{2,})").matcher(combined);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        matcher = Pattern.compile("(?i)([A-Z][A-Za-z0-9&.,' -]{2,})\\s+(?:team|recruiting|talent|careers)").matcher(combined);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private String extractRole(String subject, String body) {
        String combined = safeText(subject) + " " + safeText(body);
        Matcher matcher = Pattern.compile("(?i)(?:for|regarding|role|position)\\s+([A-Za-z0-9+.#\\- /]{3,})").matcher(combined);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        matcher = Pattern.compile("(?i)([A-Za-z0-9+.#\\- /]{3,})\\s+(?:interview|assessment|offer|application)").matcher(combined);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "";
    }

    private List<String> extractMatches(Pattern pattern, String text) {
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        Matcher matcher = pattern.matcher(text);
        List<String> matches = new ArrayList<>();
        while (matcher.find()) {
            matches.add(matcher.group());
        }
        return matches.stream().distinct().toList();
    }

    private String summarizeText(String text, int maxLength) {
        if (!StringUtils.hasText(text)) {
            return "";
        }
        String cleaned = text.replaceAll("\\s+", " ").trim();
        return cleaned.length() <= maxLength ? cleaned : cleaned.substring(0, maxLength - 3) + "...";
    }

    private DomainEnums.EmailClassification parseClassification(String value) {
        try {
            return DomainEnums.EmailClassification.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (Exception exception) {
            return DomainEnums.EmailClassification.UNKNOWN;
        }
    }

    private List<String> parseStringList(Object value) {
        if (value == null) {
            return List.of();
        }
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).filter(StringUtils::hasText).toList();
        }
        String text = String.valueOf(value).trim();
        if (!StringUtils.hasText(text)) {
            return List.of();
        }
        if (text.startsWith("[")) {
            try {
                return objectMapper.readValue(text, new TypeReference<List<String>>() {});
            } catch (Exception ignored) {
            }
        }
        return Arrays.stream(text.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }

    private double parseDouble(Object value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (Exception exception) {
            return defaultValue;
        }
    }

    private String extractJson(String content) {
        if (!StringUtils.hasText(content)) {
            return "{}";
        }
        String trimmed = content.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        int start = trimmed.indexOf('{');
        int end = trimmed.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return trimmed.substring(start, end + 1);
        }
        return trimmed;
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public record EmailAnalysis(
            DomainEnums.EmailClassification classification,
            String summary,
            String company,
            String role,
            List<String> dates,
            List<String> links,
            double confidence) {
    }

    public record ResumeAnalysis(
            int atsScore,
            List<String> matchedKeywords,
            List<String> missingKeywords,
            List<String> suggestions,
            String summary) {
    }

    public record InsightDraft(String title, String summary, List<String> recommendations, double confidence) {
    }
}
