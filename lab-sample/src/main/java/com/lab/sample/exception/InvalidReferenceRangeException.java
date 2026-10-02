package com.lab.sample.exception;

import org.springframework.http.HttpStatus;

/** refMin > refMax (400). */
public class InvalidReferenceRangeException extends BusinessException {

    public InvalidReferenceRangeException() {
        super(HttpStatus.BAD_REQUEST, "INVALID_REFERENCE_RANGE", "Referans aralığı geçersiz: minimum değer maksimumdan büyük olamaz");
    }
}
