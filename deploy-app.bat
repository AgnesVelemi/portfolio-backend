:: ssh -i C:/Users/User/.ssh/portfolio-siample-key.pem ubuntu@13.63.37.93
:: C:\Users\User>  cd c:/ws/2026/portfolio-backend
:: .\deploy-app.bat

@echo off
set "PRIVATE_KEY_PATH=C:/Users/User/.ssh/portfolio-siample-key.pem"
::set "SITE_DOMAIN=13.63.37.93"
set "SITE_DOMAIN=portfolio-frontend.aws.siample.dev"
set "SERVER_USER=ubuntu"
set "FRONTEND_DIR=C:/ws/2026/portfolio-frontend"
set "BACKEND_BUILD_DIR=./target"
set "FRONTEND_BUILD_DIR=%FRONTEND_DIR%/dist/portfolio-frontend/browser"
:: set "JAR_FILE_NAME=portfolio-backend-1.0.1.jar"
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

:: Automatically detect the newly built JAR file (ignoring the .original file)
for /f "delims=" %%i in ('dir /b "%BACKEND_BUILD_DIR%\portfolio-backend-*.jar" ^| findstr /v ".original"') do set "JAR_FILE_NAME=%%i"
echo Successfully built and detected: %JAR_FILE_NAME%

echo --- Uploading Scripts ---
scp -i "%PRIVATE_KEY_PATH%" shutdown.sh startup.sh "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_BACKEND_DIR%/"
ssh -i "%PRIVATE_KEY_PATH%" "%SERVER_USER%@%SITE_DOMAIN%" "chmod +x %REMOTE_SHUTDOWN_SCRIPT% %REMOTE_STARTUP_SCRIPT%"

echo --- Stopping existing backend ---
ssh -i "%PRIVATE_KEY_PATH%" "%SERVER_USER%@%SITE_DOMAIN%" "%REMOTE_SHUTDOWN_SCRIPT% %JAR_FILE_NAME%"

echo --- Cleaning remote frontend directory ---
ssh -i "%PRIVATE_KEY_PATH%" "%SERVER_USER%@%SITE_DOMAIN%" "rm -rf %REMOTE_FRONTEND_DIR%/*"

echo --- Uploading frontend files ---
:: Note: Angular builds usually go to dist/project-name/browser or similar.
:: If the path is different, adjust FRONTEND_BUILD_DIR above.
scp -i "%PRIVATE_KEY_PATH%" -r "%FRONTEND_BUILD_DIR%/"* "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_FRONTEND_DIR%/"

echo --- Uploading backend JAR ---
scp -i "%PRIVATE_KEY_PATH%" "%BACKEND_BUILD_DIR%/%JAR_FILE_NAME%" "%SERVER_USER%@%SITE_DOMAIN%:%REMOTE_BACKEND_DIR%/"

echo --- Starting backend ---
ssh -i "%PRIVATE_KEY_PATH%" "%SERVER_USER%@%SITE_DOMAIN%" "%REMOTE_STARTUP_SCRIPT% %REMOTE_BACKEND_DIR%/%JAR_FILE_NAME%"

echo Deployment complete!
