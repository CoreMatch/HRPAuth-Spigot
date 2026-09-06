#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
./gradlew build
echo "Done: build/libs/HRPAuth-Proxy-1.0-SNAPSHOT.jar"
