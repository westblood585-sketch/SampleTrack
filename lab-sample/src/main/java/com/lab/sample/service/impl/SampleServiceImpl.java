package com.lab.sample.service.impl;

import com.lab.sample.dto.SampleCreateRequest;
import com.lab.sample.dto.SampleDetailResponse;
import com.lab.sample.dto.SampleRejectRequest;
import com.lab.sample.dto.SampleSummaryResponse;
import com.lab.sample.dto.SampleTestResponse;
import com.lab.sample.dto.TestResultRequest;
import com.lab.sample.entity.Customer;
import com.lab.sample.entity.Sample;
import com.lab.sample.entity.SampleStatus;
import com.lab.sample.entity.SampleTest;
import com.lab.sample.entity.SampleTestStatus;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.entity.TestResult;
import com.lab.sample.exception.DuplicateBarcodeException;
import com.lab.sample.exception.IncompleteTestsException;
import com.lab.sample.exception.InactiveTestDefinitionException;
import com.lab.sample.exception.InvalidStateTransitionException;
import com.lab.sample.exception.ResourceNotFoundException;
import com.lab.sample.exception.ResultAlreadyEnteredException;
import com.lab.sample.exception.SampleRejectedException;
import com.lab.sample.mapper.SampleMapper;
import com.lab.sample.repository.CustomerRepository;
import com.lab.sample.repository.SampleRepository;
import com.lab.sample.repository.SampleSpecifications;
import com.lab.sample.repository.SampleTestRepository;
import com.lab.sample.repository.TestDefinitionRepository;
import com.lab.sample.service.SampleService;
import com.lab.sample.service.SampleStateMachine;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SampleServiceImpl implements SampleService {

    private static final String SAMPLE = "Numune";

    private final SampleRepository sampleRepository;
    private final SampleTestRepository sampleTestRepository;
    private final CustomerRepository customerRepository;
    private final TestDefinitionRepository testDefinitionRepository;
    private final SampleMapper sampleMapper;
    private final SampleStateMachine stateMachine;

    @Override
    public SampleDetailResponse create(SampleCreateRequest request) {
        String barcode = request.barcode().trim();
        // Is kurali 1: once hizli kontrol; yarisma durumunda DB unique kisiti son savunma hattidir.
        if (sampleRepository.existsByBarcode(barcode)) {
            throw new DuplicateBarcodeException(barcode);
        }
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Müşteri", request.customerId()));
        List<TestDefinition> definitions = resolveDefinitions(request.testDefinitionIds());

        Sample sample = new Sample();
        sample.setBarcode(barcode);
        sample.setCustomer(customer);
        sample.setStatus(SampleStatus.RECEIVED);
        for (TestDefinition definition : definitions) {
            SampleTest test = new SampleTest();
            test.setTestDefinition(definition);
            sample.addTest(test);
        }
        sample.addHistory(null, SampleStatus.RECEIVED, "Numune kabul edildi");

        try {
            sampleRepository.saveAndFlush(sample);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateBarcodeException(barcode, ex);
        }
        return sampleMapper.toDetail(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public SampleDetailResponse getDetail(Long id) {
        Sample sample = sampleRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SAMPLE, id));
        return sampleMapper.toDetail(sample);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SampleSummaryResponse> search(SampleStatus status, Long customerId, String barcode,
                                              Pageable pageable) {
        return sampleRepository
                .findAll(SampleSpecifications.withFilters(status, customerId, barcode), pageable)
                .map(sampleMapper::toSummary);
    }

    @Override
    public SampleDetailResponse start(Long id) {
        Sample sample = findOrThrow(id);
        changeStatus(sample, SampleStatus.IN_PROGRESS, "Analiz başlatıldı");
        sampleRepository.saveAndFlush(sample);
        return sampleMapper.toDetail(sample);
    }

    @Override
    public SampleDetailResponse reject(Long id, SampleRejectRequest request) {
        Sample sample = findOrThrow(id);
        String reason = request.reason().trim();
        changeStatus(sample, SampleStatus.REJECTED, reason);
        sample.setRejectionReason(reason);
        sampleRepository.saveAndFlush(sample);
        return sampleMapper.toDetail(sample);
    }

    @Override
    public SampleDetailResponse complete(Long id) {
        Sample sample = findOrThrow(id);
        // Is kurali 3: terminal olmayan bir numunede bitmemis test varsa COMPLETED olamaz.
        if (!sample.getStatus().isTerminal()) {
            long pending = sampleTestRepository.countBySampleIdAndStatusNot(id, SampleTestStatus.COMPLETED);
            if (pending > 0) {
                throw new IncompleteTestsException(pending);
            }
        }
        changeStatus(sample, SampleStatus.COMPLETED, "Tüm testler tamamlandı");
        sampleRepository.saveAndFlush(sample);
        return sampleMapper.toDetail(sample);
    }

    @Override
    public SampleTestResponse enterResult(Long sampleId, Long sampleTestId, TestResultRequest request) {
        Sample sample = findOrThrow(sampleId);
        // Is kurali 2: reddedilen numuneye sonuc girilemez.
        if (sample.getStatus() == SampleStatus.REJECTED) {
            throw new SampleRejectedException(sample.getBarcode());
        }
        if (sample.getStatus() == SampleStatus.COMPLETED) {
            throw new InvalidStateTransitionException("Tamamlanmış numuneye sonuç girilemez");
        }
        SampleTest test = sampleTestRepository.findByIdAndSampleId(sampleTestId, sampleId)
                .orElseThrow(() -> new ResourceNotFoundException("Numune testi", sampleTestId));
        if (test.getResult() != null) {
            throw new ResultAlreadyEnteredException(sampleTestId);
        }

        TestResult result = new TestResult();
        result.setResultValue(request.value());
        result.setEnteredBy(request.enteredBy().trim());
        test.setResult(result);
        test.setStatus(SampleTestStatus.COMPLETED);

        if (sample.getStatus() == SampleStatus.RECEIVED) {
            changeStatus(sample, SampleStatus.IN_PROGRESS, "İlk sonuç girildi");
            sampleRepository.saveAndFlush(sample);
        }
        sampleTestRepository.saveAndFlush(test);
        return sampleMapper.toTestResponse(test);
    }

    private void changeStatus(Sample sample, SampleStatus target, String note) {
        SampleStatus from = sample.getStatus();
        stateMachine.assertTransition(from, target);
        sample.setStatus(target);
        sample.addHistory(from, target, note);
    }

    private Sample findOrThrow(Long id) {
        return sampleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(SAMPLE, id));
    }

    private List<TestDefinition> resolveDefinitions(Set<Long> requestedIds) {
        Set<Long> ids = new LinkedHashSet<>(requestedIds);
        List<TestDefinition> found = testDefinitionRepository.findAllByIdIn(new ArrayList<>(ids));
        Set<Long> foundIds = found.stream().map(TestDefinition::getId).collect(Collectors.toSet());
        List<Long> missing = ids.stream().filter(id -> !foundIds.contains(id)).toList();
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Test tanımı", missing);
        }
        List<String> inactive = found.stream()
                .filter(definition -> !definition.isActive())
                .map(TestDefinition::getCode)
                .toList();
        if (!inactive.isEmpty()) {
            throw new InactiveTestDefinitionException(inactive);
        }
        return found;
    }
}
