package com.sapiens.ssiAudit.service;

import com.sapiens.ssiAudit.model.AuditLog;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface AuditLogService {
    AuditLog saveOrUpdate(AuditLog auditLog);
    CompletableFuture<AuditLog> saveOrUpdateAsync(AuditLog auditLog);
    List<Object> findBusinessStartDate(String Target_Audit_Schema_Nm, int WF_NUM);
    CompletableFuture<List<Object>> findBusinessStartDateAsync(String Target_Audit_Schema_Nm, int WF_NUM);
}
