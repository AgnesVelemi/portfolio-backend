#!/bin/bash

# $1 is the JAR file name
JAR_PATH=$1

if [ -z "$JAR_PATH" ]; then
    echo "Usage: ./startup.sh <jar_path>"
    exit 1
fi

LOG_FILE="app.log"

# nohup: 'No HangUp' - keeps the process running even after you log out of the console.
# 2>&1: Redirects error messages (stderr) to the same place as normal messages (stdout).
# &: Runs the command in the background.
echo "Starting $JAR_PATH with 'prod' profile (Port 8080)..."
nohup java -Dspring.profiles.active=prod -Xmx1024m -jar "$JAR_PATH" > "$LOG_FILE" 2>&1 &

echo "Application started in background. Logs: $LOG_FILE"
