package com.sapiens.ssiAudit.service;

import com.sapiens.ssiAudit.model.ControlTable;

import java.util.concurrent.CompletableFuture;

public interface ControlTableService {
    ControlTable saveOrUpdate(ControlTable controlTable);
    CompletableFuture<ControlTable> saveAsync(ControlTable controlTable);
    CompletableFuture<ControlTable> persistAsync(ControlTable controlTable);
}
