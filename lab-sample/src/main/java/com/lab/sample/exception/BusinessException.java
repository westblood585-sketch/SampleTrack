package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/** Tum is kurali hatalarinin ortak atasi; HTTP durumu ve makine-okunur hata kodu tasir. */
@Getter
public abstract class BusinessException extends RuntimeException {

    private final transient HttpStatus status;
    private final String code;

    protected BusinessException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    protected BusinessException(HttpStatus status, String code, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
        this.code = code;
    }
}
