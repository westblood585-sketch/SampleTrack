package com.lab.sample.exception;

import org.springframework.http.HttpStatus;

/** Is kurali 1: ayni barkod iki kez olusturulamaz (409). */
public class DuplicateBarcodeException extends BusinessException {

    public DuplicateBarcodeException(String barcode) {
        super(HttpStatus.CONFLICT, "DUPLICATE_BARCODE", "Bu barkod zaten kayıtlı: %s".formatted(barcode));
    }

    public DuplicateBarcodeException(String barcode, Throwable cause) {
        super(HttpStatus.CONFLICT, "DUPLICATE_BARCODE", "Bu barkod zaten kayıtlı: %s".formatted(barcode), cause);
    }
}
