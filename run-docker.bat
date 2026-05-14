@echo off
REM ApplyFlow - Docker Compose Startup Script for Windows Command Prompt
REM Usage: run-docker.bat

echo.
echo ================================
echo ApplyFlow - Docker Compose Start
echo ================================
echo.

REM Check if Docker is installed
echo Checking Docker installation...
docker --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker is not installed
    echo Please install Docker Desktop from https://www.docker.com/products/docker-desktop
    pause
    exit /b 1
)
echo OK: Docker is installed

REM Check if Docker daemon is running
echo Checking Docker daemon...
docker ps >nul 2>&1
if errorlevel 1 (
    echo ERROR: Docker daemon is not running
    echo Please start Docker Desktop
    pause
    exit /b 1
)
echo OK: Docker daemon is running

echo.
echo Starting ApplyFlow services...
echo This will start PostgreSQL, Redis, RabbitMQ, Backend, and Frontend
echo.

REM Start services
docker-compose up --build

echo.
echo ================================
echo ApplyFlow is now running:
echo ================================
echo Frontend:  http://localhost:5173
echo Backend:   http://localhost:8080
echo Swagger:   http://localhost:8080/swagger-ui.html
echo RabbitMQ:  http://localhost:15672 (guest/guest)
echo.
echo Press Ctrl+C to stop all services
echo ================================
pause
