package com.digitallibrary.modules.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void logAction(Long libraryId, Long actorId, String action, String entityType,
                          Long entityId, String oldValue, String newValue, String ipAddress) {
        AuditLog log = new AuditLog(libraryId, actorId, action, entityType, entityId, oldValue, newValue, ipAddress);
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Long libraryId, Pageable pageable) {
        return auditLogRepository.findByLibraryIdOrderByTimestampDesc(libraryId, pageable);
    }
}
