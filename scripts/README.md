# Sanjay Library - Operations & Backup Scripts

This directory contains automated maintenance and operational scripts for the Sanjay Library database and backend.

## 1. Automated PostgreSQL Backups (`backup_db.sh`)

Creates timestamped, gzip-compressed SQL dumps (`.sql.gz`) of the PostgreSQL database and prunes archives older than the retention period (default: 14 days).

### Manual Run
```bash
chmod +x scripts/backup_db.sh
./scripts/backup_db.sh
```

Backups are placed in `./backups/sanjay_library_digital_library_<YYYYMMDD_HHMMSS>.sql.gz`.

### Automated Cron Job (Nightly at 2:00 AM IST)
Add the following line to your server's crontab (`crontab -e`):
```cron
0 2 * * * cd /home/afsarai/Library-Application && ./scripts/backup_db.sh >> /var/log/sanjay_library_backup.log 2>&1
```

---

## 2. Database Restore (`restore_db.sh`)

Restores a compressed `.sql.gz` dump into PostgreSQL.

### Usage
```bash
chmod +x scripts/restore_db.sh
./scripts/restore_db.sh backups/sanjay_library_digital_library_20260925_120000.sql.gz
```

---

## Environment Variables

| Variable | Default Value | Description |
|---|---|---|
| `DB_CONTAINER` | `digital_library_postgres` | Docker container name |
| `DB_NAME` | `digital_library` | Database name |
| `DB_USER` | `postgres` | Database superuser |
| `RETENTION_DAYS` | `14` | Backup retention threshold |
| `BACKUP_DIR` | `$(pwd)/backups` | Target backup directory |
