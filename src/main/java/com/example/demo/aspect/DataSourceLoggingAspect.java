package com.example.demo.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Aspect để log database routing information
 */
@Aspect
@Component
@Slf4j
public class DataSourceLoggingAspect {

    @Around("@annotation(com.example.demo.commons.annotations.ReadOnlyService) || " +
            "@annotation(com.example.demo.commons.annotations.WriteService)")
    public Object logDataSourceRouting(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        String dataSource = isReadOnly ? "READ" : "WRITE";

        log.info("→ Executing meth  od: {} | DataSource: {} | ReadOnly: {}",
                methodName, dataSource, isReadOnly);

        long startTime = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        long executionTime = System.currentTimeMillis() - startTime;

        log.info("← Completed method: {} | DataSource: {} | Time: {}ms",
                methodName, dataSource, executionTime);

        return result;
    }
}