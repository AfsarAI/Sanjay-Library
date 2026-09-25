#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library - Start All Background Servers (Docker Daemon)
# ==============================================================================
# Runs PostgreSQL, Spring Boot Backend, and Flutter Web in Docker containers
# with 'restart: unless-stopped'. They run completely independent of your
# terminal, IDE, or login session.
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "$PROJECT_ROOT"

echo "=========================================================="
echo "  Sanjay Library - Starting Background Services"
echo "=========================================================="

# 1. Detect LAN IP
LAN_IP=$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7}' || true)
if [ -z "$LAN_IP" ]; then
    LAN_IP=$(ip -4 addr show | grep -oP '(?<=inet\s)192\.168\.\d+\.\d+' | head -n 1 || echo "localhost")
fi
echo "🌐 Detected Host IP: ${LAN_IP}"

# 2. Start all containers in background
echo "🚀 Starting Docker containers (PostgreSQL, Backend, Frontend)..."
docker compose up -d

# 3. Connect Wireless ADB
echo "📱 Connecting Wireless ADB to phone..."
adb connect 192.168.31.164:5555 2>/dev/null || true
adb -s 192.168.31.164:5555 reverse tcp:8080 tcp:8080 2>/dev/null || true

echo "=========================================================="
echo "🎉 ALL SERVERS ARE RUNNING INDEPENDENTLY IN BACKGROUND!"
echo "   (You can safely close the terminal or Antigravity IDE)"
echo "=========================================================="
echo "📍 Access URLs:"
echo "   - Frontend Web / PWA:  http://${LAN_IP}:3000"
echo "   - Backend API:         http://${LAN_IP}:8080/api/v1"
echo "   - Swagger UI:          http://${LAN_IP}:8080/swagger-ui/index.html"
echo "   - Health Check:        http://${LAN_IP}:8080/actuator/health"
echo "=========================================================="
docker compose ps
