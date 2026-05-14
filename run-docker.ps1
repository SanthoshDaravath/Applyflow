# ApplyFlow - Docker Compose Startup Script for Windows PowerShell
# Usage: .\run-docker.ps1

Write-Host "================================" -ForegroundColor Cyan
Write-Host "ApplyFlow - Docker Compose Start" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host ""

# Check if Docker is installed
Write-Host "Checking Docker installation..." -ForegroundColor Yellow
try {
    docker --version | Out-Null
    Write-Host "✓ Docker is installed" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker is not installed" -ForegroundColor Red
    Write-Host "Please install Docker Desktop from https://www.docker.com/products/docker-desktop" -ForegroundColor Yellow
    exit 1
}

# Check if Docker daemon is running
Write-Host "Checking Docker daemon..." -ForegroundColor Yellow
try {
    docker ps | Out-Null
    Write-Host "✓ Docker daemon is running" -ForegroundColor Green
} catch {
    Write-Host "✗ Docker daemon is not running" -ForegroundColor Red
    Write-Host "Please start Docker Desktop" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Starting ApplyFlow services..." -ForegroundColor Cyan
Write-Host "This will start PostgreSQL, Redis, RabbitMQ, Backend, and Frontend" -ForegroundColor Gray
Write-Host ""

# Start services
docker-compose up --build

Write-Host ""
Write-Host "================================" -ForegroundColor Cyan
Write-Host "ApplyFlow is now running:" -ForegroundColor Cyan
Write-Host "================================" -ForegroundColor Cyan
Write-Host "Frontend:  http://localhost:5173" -ForegroundColor Green
Write-Host "Backend:   http://localhost:8080" -ForegroundColor Green
Write-Host "Swagger:   http://localhost:8080/swagger-ui.html" -ForegroundColor Green
Write-Host "RabbitMQ:  http://localhost:15672 (guest/guest)" -ForegroundColor Green
Write-Host ""
Write-Host "Press Ctrl+C to stop all services" -ForegroundColor Yellow
Write-Host "================================" -ForegroundColor Cyan
