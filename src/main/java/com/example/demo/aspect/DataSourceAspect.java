package com.example.demo.aspect;

import com.example.demo.commons.annotations.ReadOnly;
import com.example.demo.configs.DataSourceContextHolder;
import com.example.demo.commons.enums.DataSourceType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

@Aspect
@Component
@Order(1)
public class DataSourceAspect {

    @Around("@annotation(com.example.demo.commons.annotations.ReadOnly) || " +
            "@within(com.example.demo.commons.annotations.ReadOnly)")
    public Object routeDataSource(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            Method method = signature.getMethod();

            // Check if method or class has @ReadOnly annotation
            boolean isReadOnly = method.isAnnotationPresent(ReadOnly.class) ||
                    joinPoint.getTarget().getClass().isAnnotationPresent(ReadOnly.class);

            // Check if method has @Transactional with readOnly=false
            Transactional transactional = method.getAnnotation(Transactional.class);
            boolean isTransactionalWrite = transactional != null && !transactional.readOnly();

            if (isReadOnly && !isTransactionalWrite) {
                DataSourceContextHolder.setDataSourceType(DataSourceType.READ);
            } else {
                DataSourceContextHolder.setDataSourceType(DataSourceType.WRITE);
            }

            return joinPoint.proceed();
        } finally {
            DataSourceContextHolder.clearDataSourceType();
        }
    }
}
