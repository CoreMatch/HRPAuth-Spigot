#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
./gradlew build
echo "Done: build/libs/HRPAuth-Proxy-1.3.0.jar (Armorial Velocity Proxy)"
