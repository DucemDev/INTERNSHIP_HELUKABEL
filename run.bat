@echo off
title HELUKABEL Application Launcher
echo ===================================================
echo   HELUKABEL SPRING BOOT APPLICATION RUNNER
echo ===================================================
echo.
echo [1/3] Khoi chay Spring Boot server...
start "HELUKABEL Server Console" cmd /k mvnw.cmd spring-boot:run

echo [2/3] Dang doi server khoi dong tren cong 8080...
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
echo [3/3] Server da san sang! Tu dong mo trinh duyet...
start http://localhost:8080/
echo.
echo ===================================================
echo Khoi chay thanh cong!
echo Cua so lenh chay Server van tiep tuc hoat dong o nen.
echo ===================================================
timeout /t 3 >nul
exit
