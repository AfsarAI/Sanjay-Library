#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library - 1-Command Wireless Sync & Update Script for Android
# ==============================================================================
# This script:
# 1. Detects your laptop's current Wi-Fi LAN IP automatically.
# 2. Verifies ADB connection (USB or Wireless over Wi-Fi).
# 3. Ensures the Spring Boot backend is healthy and reachable.
# 4. Compiles the Flutter app with the live LAN IP configuration.
# 5. Wirelessly installs and launches "Sanjay Library" directly on your phone.
# ==============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

echo "=========================================================="
echo "  Sanjay Library - Wireless Phone Sync & Update"
echo "=========================================================="

# 1. Detect Wi-Fi LAN IP
LAN_IP=$(ip -4 route get 1.1.1.1 2>/dev/null | awk '{print $7}' || true)
if [ -z "$LAN_IP" ]; then
    LAN_IP=$(ip -4 addr show | grep -oP '(?<=inet\s)192\.168\.\d+\.\d+' | head -n 1 || true)
fi

if [ -z "$LAN_IP" ]; then
    echo "❌ ERROR: Could not detect local Wi-Fi IP address. Please check your Wi-Fi connection."
    exit 1
fi
echo "✅ Detected Laptop Wi-Fi IP: ${LAN_IP}"

# 2. Check Backend Health
echo "🔍 Checking Spring Boot backend health on port 8080..."
if curl -s -m 3 "http://localhost:8080/actuator/health" | grep -q "UP"; then
    echo "✅ Spring Boot backend is running and healthy!"
else
    echo "⚠️ Backend not responding on port 8080. Attempting to start in background..."
    cd "${PROJECT_ROOT}/backend"
    nohup java -jar target/digital-library-backend-1.0.0-SNAPSHOT.jar > /tmp/spring-boot-library.log 2>&1 &
    sleep 5
    if curl -s -m 5 "http://localhost:8080/actuator/health" | grep -q "UP"; then
        echo "✅ Spring Boot backend successfully started!"
    else
        echo "❌ Backend failed to start. Check /tmp/spring-boot-library.log."
        exit 1
    fi
fi

# 3. Detect and Connect ADB Device
echo "📱 Checking ADB device connection..."
DEVICES=$(adb devices | grep -v "List" | grep "device" | awk '{print $1}' || true)

if [ -z "$DEVICES" ]; then
    echo "⚠️ No ADB device found. Attempting wireless reconnect to 192.168.31.164:5555..."
    adb connect 192.168.31.164:5555 || true
    sleep 2
    DEVICES=$(adb devices | grep -v "List" | grep "device" | awk '{print $1}' || true)
fi

if [ -z "$DEVICES" ]; then
    echo "❌ No Android phone found via USB or Wi-Fi."
    echo "👉 Ensure USB debugging and Wireless ADB are active on your phone, then re-run."
    exit 1
fi

TARGET_DEVICE=$(echo "$DEVICES" | head -n 1)
echo "✅ Target Android device: ${TARGET_DEVICE}"

# Set up port reverse proxy for localhost compatibility
adb -s "$TARGET_DEVICE" reverse tcp:8080 tcp:8080 || true
echo "✅ Port reverse proxy active (tcp:8080 -> tcp:8080)"

# 4. Build Flutter Android APK
echo "🔨 Building Flutter APK with API_BASE_URL=http://${LAN_IP}:8080/api/v1..."
cd "${PROJECT_ROOT}/mobile"
flutter build apk --debug --dart-define=API_BASE_URL="http://${LAN_IP}:8080/api/v1"

APK_PATH="${PROJECT_ROOT}/mobile/build/app/outputs/flutter-apk/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "❌ APK build failed. ${APK_PATH} not found."
    exit 1
fi

# 5. Install APK Wirelessly onto Phone
echo "🚀 Wirelessly installing Sanjay Library onto ${TARGET_DEVICE}..."
adb -s "$TARGET_DEVICE" install -r "$APK_PATH"

# 6. Launch App on Phone Screen
echo "📱 Launching Sanjay Library on your phone screen..."
adb -s "$TARGET_DEVICE" shell am start -n com.digitallibrary.mobile/.MainActivity

echo "=========================================================="
echo "🎉 SUCCESS: Sanjay Library is updated and running on your phone!"
echo "   - Backend API: http://${LAN_IP}:8080/api/v1"
echo "   - Web PWA:     http://${LAN_IP}:3000"
echo "=========================================================="
