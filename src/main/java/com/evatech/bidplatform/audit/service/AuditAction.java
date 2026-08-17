package com.evatech.bidplatform.audit.service;

import java.lang.annotation.*;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditAction {
    String action();
    String entity() default "";
}
