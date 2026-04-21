@echo off
:: Deployment Script Template
:: Copy this file to deploy-app.bat and fill in your actual values.
:: deploy-app.bat is ignored by git to protect your sensitive data.

set "PRIVATE_KEY_PATH=C:/path/to/your/key.pem"
set "SITE_DOMAIN=your-domain.com"
set "SERVER_USER=ubuntu"
set "FRONTEND_DIR=C:/path/to/your/portfolio-frontend"
set "BACKEND_BUILD_DIR=./target"
set "FRONTEND_BUILD_DIR=%FRONTEND_DIR%/dist/portfolio-frontend/browser"

:: Remote directory structure
set "REMOTE_FRONTEND_DIR=~/frontend"
set "REMOTE_BACKEND_DIR=~"
set "REMOTE_STARTUP_SCRIPT=~/startup.sh"
set "REMOTE_SHUTDOWN_SCRIPT=~/shutdown.sh"

echo --- Building Backend ---
call mvn clean package -DskipTests=true
if %ERRORLEVEL% neq 0 (
    echo Maven build failed!
    exit /b %ERRORLEVEL%
)

:: Automatically detect the newly built JAR file
for /f "delims=" %%i in ('dir /b "%BACKEND_BUILD_DIR%\portfolio-backend-*.jar" ^| findstr /v ".original"') do set "JAR_FILE_NAME=%%i"
echo Successfully built and detected: %JAR_FILE_NAME%

echo --- Uploading Scripts ---
scp -i "%PRIVATE_KEY_PATH%" -o StrictHostKeyChecking=no shutdown.sh startup.sh "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_BACKEND_DIR%/"

echo --- Stopping backend and cleaning frontend ---
ssh -i "%PRIVATE_KEY_PATH%" -o StrictHostKeyChecking=no "%SERVER_USER%@%SITE_DOMAIN%" "chmod +x %REMOTE_SHUTDOWN_SCRIPT% %REMOTE_STARTUP_SCRIPT% && %REMOTE_SHUTDOWN_SCRIPT% %JAR_FILE_NAME% && rm -rf %REMOTE_FRONTEND_DIR%/*"

echo --- Uploading frontend files ---
scp -i "%PRIVATE_KEY_PATH%" -o StrictHostKeyChecking=no -r "%FRONTEND_BUILD_DIR%/"* "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_FRONTEND_DIR%/"

echo --- Uploading backend JAR ---
scp -i "%PRIVATE_KEY_PATH%" -o StrictHostKeyChecking=no "%BACKEND_BUILD_DIR%/%JAR_FILE_NAME%" "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_BACKEND_DIR%/"

echo --- Starting backend ---
ssh -i "%PRIVATE_KEY_PATH%" -o StrictHostKeyChecking=no "%SERVER_USER%@%SITE_DOMAIN%" "%REMOTE_STARTUP_SCRIPT% %REMOTE_BACKEND_DIR%/%JAR_FILE_NAME%"

echo Deployment complete!
