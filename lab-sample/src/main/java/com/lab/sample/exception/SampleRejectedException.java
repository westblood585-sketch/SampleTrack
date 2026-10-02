package com.lab.sample.exception;

import org.springframework.http.HttpStatus;

/** Is kurali 2: reddedilen numuneye sonuc girilemez (422). */
public class SampleRejectedException extends BusinessException {

    public SampleRejectedException(String barcode) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "SAMPLE_REJECTED", "Reddedilmiş numuneye (%s) sonuç girilemez".formatted(barcode));
    }
}
