#!/bin/bash

# $1 is the JAR file name
JAR_NAME=$1

if [ -z "$JAR_NAME" ]; then
    echo "Usage: ./shutdown.sh <jar_name>"
    exit 1
fi

# Find the PID of any running portfolio-backend JAR
JAR_PID=$(pgrep -f "portfolio-backend-.*\.jar")

if [ -n "$JAR_PID" ]; then
    echo "Stopping $JAR_NAME (PID: $JAR_PID)..."
    kill -15 $JAR_PID
    sleep 5
    # Force kill if still running
    if ps -p $JAR_PID > /dev/null; then
        echo "Process $JAR_PID still running, force killing..."
        kill -9 $JAR_PID
    fi
else
    echo "No running process found for $JAR_NAME"
fi
