package com.example.demo.configs;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class RoutingDataSource extends AbstractRoutingDataSource {
    @Override
    protected Object determineCurrentLookupKey() {
        // Kiểm tra xem transaction có đang ở chế độ read-only không
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();

        // Nếu read-only = true, route tới read database
        // Nếu read-only = false hoặc không có transaction, route tới write database
        return isReadOnly ? "read" : "write";
    }
}
