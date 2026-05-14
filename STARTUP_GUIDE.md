# ApplyFlow AI - Startup Guide

## Prerequisites

### Required Software
- **Java 21**: Eclipse Temurin or OpenJDK 21
- **Maven 3.9+**: For building the Spring Boot backend
- **Node.js 20+**: For running the React frontend
- **npm 10+**: Comes with Node.js
- **Docker & Docker Compose** (Recommended): To run PostgreSQL, Redis, and RabbitMQ

### Verify Installation
```powershell
java -version          # Should show Java 21
mvn -version          # Should show Maven 3.9+
node --version        # Should show Node 20+
npm --version         # Should show npm 10+
docker --version      # Should show Docker version
docker-compose --version
```

## Configuration Files

The following `.env` files have been configured:

### Main .env (C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\.env)
- PostgreSQL: localhost:5432 (database: applyflow)
- Redis: localhost:6379
- RabbitMQ: localhost:5672
- Backend: http://localhost:8080
- Frontend: http://localhost:5173
- JWT Secret: Configured (change for production)

### Frontend .env (C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\frontend\.env)
- API Base URL: http://localhost:8080/api/v1

## Startup Steps

### Option 1: Full Stack with Docker Compose (Recommended)

This is the easiest and most reliable way to run the project. It automatically sets up all required services.

```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow

# Start all services (PostgreSQL, Redis, RabbitMQ, Backend, Frontend)
docker-compose up --build

# In another terminal, you can monitor logs
docker-compose logs -f backend
docker-compose logs -f frontend

# To stop all services
docker-compose down

# To stop and remove all data
docker-compose down -v
```

Once services are running:
- Frontend: http://localhost:5173
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- RabbitMQ Management: http://localhost:15672 (guest/guest)

---

### Option 2: Local Development (Backend + Frontend with Docker Services)

This allows you to run backend and frontend locally while Docker provides the services.

#### Step 1: Start Infrastructure Services
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow

# Start only PostgreSQL, Redis, RabbitMQ
docker-compose up -d postgres redis rabbitmq

# Verify services are running
docker-compose ps

# Wait for PostgreSQL to be ready (about 10 seconds)
```

#### Step 2: Build and Run Backend
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\backend

# Download dependencies and build (first time only, takes ~5-10 minutes)
mvn clean install

# Run the backend with Maven
mvn spring-boot:run

# Expected output includes:
# - "Started ApplyFlowApplication in X seconds"
# - "Server is running on port 8080"
# - Swagger UI available at http://localhost:8080/swagger-ui.html

# In a new terminal, verify it's working:
curl http://localhost:8080/actuator/health
```

#### Step 3: Run Frontend
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev

# Expected output:
# - "Local: http://localhost:5173/"
# - "Press 'q' to quit"
```

Once both are running:
- Access frontend: http://localhost:5173
- Swagger API docs: http://localhost:8080/swagger-ui.html

---

### Option 3: Local Development (Everything Local)

Requires PostgreSQL 16, Redis 7, and RabbitMQ 3 installed locally.

#### Setup Local Services

**PostgreSQL:**
- Install PostgreSQL 16
- Create database: `createdb -U postgres -E UTF8 applyflow`
- Create user with password: (see .env for credentials)

**Redis:**
- Install Redis 7
- Start: `redis-server`

**RabbitMQ:**
- Install RabbitMQ 3
- Start: `rabbitmq-server`

#### Run Backend and Frontend (same as Option 2)

---

## Testing the Setup

### Test Backend API
```powershell
# Health check
curl http://localhost:8080/actuator/health

# View API documentation
# Open browser: http://localhost:8080/swagger-ui.html

# Try login (should fail with 401 initially - that's normal)
curl -X POST http://localhost:8080/api/v1/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "email": "test@example.com",
    "password": "Password123!",
    "fullName": "Test User"
  }'
