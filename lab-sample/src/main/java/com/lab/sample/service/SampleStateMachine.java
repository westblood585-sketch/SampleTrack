package com.lab.sample.service;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleStatus;
import com.lab.sample.exception.InvalidStateTransitionException;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Numune durum gecislerinin tek dogruluk kaynagi.
 * <pre>
 * RECEIVED    -> IN_PROGRESS, REJECTED
 * IN_PROGRESS -> COMPLETED, REJECTED
 * COMPLETED, REJECTED -> (terminal)
 * </pre>
 */
@Component
public class SampleStateMachine {

    private static final Map<SampleStatus, Set<SampleStatus>> ALLOWED = Map.of(
            SampleStatus.RECEIVED, EnumSet.of(SampleStatus.IN_PROGRESS, SampleStatus.REJECTED),
            SampleStatus.IN_PROGRESS, EnumSet.of(SampleStatus.COMPLETED, SampleStatus.REJECTED),
            SampleStatus.COMPLETED, EnumSet.noneOf(SampleStatus.class),
            SampleStatus.REJECTED, EnumSet.noneOf(SampleStatus.class));

    public boolean canTransition(SampleStatus from, SampleStatus to) {
        return ALLOWED.getOrDefault(from, Set.of()).contains(to);
    }

    public void assertTransition(SampleStatus from, SampleStatus to) {
        if (!canTransition(from, to)) {
            throw new InvalidStateTransitionException(from, to);
        }
    }
}
