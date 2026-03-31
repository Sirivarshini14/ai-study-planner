# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run Commands

### Backend (Spring Boot 3.2.5, Java 17)
```bash
cd backend
mvn package -DskipTests       # Build JAR
mvn spring-boot:run            # Dev server (port 8080)
```
Requires Java 17 (`sdk use java 17.0.18-amzn` via SDKMAN).

### Frontend (React 18 + Vite)
```bash
cd frontend
npm install                    # Install dependencies
npm run dev                    # Dev server (port 3000)
npm run build                  # Production build to dist/
```

### Docker Compose (full stack)
```bash
docker-compose up              # DB:5432, Backend:8080, Frontend:3000
```

### Production (Render)
Configured via `render.yaml`. Backend deploys as Docker on port 10000, frontend as Nginx serving Vite build. Health check at `/actuator/health`.

## Architecture

**Monorepo** with three top-level directories: `backend/`, `frontend/`, `db/`.

### Auth Flow
- **Signup**: name + mobile + email + password → user created as verified → JWT tokens returned immediately
- **Login**: email + password → OTP sent to email via Gmail SMTP → user enters OTP → JWT tokens returned
- JWT: access token (15min) + refresh token (7 days), HMAC-SHA256 signed
- Frontend auto-refreshes expired access tokens via axios response interceptor

### Backend Structure (`backend/src/main/java/com/studyplanner/`)
- `config/` — SecurityConfig (CORS, JWT filter chain, stateless sessions), JwtUtil, JwtAuthenticationFilter, GroqProperties
- `controller/` — REST endpoints: Auth, Chat, StudySession, Pomodoro, Notification, Health
- `service/` — Business logic: AuthService, OtpService, EmailService (Gmail SMTP), ChatService, GroqApiClient (AI), StudySessionService
- `entity/` — JPA entities: User, StudySession, ChatMessage, Notification, PomodoroSettings, OtpVerification
- `scheduler/` — NotificationScheduler: sends email reminders for upcoming study sessions every 60s

### Frontend Structure (`frontend/src/`)
- `pages/` — Login, Signup, VerifyOtp, Dashboard, Chat, Pomodoro
- `context/AuthContext.jsx` — Auth state + token management via React Context
- `services/api.js` — Axios instance with JWT interceptor and auto-refresh on 401
- `hooks/useTimer.jsx` — Pomodoro timer logic with phase transitions
- Entry point: `main.jsx`. Router in `App.jsx`. All JSX files use `.jsx` extension (Vite requirement).

### Key Integration: AI Chat
`ChatService` → `GroqApiClient` → Groq API (llama-3.3-70b-versatile). Chat messages are scoped to study sessions for context-aware tutoring.

## Environment Variables

### Backend (via `application.yml` defaults)
| Variable | Purpose | Default |
|---|---|---|
| `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` | PostgreSQL connection | localhost:5432/studyplanner, postgres/postgres |
| `JWT_SECRET` | Token signing key (min 32 chars) | dev placeholder |
| `GROQ_API_KEY` | Groq AI API key | (none) |
| `MAIL_USERNAME`, `MAIL_APP_PASSWORD` | Gmail SMTP for OTP emails | (none) |
| `CORS_ORIGINS` | Allowed frontend origins (comma-separated) | http://localhost:3000 |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Schema management | validate (use `update` for dev) |

### Frontend
| Variable | Purpose | Default |
|---|---|---|
| `VITE_API_URL` | Backend API base URL | http://localhost:8080/api |

## Database

PostgreSQL. Schema in `db/init.sql`. Tables: users, study_sessions, pomodoro_settings, chat_messages, notifications, otp_verifications.

`ddl-auto: validate` in production (schema must pre-exist). Use `update` for development via `SPRING_JPA_HIBERNATE_DDL_AUTO=update`.

OTP records are keyed by email. User lookup for login is by email; signup checks uniqueness on both mobile and email.

## Security

- Public endpoints: `/api/auth/signup`, `/api/auth/login`, `/api/auth/refresh`, `/api/auth/verify-otp`, `/api/auth/resend-otp`, `/api/health`, `/actuator/health`
- All other `/api/**` endpoints require Bearer token
- BCrypt password hashing (strength 12)
- Mail health indicator disabled (`management.health.mail.enabled=false`) to prevent health check failures
