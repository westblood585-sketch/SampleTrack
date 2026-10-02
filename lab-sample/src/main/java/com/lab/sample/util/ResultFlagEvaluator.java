package com.lab.sample.util;

import com.lab.sample.dto.ResultFlag;

import java.math.BigDecimal;

/** Bir sonucun referans araligina gore durumunu hesaplar. */
public final class ResultFlagEvaluator {

    private ResultFlagEvaluator() {
    }

    public static ResultFlag evaluate(BigDecimal value, BigDecimal refMin, BigDecimal refMax) {
        if (value == null || (refMin == null && refMax == null)) {
            return ResultFlag.NOT_APPLICABLE;
        }
        if (refMin != null && value.compareTo(refMin) < 0) {
            return ResultFlag.LOW;
        }
        if (refMax != null && value.compareTo(refMax) > 0) {
            return ResultFlag.HIGH;
        }
        return ResultFlag.NORMAL;
    }
}
