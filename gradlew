#!/bin/sh
set -e

APP_HOME="$(cd "$(dirname "$0")" && pwd)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ ! -f "$WRAPPER_JAR" ]; then
    mkdir -p "$APP_HOME/gradle/wrapper"
    echo "Downloading gradle-wrapper.jar..."
    if command -v curl >/dev/null 2>&1; then
        curl -sSL "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar" -o "$WRAPPER_JAR" || true
    elif command -v wget >/dev/null 2>&1; then
        wget -q "https://raw.githubusercontent.com/gradle/gradle/v8.6.0/gradle/wrapper/gradle-wrapper.jar" -O "$WRAPPER_JAR" || true
    fi
fi

if [ -f "$WRAPPER_JAR" ]; then
    exec java -Dorg.gradle.appname=gradlew -jar "$WRAPPER_JAR" "$@"
elif command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Error: Neither gradle-wrapper.jar nor system gradle could be executed." >&2
    exit 1
fi
