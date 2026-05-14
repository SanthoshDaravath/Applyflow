# ApplyFlow AI - Quick Start Reference Card

## 🚀 QUICKEST START (Recommended)

```powershell
# Copy these commands into PowerShell one at a time

cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow

# Run everything with Docker
docker-compose up --build
```

**Wait ~3-5 minutes**, then open:
- http://localhost:5173 (Frontend)
- http://localhost:8080/swagger-ui.html (API Docs)

**Stop**: Press `Ctrl+C`

---

## 🛠 LOCAL DEVELOPMENT (Need hot-reload?)

### Terminal 1: Services
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow
docker-compose up -d postgres redis rabbitmq
```

### Terminal 2: Backend
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\backend
mvn spring-boot:run
```

### Terminal 3: Frontend
```powershell
cd C:\Users\santh\Downloads\BankingPortal-API-main\Applyflow\frontend
npm install
npm run dev
```

---

## 📋 Prerequisites Checklist

- [ ] Docker Desktop installed? https://docker.com/download
- [ ] Port 5173 free?
- [ ] Port 8080 free?

For local dev also need:
- [ ] Java 21? (`java -version`)
- [ ] Maven 3.9+? (`mvn -version`)
- [ ] Node.js 20+? (`node --version`)
- [ ] npm 10+? (`npm --version`)

---

## 🌐 URLs After Startup

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend | http://localhost:8080 |
| API Docs | http://localhost:8080/swagger-ui.html |
| RabbitMQ Admin | http://localhost:15672 (guest/guest) |
| Health Check | http://localhost:8080/actuator/health |

---

## 👤 Test Account

After startup, register:
- **Email**: test@example.com
- **Password**: Test@1234
- **Name**: Test User

---

## 📊 Services Running (Docker)

```
postgres:5432        Database
redis:6379           Cache
rabbitmq:5672        Message Queue
backend:8080         API Server
frontend:5173        Web App
```

---

## 🔍 Verify It's Working

```powershell
# Check backend health
curl http://localhost:8080/actuator/health

# Check frontend
Start-Process http://localhost:5173

# View docker logs
docker-compose logs -f backend
```

---

## ⚠️ Common Issues Quick Fixes

**Port already in use**:
```powershell
# Find what's using port 8080
netstat -ano | findstr :8080
# Kill it (replace PID with actual number)
taskkill /PID <PID> /F
```

**Backend won't start**:
```powershell
# Restart database
docker-compose restart postgres
# Wait 10 seconds
# Retry backend
```

**Frontend shows blank page**:
```powershell
# Clear browser cache (Ctrl+Shift+Delete)
# Restart frontend server
# Hard refresh browser (Ctrl+F5)
```

**Maven not found**:
- Install from https://maven.apache.org/download.cgi
- Add to PATH
- Restart PowerShell

**Java wrong version**:
- Install Java 21: https://adoptium.net/
- Set `JAVA_HOME` environment variable
- Restart PowerShell

---

## 📖 Documentation

- **Full Guide**: See `STARTUP_GUIDE.md`
- **Technical Details**: See `SETUP_SUMMARY.md`
- **Complete Report**: See `STARTUP_REPORT.md`

---

## 🎯 Common Tasks

### Stop everything
```powershell
docker-compose down
```

### Clean rebuild
```powershell
docker-compose down -v
docker-compose up --build
```

### View all logs
```powershell
docker-compose logs -f
```

### Backend logs only
```powershell
docker-compose logs -f backend
```

### Database shell
```powershell
docker-compose exec postgres psql -U applyflow
```

### Clear npm cache
```powershell
cd frontend
npm cache clean --force
npm install
```

### Rebuild backend only
```powershell
docker-compose up -d --build backend
```

---

## ✅ Success Indicators

- ✅ Backend logs show "Started ApplyFlowApplication in X seconds"
- ✅ Frontend shows login page at http://localhost:5173
- ✅ Can register new user account
- ✅ Can see dashboard after login
- ✅ Swagger UI shows 40+ API endpoints

---

## 🆘 Still Having Issues?

1. Read `STARTUP_GUIDE.md` section "Troubleshooting"
2. Check Docker is running: `docker ps`
3. Check ports are free: `netstat -ano | findstr :5173`
4. Check logs: `docker-compose logs --tail=50 backend`
5. Restart everything: `docker-compose down -v && docker-compose up --build`

---

## 📞 Need Help?

See the detailed guides:
- `STARTUP_GUIDE.md` - Complete instructions & troubleshooting
- `SETUP_SUMMARY.md` - Technical architecture & configuration
- `STARTUP_REPORT.md` - Full setup report & reference

---

**Ready to start? → Run: `docker-compose up --build`**
