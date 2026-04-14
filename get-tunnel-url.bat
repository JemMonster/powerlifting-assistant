@echo off
setlocal enabledelayedexpansion

echo ============================================
echo Get cloudpub tunnel URL
echo ============================================
echo.

REM Path to clo.exe
set "CLO_PATH=d:\android projects\android projects\clo-3.0.2-stable-windows-x86_64\clo.exe"

echo Active tunnels:
echo.
"%CLO_PATH%" ls

echo.
echo ============================================
echo To update client URL:
echo 1. Copy the URL from the list above (URL column)
echo 2. Build client with:
echo    gradlew assembleDebug -PPOWERLIFT_SERVER_BASE_URL="https://YOUR-URL/"
echo ============================================

pause
