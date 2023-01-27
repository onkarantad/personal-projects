package com.sapiens.ssiAudit.service.impl;

import com.sapiens.ssiAudit.model.AuditLog;
import com.sapiens.ssiAudit.repository.AuditLogRepository;
import com.sapiens.ssiAudit.service.AuditLogService;
import com.sapiens.ssiAudit.service.dbService.DBService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AuditLogImpl implements AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    DBService dbService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public AuditLog saveOrUpdate(AuditLog auditLog) {
        return auditLogRepository.save(auditLog);
    }

    @Override
    @Async
    public CompletableFuture<AuditLog> saveOrUpdateAsync(AuditLog auditLog) {
        return CompletableFuture.completedFuture(auditLogRepository.save(auditLog));
    }

    @Override
    public List<Object> findBusinessStartDate(String Target_Audit_Schema_Nm, int WF_NUM) {
        return auditLogRepository.findBusinessStartDate(entityManager,dbService.loadJobsBusinessStartDateQueryJPA(),WF_NUM);
    }

    @Override
    @Async
    public CompletableFuture<List<Object>> findBusinessStartDateAsync(String Target_Audit_Schema_Nm, int WF_NUM) {
        return CompletableFuture.completedFuture(auditLogRepository.findBusinessStartDate(entityManager,dbService.loadJobsBusinessStartDateQueryJPA(),WF_NUM));
    }
}
