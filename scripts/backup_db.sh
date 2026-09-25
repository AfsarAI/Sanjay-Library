#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library Management System - Automated PostgreSQL Backup Script
# Creates compressed, timestamped pg_dump backups with automated retention.
# ==============================================================================

set -euo pipefail

# Configuration
BACKUP_DIR="${BACKUP_DIR:-$(pwd)/backups}"
CONTAINER_NAME="${DB_CONTAINER:-digital_library_postgres}"
DB_NAME="${DB_NAME:-digital_library}"
DB_USER="${DB_USER:-postgres}"
RETENTION_DAYS="${RETENTION_DAYS:-14}"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")
BACKUP_FILE="${BACKUP_DIR}/sanjay_library_${DB_NAME}_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"

echo "========================================================"
echo "Sanjay Library - Database Backup"
echo "Timestamp: $(date)"
echo "Target DB: ${DB_NAME}"
echo "Backup File: ${BACKUP_FILE}"
echo "========================================================"

# Check if Docker container is running, otherwise use local pg_dump
if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "[INFO] Dumping database from running Docker container: ${CONTAINER_NAME}..."
    docker exec -t "${CONTAINER_NAME}" pg_dump -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists | gzip > "${BACKUP_FILE}"
elif command -v pg_dump >/dev/null 2>&1; then
    echo "[INFO] Dumping database via local pg_dump..."
    PGPASSWORD="${DB_PASSWORD:-postgres_secure_password}" pg_dump -h "${DB_HOST:-localhost}" -p "${DB_PORT:-5432}" -U "${DB_USER}" -d "${DB_NAME}" --clean --if-exists | gzip > "${BACKUP_FILE}"
else
    echo "[ERROR] Neither Docker container '${CONTAINER_NAME}' nor local 'pg_dump' utility found." >&2
    exit 1
fi

FILE_SIZE=$(du -h "${BACKUP_FILE}" | cut -f1)
echo "[SUCCESS] Backup created successfully: ${BACKUP_FILE} (${FILE_SIZE})"

# Automated retention cleanup
echo "[INFO] Cleaning up backups older than ${RETENTION_DAYS} days..."
find "${BACKUP_DIR}" -type f -name "sanjay_library_${DB_NAME}_*.sql.gz" -mtime +"${RETENTION_DAYS}" -exec rm -f {} +
echo "[INFO] Retention cleanup finished."
echo "========================================================"
