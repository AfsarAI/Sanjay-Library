#!/usr/bin/env bash
# ==============================================================================
# Sanjay Library Management System - Database Restore Script
# Restores a compressed pg_dump backup (.sql.gz) into PostgreSQL.
# ==============================================================================

set -euo pipefail

CONTAINER_NAME="${DB_CONTAINER:-digital_library_postgres}"
DB_NAME="${DB_NAME:-digital_library}"
DB_USER="${DB_USER:-postgres}"

if [ $# -lt 1 ]; then
    echo "Usage: $0 <path_to_backup_file.sql.gz>"
    echo "Example: $0 backups/sanjay_library_digital_library_20260925_120000.sql.gz"
    exit 1
fi

BACKUP_FILE="$1"

if [ ! -f "${BACKUP_FILE}" ]; then
    echo "[ERROR] Backup file not found: ${BACKUP_FILE}" >&2
    exit 1
fi

echo "========================================================"
echo "Sanjay Library - Database Restore"
echo "Backup File: ${BACKUP_FILE}"
echo "Target DB: ${DB_NAME}"
echo "========================================================"

if docker ps --format '{{.Names}}' | grep -q "^${CONTAINER_NAME}$"; then
    echo "[INFO] Restoring to Docker container: ${CONTAINER_NAME}..."
    gunzip -c "${BACKUP_FILE}" | docker exec -i "${CONTAINER_NAME}" psql -U "${DB_USER}" -d "${DB_NAME}"
elif command -v psql >/dev/null 2>&1; then
    echo "[INFO] Restoring via local psql..."
    PGPASSWORD="${DB_PASSWORD:-postgres_secure_password}" gunzip -c "${BACKUP_FILE}" | psql -h "${DB_HOST:-localhost}" -p "${DB_PORT:-5432}" -U "${DB_USER}" -d "${DB_NAME}"
else
    echo "[ERROR] Neither Docker container '${CONTAINER_NAME}' nor local 'psql' utility found." >&2
    exit 1
fi

echo "[SUCCESS] Database restored successfully from ${BACKUP_FILE}"
echo "========================================================"
