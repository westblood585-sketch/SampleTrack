package com.lab.sample.exception;

import org.springframework.http.HttpStatus;

/** Bir teste ikinci kez sonuc girilmeye calisildi (409). */
public class ResultAlreadyEnteredException extends BusinessException {

    public ResultAlreadyEnteredException(long sampleTestId) {
        super(HttpStatus.CONFLICT, "RESULT_ALREADY_ENTERED", "Numune testi (%d) için sonuç zaten girilmiş".formatted(sampleTestId));
    }
}
