@echo off
title HELUKABEL Application Launcher
echo ===================================================
echo   HELUKABEL SPRING BOOT & CHATBOT RUNNER
echo ===================================================
echo.
echo [1/4] Khoi chay FastAPI Chatbot Service...
start "HELUKABEL Chatbot Console" cmd /k uvicorn chatbot.main:app --port 8000

echo [2/4] Khoi chay Spring Boot server...
start "HELUKABEL Server Console" cmd /k mvnw.cmd spring-boot:run

echo [3/4] Dang doi server khoi dong tren cong 8080...
echo.

:wait_loop
timeout /t 1 /nobreak >nul
netstat -ano | findstr LISTENING | findstr :8080 >nul
if %errorlevel% neq 0 (
    <nul set /p =.
    goto wait_loop
)

echo.
echo.
echo [4/4] Server da san sang! Tu dong mo trinh duyet...
start http://localhost:8080/
echo.
echo ===================================================
echo Khoi chay thanh cong!
echo Ca hai cua so lenh van tiep tuc hoat dong o nen.
echo ===================================================
timeout /t 3 >nul
exit
