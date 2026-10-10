package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

/** Benzersiz olmasi gereken bir deger tekrar edildi (409). */
public class DuplicateResourceException extends BusinessException {

    public DuplicateResourceException(String resource, Object value) {
        super(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", "%s zaten kayıtlı: %s".formatted(resource, value));
    }
}
