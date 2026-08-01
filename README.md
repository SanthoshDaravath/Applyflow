# ApplyFlow AI

ApplyFlow AI is a production-oriented, SaaS-style job application operating system for tracking applications, analyzing resumes, classifying job emails, and generating AI-powered career insights.

## Architecture

- **Frontend:** React + Vite + Tailwind + React Router + Axios + Framer Motion + Recharts + Zustand
- **Backend:** Java 21 + Spring Boot 3 + Spring Security + Spring Data JPA + JWT + OAuth2 + Validation + Lombok + Spring AI-ready service layer
- **Data:** PostgreSQL, Redis, RabbitMQ
- **Deployment:** Docker + docker-compose

## Monorepo Layout

- `backend/` — Spring Boot API
- `frontend/` — React SaaS UI
- `docker-compose.yml` — local production-like stack
- `.env.example` — environment variable reference

## Local Run

### Backend

```cmd
cd backend
mvn spring-boot:run
```

### Frontend

```cmd
cd frontend
npm install
npm run dev
```

### Full stack with Docker

```cmd
docker-compose up --build
```

## Core Features Included

- JWT auth with refresh tokens
- Google OAuth2 login wiring
- Application CRUD and Kanban pipeline
- Gmail ingestion event pipeline with RabbitMQ
- AI summarization / classification service with confidence scoring and regex fallback
- Resume analyzer with ATS-style scoring
- Analytics dashboard with Recharts
- Interview tracking and reminders
- Notification system
- Swagger/OpenAPI docs
- CORS, validation, global error handling, and rate-limit-ready security structure

## Environment Variables

See `.env.example` for backend and frontend configuration values.

## Render Deployment (Blueprint)

This repository now includes `render.yaml` at the repo root for one-click Render Blueprint provisioning.

1. Push this repository to GitHub.
2. In Render, create a **Blueprint** from this repository.
3. Render provisions backend, frontend, Redis, RabbitMQ (private service), and MySQL.
4. Set required secret env vars in Render:
   - `JWT_SECRET`
   - `RABBITMQ_PASSWORD`
   - `OPENAI_API_KEY` (if AI features are enabled)
5. Confirm/update public URLs:
   - Backend health check: `/api/v1/health`
   - Backend API base: `https://<backend-domain>/api/v1`
   - Frontend URL value in backend `FRONTEND_URL`
   - Google OAuth redirect URI if OAuth is enabled

## Notes

The repository is structured to be production-ready and extensible. External integrations such as Gmail, Google OAuth, OpenAI, and mail delivery are isolated behind service boundaries so they can be configured per environment.
