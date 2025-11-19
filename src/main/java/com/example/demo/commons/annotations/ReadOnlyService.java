package com.example.demo.commons.annotations;

import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Annotation để đánh dấu các method/class sử dụng Read Database
 * Tự động set readOnly=true cho transaction
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Transactional(readOnly = true)
@Documented
public @interface ReadOnlyService {
}
