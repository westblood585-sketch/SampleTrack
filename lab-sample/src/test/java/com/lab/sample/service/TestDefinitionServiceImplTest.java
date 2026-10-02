package com.lab.sample.service;

import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.exception.DuplicateResourceException;
import com.lab.sample.exception.InvalidReferenceRangeException;
import com.lab.sample.exception.TestDefinitionLockedException;
import com.lab.sample.mapper.TestDefinitionMapper;
import com.lab.sample.repository.SampleTestRepository;
import com.lab.sample.repository.TestDefinitionRepository;
import com.lab.sample.service.impl.TestDefinitionServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TestDefinitionServiceImplTest {

    @Mock
    private TestDefinitionRepository testDefinitionRepository;
    @Mock
    private SampleTestRepository sampleTestRepository;
    @Mock
    private TestDefinitionMapper testDefinitionMapper;

    @InjectMocks
    private TestDefinitionServiceImpl service;

    private static TestDefinition definition() {
        TestDefinition definition = new TestDefinition();
        definition.setId(1L);
        definition.setCode("GLU");
        definition.setName("Glukoz");
        return definition;
    }

    private static TestDefinitionRequest request(String code, String min, String max) {
        return new TestDefinitionRequest(code, "Glukoz", "mg/dL",
                min == null ? null : new BigDecimal(min), max == null ? null : new BigDecimal(max));
    }

    // ---- Is kurali 4 ----

    @Test
    void update_whenResultAlreadyEntered_throwsLocked() {
        when(testDefinitionRepository.findById(1L)).thenReturn(Optional.of(definition()));
        when(sampleTestRepository.existsByTestDefinitionIdAndResultIsNotNull(1L)).thenReturn(true);
        TestDefinitionRequest request = request("GLU", "70", "100");

        assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(TestDefinitionLockedException.class);
        verify(testDefinitionMapper, never()).updateEntity(any(), any());
    }

    @Test
    void update_whenNoResult_appliesChanges() {
        TestDefinition definition = definition();
        TestDefinitionRequest request = request("GLU", "70", "100");
        when(testDefinitionRepository.findById(1L)).thenReturn(Optional.of(definition));
        when(sampleTestRepository.existsByTestDefinitionIdAndResultIsNotNull(1L)).thenReturn(false);

        service.update(1L, request);

        verify(testDefinitionMapper).updateEntity(request, definition);
    }

    @Test
    void update_withCodeOfAnotherDefinition_throwsDuplicate() {
        when(testDefinitionRepository.findById(1L)).thenReturn(Optional.of(definition()));
        when(sampleTestRepository.existsByTestDefinitionIdAndResultIsNotNull(1L)).thenReturn(false);
        when(testDefinitionRepository.existsByCode("HGB")).thenReturn(true);
        TestDefinitionRequest request = request("HGB", null, null);

        assertThatThrownBy(() -> service.update(1L, request)).isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void setActive_togglesFlagEvenWhenLocked() {
        TestDefinition definition = definition();
        when(testDefinitionRepository.findById(1L)).thenReturn(Optional.of(definition));

        service.setActive(1L, false);

        org.assertj.core.api.Assertions.assertThat(definition.isActive()).isFalse();
    }

    // ---- Olusturma ----

    @Test
    void create_withDuplicateCode_throwsDuplicate() {
        when(testDefinitionRepository.existsByCode("GLU")).thenReturn(true);
        TestDefinitionRequest request = request("GLU", "70", "100");

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateResourceException.class);
        verify(testDefinitionRepository, never()).save(any());
    }

    @Test
    void create_withInvertedRange_throwsInvalidRange() {
        TestDefinitionRequest request = request("GLU", "100", "70");

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(InvalidReferenceRangeException.class);
    }
}
