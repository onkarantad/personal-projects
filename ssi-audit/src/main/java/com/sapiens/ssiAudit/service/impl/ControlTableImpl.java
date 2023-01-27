package com.sapiens.ssiAudit.service.impl;

import com.sapiens.ssiAudit.model.ControlTable;
import com.sapiens.ssiAudit.repository.ControlTableRepository;
import com.sapiens.ssiAudit.service.ControlTableService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.concurrent.CompletableFuture;

@Service
public class ControlTableImpl implements ControlTableService {
    @Autowired
    ControlTableRepository controlTableRepository;

    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public ControlTable saveOrUpdate(ControlTable controlTable) {
        return controlTableRepository.save(controlTable);
    }

    @Override
    @Async
    public CompletableFuture<ControlTable> saveAsync(ControlTable controlTable) {
        return CompletableFuture.completedFuture(controlTableRepository.save(controlTable));
    }

    @Override
    @Async
    public CompletableFuture<ControlTable> persistAsync(ControlTable controlTable) {
        return CompletableFuture.completedFuture(controlTableRepository.persist(entityManager,controlTable));
    }
}
