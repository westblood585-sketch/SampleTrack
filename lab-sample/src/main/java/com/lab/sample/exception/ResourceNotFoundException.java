package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

/** Istenen kayit yok (404). */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resource, Object id) {
        super(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "%s bulunamadı: %s".formatted(resource, id));
    }
}
