package com.lab.sample.exception;

import org.springframework.http.HttpStatus;

/** Is kurali 4: sonucu girilmis test tanimi degistirilemez (409). */
public class TestDefinitionLockedException extends BusinessException {

    public TestDefinitionLockedException(String code) {
        super(HttpStatus.CONFLICT, "TEST_DEFINITION_LOCKED", "%s kodlu test tanımının sonucu girilmiş; artık değiştirilemez".formatted(code));
    }
}
