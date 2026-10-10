package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;

/** Is kurali 3: tum testler bitmeden numune COMPLETED olamaz (422). */
public class IncompleteTestsException extends BusinessException {

    public IncompleteTestsException(long pendingCount) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INCOMPLETE_TESTS", "Tamamlanmamış %d test var; numune tamamlanamaz".formatted(pendingCount));
    }
}
