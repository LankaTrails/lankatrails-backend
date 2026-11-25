#!/bin/sh
# Runs the backend with hot reload inside the dev container.
# Maven dependencies live in the maven_repo volume, so only the first start downloads them.
set -e
cd /app

echo "[dev] initial compile..."
mvn -B -q -DskipTests compile

# Recompile whenever a source or resource file changes. DevTools watches target/classes
# and restarts the application context. Polling works with any bind mount (macOS included).
(
  stamp=/tmp/.last-compile
  touch "$stamp"
  while sleep 2; do
    if [ -n "$(find src/main -newer "$stamp" -type f -print -quit)" ]; then
      touch "$stamp"
      echo "[dev] change detected, recompiling..."
      mvn -B -q -o -DskipTests compile || echo "[dev] compile failed; fix the error and save again"
    fi
  done
) &

exec mvn -B -DskipTests spring-boot:run
