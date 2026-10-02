package com.lab.sample.service;

import com.lab.sample.dto.ResultFlag;
import com.lab.sample.util.ResultFlagEvaluator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ResultFlagEvaluatorTest {

    private static final BigDecimal MIN = new BigDecimal("70");
    private static final BigDecimal MAX = new BigDecimal("100");

    @Test
    void insideRange_isNormal_includingBoundaries() {
        assertThat(ResultFlagEvaluator.evaluate(new BigDecimal("85"), MIN, MAX)).isEqualTo(ResultFlag.NORMAL);
        assertThat(ResultFlagEvaluator.evaluate(MIN, MIN, MAX)).isEqualTo(ResultFlag.NORMAL);
        assertThat(ResultFlagEvaluator.evaluate(MAX, MIN, MAX)).isEqualTo(ResultFlag.NORMAL);
    }

    @Test
    void outsideRange_isLowOrHigh() {
        assertThat(ResultFlagEvaluator.evaluate(new BigDecimal("50"), MIN, MAX)).isEqualTo(ResultFlag.LOW);
        assertThat(ResultFlagEvaluator.evaluate(new BigDecimal("150"), MIN, MAX)).isEqualTo(ResultFlag.HIGH);
    }

    @Test
    void withoutReference_isNotApplicable() {
        assertThat(ResultFlagEvaluator.evaluate(BigDecimal.ONE, null, null)).isEqualTo(ResultFlag.NOT_APPLICABLE);
    }

    @Test
    void oneSidedRange_isEvaluatedOnDefinedSide() {
        assertThat(ResultFlagEvaluator.evaluate(new BigDecimal("5"), null, new BigDecimal("3"))).isEqualTo(ResultFlag.HIGH);
        assertThat(ResultFlagEvaluator.evaluate(new BigDecimal("1"), new BigDecimal("3"), null)).isEqualTo(ResultFlag.LOW);
    }
}
