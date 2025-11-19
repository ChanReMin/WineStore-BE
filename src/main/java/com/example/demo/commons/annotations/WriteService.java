package com.example.demo.commons.annotations;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Annotation để đánh dấu các method/class sử dụng Write Database
 * Tự động set readOnly=false cho transaction
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(readOnly = false)
@Documented
public @interface WriteService {
}
