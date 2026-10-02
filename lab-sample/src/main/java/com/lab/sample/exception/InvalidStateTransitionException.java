package com.lab.sample.exception;

import com.lab.sample.entity.SampleStatus;
import org.springframework.http.HttpStatus;

/** Durum makinesinin izin vermedigi gecis (422). */
public class InvalidStateTransitionException extends BusinessException {

    public InvalidStateTransitionException(SampleStatus from, SampleStatus to) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION",
                "Numune durumu %s → %s geçişine izin verilmiyor".formatted(from, to));
    }

    public InvalidStateTransitionException(String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, "INVALID_STATE_TRANSITION", message);
    }
}
