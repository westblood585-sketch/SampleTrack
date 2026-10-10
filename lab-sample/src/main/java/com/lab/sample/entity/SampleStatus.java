package com.lab.sample.entity;

import lombok.extern.slf4j.Slf4j;

/** Numunenin laboratuvardaki yasam dongusu asamalari. */
public enum SampleStatus {
    RECEIVED,
    IN_PROGRESS,
    COMPLETED,
    REJECTED;

    /** Terminal durumdan baska duruma gecilemez. */
    public boolean isTerminal() {
        return this == COMPLETED || this == REJECTED;
    }
}
