package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

/** Pasif bir test numuneye atanmaya calisildi (422). */
public class InactiveTestDefinitionException extends BusinessException {

    public InactiveTestDefinitionException(java.util.List<String> codes) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INACTIVE_TEST_DEFINITION", "Pasif test tanımları numuneye eklenemez: %s".formatted(String.join(", ", codes)));
    }
}
