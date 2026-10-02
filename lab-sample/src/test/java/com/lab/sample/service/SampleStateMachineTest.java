package com.lab.sample.service;

import com.lab.sample.entity.SampleStatus;
import com.lab.sample.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SampleStateMachineTest {

    private final SampleStateMachine stateMachine = new SampleStateMachine();

    @Test
    void allowedTransitions() {
        assertThat(stateMachine.canTransition(SampleStatus.RECEIVED, SampleStatus.IN_PROGRESS)).isTrue();
        assertThat(stateMachine.canTransition(SampleStatus.RECEIVED, SampleStatus.REJECTED)).isTrue();
        assertThat(stateMachine.canTransition(SampleStatus.IN_PROGRESS, SampleStatus.COMPLETED)).isTrue();
        assertThat(stateMachine.canTransition(SampleStatus.IN_PROGRESS, SampleStatus.REJECTED)).isTrue();
    }

    @Test
    void forbiddenTransitions() {
        assertThat(stateMachine.canTransition(SampleStatus.RECEIVED, SampleStatus.COMPLETED)).isFalse();
        assertThat(stateMachine.canTransition(SampleStatus.COMPLETED, SampleStatus.REJECTED)).isFalse();
        assertThat(stateMachine.canTransition(SampleStatus.REJECTED, SampleStatus.IN_PROGRESS)).isFalse();
    }

    @Test
    void assertTransition_throwsForForbidden() {
        assertThatThrownBy(() -> stateMachine.assertTransition(SampleStatus.REJECTED, SampleStatus.COMPLETED))
                .isInstanceOf(InvalidStateTransitionException.class);
    }
}
