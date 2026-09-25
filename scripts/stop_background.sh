#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library - Stop All Background Servers
# ==============================================================================

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"

cd "$PROJECT_ROOT"

echo "=========================================================="
echo "  Sanjay Library - Stopping Background Services"
echo "=========================================================="

docker compose down

echo "✅ All containers stopped successfully."
