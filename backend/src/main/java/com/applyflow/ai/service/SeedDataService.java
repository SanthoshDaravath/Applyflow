package com.applyflow.ai.service;

import com.applyflow.ai.common.DomainEnums;
import com.applyflow.ai.entity.AiInsightEntity;
import com.applyflow.ai.entity.InterviewEntity;
import com.applyflow.ai.entity.JobApplicationEntity;
import com.applyflow.ai.entity.NotificationEntity;
import com.applyflow.ai.entity.ResumeEntity;
import com.applyflow.ai.entity.UserEntity;
import com.applyflow.ai.repository.AiInsightRepository;
import com.applyflow.ai.repository.ApplicationRepository;
import com.applyflow.ai.repository.InterviewRepository;
import com.applyflow.ai.repository.NotificationRepository;
import com.applyflow.ai.repository.ResumeRepository;
import com.applyflow.ai.repository.UserRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SeedDataService {

    private final UserRepository userRepository;
    private final ApplicationRepository applicationRepository;
    private final ResumeRepository resumeRepository;
    private final InterviewRepository interviewRepository;
    private final NotificationRepository notificationRepository;
    private final AiInsightRepository aiInsightRepository;
    private final PasswordEncoder passwordEncoder;

    public SeedDataService(UserRepository userRepository, ApplicationRepository applicationRepository, ResumeRepository resumeRepository, InterviewRepository interviewRepository, NotificationRepository notificationRepository, AiInsightRepository aiInsightRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.applicationRepository = applicationRepository;
        this.resumeRepository = resumeRepository;
        this.interviewRepository = interviewRepository;
        this.notificationRepository = notificationRepository;
        this.aiInsightRepository = aiInsightRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        UserEntity admin = userRepository.findByEmailIgnoreCase("admin@applyflow.ai")
                .orElseGet(() -> userRepository.save(UserEntity.builder()
                        .email("admin@applyflow.ai")
                        .fullName("ApplyFlow Admin")
                        .passwordHash(passwordEncoder.encode("Admin@12345"))
                        .provider("local")
                        .role(DomainEnums.UserRole.ADMIN)
                        .enabled(true)
                        .emailVerified(true)
                        .build()));

        UserEntity demo = userRepository.findByEmailIgnoreCase("demo@applyflow.ai")
                .orElseGet(() -> userRepository.save(UserEntity.builder()
                        .email("demo@applyflow.ai")
                        .fullName("Maya Patel")
                        .passwordHash(passwordEncoder.encode("Password123!"))
                        .provider("local")
                        .role(DomainEnums.UserRole.USER)
                        .enabled(true)
                        .emailVerified(true)
                        .build()));

        if (applicationRepository.countByUser(demo) == 0) {
            List<JobApplicationEntity> apps = List.of(
                    JobApplicationEntity.builder().user(demo).company("Stripe").roleName("Senior Frontend Engineer").salary(new BigDecimal("180000")).location("Remote").sourcePlatform("LinkedIn").applicationDate(LocalDate.now().minusDays(18)).status(DomainEnums.ApplicationStatus.INTERVIEW).notes("Portfolio review complete").jobUrl("https://stripe.com/jobs").resumeSnapshot("Strong React, design systems, and performance work.").build(),
                    JobApplicationEntity.builder().user(demo).company("Notion").roleName("Product Engineer").salary(new BigDecimal("170000")).location("San Francisco").sourcePlatform("Wellfound").applicationDate(LocalDate.now().minusDays(15)).status(DomainEnums.ApplicationStatus.APPLIED).notes("Referral submitted").jobUrl("https://notion.so/careers").resumeSnapshot("Full-stack product engineering and UX affinity.").build(),
                    JobApplicationEntity.builder().user(demo).company("Revolut").roleName("Backend Engineer").salary(new BigDecimal("160000")).location("London").sourcePlatform("Gmail").applicationDate(LocalDate.now().minusDays(12)).status(DomainEnums.ApplicationStatus.ONLINE_ASSESSMENT).notes("Assessment pending").jobUrl("https://www.revolut.com/careers").resumeSnapshot("Java microservices, Kafka, PostgreSQL.").build(),
                    JobApplicationEntity.builder().user(demo).company("Atlassian").roleName("Platform Engineer").salary(new BigDecimal("175000")).location("Remote").sourcePlatform("Indeed").applicationDate(LocalDate.now().minusDays(9)).status(DomainEnums.ApplicationStatus.SAVED).notes("Need to tailor resume").jobUrl("https://www.atlassian.com/company/careers").resumeSnapshot("Kubernetes, observability, and cloud architecture.").build(),
                    JobApplicationEntity.builder().user(demo).company("Uber") .roleName("Staff Software Engineer").salary(new BigDecimal("210000")).location("New York").sourcePlatform("Naukri").applicationDate(LocalDate.now().minusDays(7)).status(DomainEnums.ApplicationStatus.REJECTED).notes("Feedback: limited mobile leadership depth").jobUrl("https://www.uber.com/us/en/careers").resumeSnapshot("Distributed systems, Java 21, event-driven architecture.").build(),
                    JobApplicationEntity.builder().user(demo).company("OpenAI").roleName("Applied AI Engineer").salary(new BigDecimal("220000")).location("Remote").sourcePlatform("Greenhouse").applicationDate(LocalDate.now().minusDays(3)).status(DomainEnums.ApplicationStatus.OFFER).notes("Offer call scheduled").jobUrl("https://openai.com/careers").resumeSnapshot("LLM integrations, retrieval, and platform engineering.").build());
            applicationRepository.saveAll(apps);
        }

        if (resumeRepository.findByUserOrderByCreatedAtDesc(demo).isEmpty()) {
            JobApplicationEntity application = applicationRepository.findByUserOrderByCreatedAtDesc(demo).get(0);
            resumeRepository.save(ResumeEntity.builder()
                    .user(demo)
                    .application(application)
                    .title("Primary Product Resume")
                    .fileName("maya-patel-resume.pdf")
                    .contentType("application/pdf")
                    .storagePath("/opt/app/uploads/maya-patel-resume.pdf")
                    .source(DomainEnums.ResumeSource.UPLOADED)
                    .jobDescription("Senior full-stack engineer with React, Java, and cloud experience.")
                    .resumeText("Seasoned engineer building scalable SaaS products with React, Spring Boot, PostgreSQL, Redis, Kafka, and AWS.")
                    .atsScore(92)
                    .matchedKeywordsJson("[\"React\",\"Java\",\"PostgreSQL\",\"Redis\",\"AWS\"]")
                    .missingKeywordsJson("[\"system design\",\"Kubernetes\"]")
                    .suggestionsJson("[\"Add one more quantified impact bullet.\",\"Highlight mentoring and architecture ownership.\"]")
                    .build());
        }

        if (interviewRepository.findTop10ByApplicationUserOrderByScheduledAtAsc(demo).isEmpty()) {
            JobApplicationEntity application = applicationRepository.findByUserOrderByCreatedAtDesc(demo).stream().filter(app -> app.getStatus() == DomainEnums.ApplicationStatus.INTERVIEW).findFirst().orElse(applicationRepository.findByUserOrderByCreatedAtDesc(demo).get(0));
            InterviewEntity interview = interviewRepository.save(InterviewEntity.builder()
                    .application(application)
                    .roundName("System Design Round").scheduledAt(LocalDateTime.now().plusDays(2)).location("Google Meet").interviewType("Video").feedback(null).notes("Focus on distributed caching and pagination.").status(DomainEnums.InterviewStatus.SCHEDULED).reminderSent(false).build());
            notificationRepository.save(NotificationEntity.builder()
                    .user(demo)
                    .application(application)
                    .interview(interview)
                    .channel(DomainEnums.NotificationChannel.EMAIL)
                    .title("Interview reminder")
                    .message("Prepare for the system design interview in 48 hours.")
                    .status(DomainEnums.NotificationStatus.PENDING)
                    .scheduledAt(LocalDateTime.now().plusHours(2))
                    .relatedEntityType("INTERVIEW")
                    .relatedEntityId(interview.getId().toString())
                    .build());
        }

        if (aiInsightRepository.findTop10ByUserOrderByGeneratedAtDesc(demo).isEmpty()) {
            aiInsightRepository.save(AiInsightEntity.builder()
                    .user(demo)
                    .insightType(DomainEnums.InsightType.PLATFORM_PERFORMANCE)
                    .title("LinkedIn is currently your strongest source")
                    .summary("Your highest-value pipeline started with LinkedIn and Gmail responses, which are producing interview traction.")
                    .recommendationsJson("[\"Double down on LinkedIn outreach.\",\"Keep Gmail sync enabled.\",\"Follow up after five business days.\"]")
                    .confidence(0.84)
                    .generatedAt(Instant.now())
                    .build());
        }
    }
}