```

### Test Frontend
- Open http://localhost:5173 in browser
- You should see the ApplyFlow login page

---

## Common Issues & Troubleshooting

### Issue 1: "mvn: command not found" or Maven not recognized
**Solution:**
- Install Maven from https://maven.apache.org/download.cgi
- Add Maven to PATH environment variable
- Restart PowerShell/Command Prompt

### Issue 2: "java -version" shows Java 8 or older
**Solution:**
- Install Java 21: https://adoptium.net/ (Temurin)
- Set JAVA_HOME environment variable:
  ```powershell
  [Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-21", "User")
  ```
- Restart PowerShell

### Issue 3: Port 8080 or 5173 already in use
**Solution:**
- Option A: Stop the service using that port
- Option B: Change port in `.env`:
  - Backend: Change `SERVER_PORT=8080` to `SERVER_PORT=8081`
  - Frontend: Edit `vite.config.js` server.port

### Issue 4: PostgreSQL connection refused
**Solution:**
- Verify PostgreSQL is running: `docker-compose ps postgres`
- Check credentials in `.env` match the docker-compose configuration
- Restart the container: `docker-compose restart postgres`

### Issue 5: Redis/RabbitMQ not available
**Solution:**
- Start services: `docker-compose up -d redis rabbitmq`
- Wait 10 seconds for them to fully start
- Verify: `docker-compose ps`

### Issue 6: "Flyway migration failed" error on backend startup
**Solution:**
- This usually means the database schema needs to be created
- Ensure PostgreSQL is running and accessible
- Backend will automatically run migrations on startup
- Check backend logs for the specific error
- If migrations are stuck, try: `docker-compose restart postgres`

### Issue 7: Frontend shows "Cannot find module" errors
**Solution:**
```powershell
cd frontend
npm cache clean --force
rm -Force node_modules -Recurse
npm install
npm run dev
```

### Issue 8: CORS errors in browser console
**Solution:**
- Ensure backend is running on http://localhost:8080
- Check `.env` has correct FRONTEND_URL
- Verify frontend API calls use correct base URL
- Backend must have CORS enabled (it does by default)

---

## Project URLs After Startup

| Component | URL |
|-----------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| RabbitMQ Admin | http://localhost:15672 (guest/guest) |
| Health Check | http://localhost:8080/actuator/health |

---

## Default Credentials

| Service | Username | Password |
|---------|----------|----------|
| PostgreSQL | applyflow | applyflow123 |
| RabbitMQ | guest | guest |

---

## Project Structure

```
Applyflow/
├── backend/                    # Java Spring Boot application
│   ├── src/main/java/         # Backend source code
│   ├── src/main/resources/    # Configuration and migrations
│   ├── pom.xml                # Maven dependencies
│   └── Dockerfile             # Docker build for backend
├── frontend/                   # React application
│   ├── src/                   # Frontend source code
│   ├── package.json           # NPM dependencies
│   └── Dockerfile             # Docker build for frontend
├── docker-compose.yml         # Multi-container orchestration
├── .env                       # Environment configuration (updated)
└── STARTUP_GUIDE.md          # This file
```

---

## Next Steps

1. **Create a test user:**
   - Go to http://localhost:5173
   - Click Register
   - Fill in email, password, and full name
   - Click Register

2. **Access the API:**
   - Visit http://localhost:8080/swagger-ui.html
   - Try creating an application record
   - Try fetching analytics data

3. **Monitor services:**
   - Backend logs: Terminal running `mvn spring-boot:run`
   - Frontend logs: Terminal running `npm run dev`
   - Docker logs: `docker-compose logs -f`

---

## Stopping the Application

### Docker Compose:
```powershell
docker-compose down
```

### Local Development:
- Backend: Press Ctrl+C in backend terminal
- Frontend: Press Ctrl+C in frontend terminal
- Docker services: `docker-compose down`

---

## Performance Tips

1. **First build takes time** (~5-10 minutes for Maven on first run)
2. **NPM install** (~2-3 minutes first time)
3. **Subsequent builds** are much faster due to caching
4. Use `mvn -T 1C` to use multiple cores for Maven builds

---

## Support & Documentation

- **Spring Boot**: https://spring.io/projects/spring-boot
- **React**: https://react.dev
- **Vite**: https://vitejs.dev
- **Tailwind CSS**: https://tailwindcss.com
- **Docker**: https://docs.docker.com

---

Generated: Startup configuration files have been created and configured for local development.
All environment variables are properly set in `.env` files.
