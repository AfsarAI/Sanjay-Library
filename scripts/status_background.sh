#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library - Check Status of All Background Servers
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "$PROJECT_ROOT"

echo "=========================================================="
echo "  Sanjay Library - Background Services Status"
echo "=========================================================="

docker compose ps

echo ""
echo "🩺 Health Checks:"
echo -n "   - Spring Boot Backend (port 8080): "
curl -s -m 2 http://localhost:8080/actuator/health | grep -q "UP" && echo "🟢 UP (Healthy)" || echo "🔴 DOWN"

echo -n "   - Frontend Web Server (port 3000): "
curl -s -m 2 -o /dev/null -w "%{http_code}\n" http://localhost:3000 | grep -q "200" && echo "🟢 UP (Healthy)" || echo "🔴 DOWN"

echo -n "   - Wireless Phone ADB (port 5555):  "
adb devices | grep -q "192.168.31.164:5555" && echo "🟢 CONNECTED" || echo "🔴 DISCONNECTED"
echo "=========================================================="
