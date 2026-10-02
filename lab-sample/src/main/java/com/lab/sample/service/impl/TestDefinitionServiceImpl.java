package com.lab.sample.service.impl;

import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.dto.TestDefinitionResponse;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.exception.DuplicateResourceException;
import com.lab.sample.exception.InvalidReferenceRangeException;
import com.lab.sample.exception.ResourceNotFoundException;
import com.lab.sample.exception.TestDefinitionLockedException;
import com.lab.sample.mapper.TestDefinitionMapper;
import com.lab.sample.repository.SampleTestRepository;
import com.lab.sample.repository.TestDefinitionRepository;
import com.lab.sample.service.TestDefinitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class TestDefinitionServiceImpl implements TestDefinitionService {

    private static final String RESOURCE = "Test tanımı";

    private final TestDefinitionRepository testDefinitionRepository;
    private final SampleTestRepository sampleTestRepository;
    private final TestDefinitionMapper testDefinitionMapper;

    @Override
    public TestDefinitionResponse create(TestDefinitionRequest request) {
        validateRange(request);
        if (testDefinitionRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Test kodu", request.code());
        }
        TestDefinition saved = testDefinitionRepository.save(testDefinitionMapper.toEntity(request));
        return testDefinitionMapper.toResponse(saved);
    }

    @Override
    public TestDefinitionResponse update(Long id, TestDefinitionRequest request) {
        validateRange(request);
        TestDefinition definition = findOrThrow(id);
        // Is kurali 4: sonucu girilmis bir teste bagli tanim degistirilemez.
        if (sampleTestRepository.existsByTestDefinitionIdAndResultIsNotNull(id)) {
            throw new TestDefinitionLockedException(definition.getCode());
        }
        if (!definition.getCode().equals(request.code()) && testDefinitionRepository.existsByCode(request.code())) {
            throw new DuplicateResourceException("Test kodu", request.code());
        }
        testDefinitionMapper.updateEntity(request, definition);
        return testDefinitionMapper.toResponse(definition);
    }

    @Override
    public TestDefinitionResponse setActive(Long id, boolean active) {
        TestDefinition definition = findOrThrow(id);
        definition.setActive(active);
        return testDefinitionMapper.toResponse(definition);
    }

    @Override
    @Transactional(readOnly = true)
    public TestDefinitionResponse get(Long id) {
        return testDefinitionMapper.toResponse(findOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TestDefinitionResponse> list(boolean onlyActive) {
        List<TestDefinition> definitions = onlyActive
                ? testDefinitionRepository.findByActiveTrueOrderByCodeAsc()
                : testDefinitionRepository.findAll();
        return definitions.stream().map(testDefinitionMapper::toResponse).toList();
    }

    private TestDefinition findOrThrow(Long id) {
        return testDefinitionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(RESOURCE, id));
    }

    private void validateRange(TestDefinitionRequest request) {
        if (request.refMin() != null && request.refMax() != null
                && request.refMin().compareTo(request.refMax()) > 0) {
            throw new InvalidReferenceRangeException();
        }
    }
}
