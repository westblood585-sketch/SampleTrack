package com.lab.sample.service;

import com.lab.sample.dto.SampleCreateRequest;
import com.lab.sample.dto.SampleRejectRequest;
import com.lab.sample.dto.TestResultRequest;
import com.lab.sample.entity.Customer;
import com.lab.sample.entity.Sample;
import com.lab.sample.entity.SampleStatus;
import com.lab.sample.entity.SampleTest;
import com.lab.sample.entity.SampleTestStatus;
import com.lab.sample.entity.TestDefinition;
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
import com.lab.sample.repository.SampleTestRepository;
import com.lab.sample.repository.TestDefinitionRepository;
import com.lab.sample.service.impl.SampleServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SampleServiceImplTest {

    @Mock
    private SampleRepository sampleRepository;
    @Mock
    private SampleTestRepository sampleTestRepository;
    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private TestDefinitionRepository testDefinitionRepository;
    @Mock
    private SampleMapper sampleMapper;
    @Spy
    private SampleStateMachine stateMachine = new SampleStateMachine();

    @InjectMocks
    private SampleServiceImpl service;

    private static Sample sample(Long id, SampleStatus status) {
        Sample sample = new Sample();
        sample.setId(id);
        sample.setBarcode("BC-" + id);
        sample.setStatus(status);
        return sample;
    }

    private static TestDefinition definition(Long id, boolean active) {
        TestDefinition definition = new TestDefinition();
        definition.setId(id);
        definition.setCode("T" + id);
        definition.setName("Test " + id);
        definition.setActive(active);
        return definition;
    }

    // ---- Is kurali 1: ayni barkod ----

    @Test
    void create_withExistingBarcode_throwsDuplicateBarcode() {
        when(sampleRepository.existsByBarcode("BC-1")).thenReturn(true);
        SampleCreateRequest request = new SampleCreateRequest("BC-1", 1L, Set.of(1L));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(DuplicateBarcodeException.class);
        verify(sampleRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_success_buildsSampleWithTestsAndHistory() {
        when(sampleRepository.existsByBarcode("BC-1")).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(testDefinitionRepository.findAllByIdIn(anyList()))
                .thenReturn(List.of(definition(1L, true), definition(2L, true)));

        service.create(new SampleCreateRequest(" BC-1 ", 1L, Set.of(1L, 2L)));

        ArgumentCaptor<Sample> captor = ArgumentCaptor.forClass(Sample.class);
        verify(sampleRepository).saveAndFlush(captor.capture());
        Sample saved = captor.getValue();
        assertThat(saved.getBarcode()).isEqualTo("BC-1");
        assertThat(saved.getStatus()).isEqualTo(SampleStatus.RECEIVED);
        assertThat(saved.getTests()).hasSize(2);
        assertThat(saved.getHistory()).hasSize(1);
    }

    @Test
    void create_withUnknownCustomer_throwsNotFound() {
        when(sampleRepository.existsByBarcode("BC-1")).thenReturn(false);
        when(customerRepository.findById(9L)).thenReturn(Optional.empty());
        SampleCreateRequest request = new SampleCreateRequest("BC-1", 9L, Set.of(1L));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withMissingTestDefinition_throwsNotFound() {
        when(sampleRepository.existsByBarcode("BC-1")).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(testDefinitionRepository.findAllByIdIn(anyList())).thenReturn(List.of(definition(1L, true)));
        SampleCreateRequest request = new SampleCreateRequest("BC-1", 1L, Set.of(1L, 2L));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_withInactiveTestDefinition_throwsInactive() {
        when(sampleRepository.existsByBarcode("BC-1")).thenReturn(false);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(new Customer()));
        when(testDefinitionRepository.findAllByIdIn(anyList())).thenReturn(List.of(definition(1L, false)));
        SampleCreateRequest request = new SampleCreateRequest("BC-1", 1L, Set.of(1L));

        assertThatThrownBy(() -> service.create(request)).isInstanceOf(InactiveTestDefinitionException.class);
    }

    // ---- Is kurali 2: reddedilen numuneye sonuc girilemez ----

    @Test
    void enterResult_onRejectedSample_throwsSampleRejected() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample(1L, SampleStatus.REJECTED)));
        TestResultRequest request = new TestResultRequest(BigDecimal.TEN, "teknisyen");

        assertThatThrownBy(() -> service.enterResult(1L, 5L, request)).isInstanceOf(SampleRejectedException.class);
        verifyNoInteractions(sampleTestRepository);
    }

    @Test
    void enterResult_onCompletedSample_throwsInvalidTransition() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample(1L, SampleStatus.COMPLETED)));
        TestResultRequest request = new TestResultRequest(BigDecimal.TEN, "teknisyen");

        assertThatThrownBy(() -> service.enterResult(1L, 5L, request))
                .isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void enterResult_firstResult_movesSampleToInProgressAndCompletesTest() {
        Sample sample = sample(1L, SampleStatus.RECEIVED);
        SampleTest test = new SampleTest();
        test.setId(5L);
        test.setTestDefinition(definition(1L, true));
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(sampleTestRepository.findByIdAndSampleId(5L, 1L)).thenReturn(Optional.of(test));

        service.enterResult(1L, 5L, new TestResultRequest(new BigDecimal("92.5"), " teknisyen "));

        assertThat(sample.getStatus()).isEqualTo(SampleStatus.IN_PROGRESS);
        assertThat(sample.getHistory()).hasSize(1);
        assertThat(test.getStatus()).isEqualTo(SampleTestStatus.COMPLETED);
        assertThat(test.getResult().getResultValue()).isEqualByComparingTo("92.5");
        assertThat(test.getResult().getEnteredBy()).isEqualTo("teknisyen");
    }

    @Test
    void enterResult_twiceOnSameTest_throwsAlreadyEntered() {
        SampleTest test = new SampleTest();
        test.setId(5L);
        test.setResult(new com.lab.sample.entity.TestResult());
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample(1L, SampleStatus.IN_PROGRESS)));
        when(sampleTestRepository.findByIdAndSampleId(5L, 1L)).thenReturn(Optional.of(test));
        TestResultRequest request = new TestResultRequest(BigDecimal.ONE, "teknisyen");

        assertThatThrownBy(() -> service.enterResult(1L, 5L, request))
                .isInstanceOf(ResultAlreadyEnteredException.class);
    }

    // ---- Is kurali 3: tum testler bitmeden COMPLETED olamaz ----

    @Test
    void complete_withPendingTests_throwsIncompleteTests() {
        Sample sample = sample(1L, SampleStatus.IN_PROGRESS);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(sampleTestRepository.countBySampleIdAndStatusNot(1L, SampleTestStatus.COMPLETED)).thenReturn(2L);

        assertThatThrownBy(() -> service.complete(1L)).isInstanceOf(IncompleteTestsException.class);
        assertThat(sample.getStatus()).isEqualTo(SampleStatus.IN_PROGRESS);
    }

    @Test
    void complete_whenAllTestsDone_setsCompletedAndWritesHistory() {
        Sample sample = sample(1L, SampleStatus.IN_PROGRESS);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));
        when(sampleTestRepository.countBySampleIdAndStatusNot(1L, SampleTestStatus.COMPLETED)).thenReturn(0L);

        service.complete(1L);

        assertThat(sample.getStatus()).isEqualTo(SampleStatus.COMPLETED);
        assertThat(sample.getHistory()).hasSize(1);
    }

    @Test
    void complete_onRejectedSample_throwsInvalidTransition() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample(1L, SampleStatus.REJECTED)));

        assertThatThrownBy(() -> service.complete(1L)).isInstanceOf(InvalidStateTransitionException.class);
    }

    // ---- Durum gecisleri ----

    @Test
    void reject_setsReasonAndStatus() {
        Sample sample = sample(1L, SampleStatus.RECEIVED);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));

        service.reject(1L, new SampleRejectRequest(" Hemolizli "));

        assertThat(sample.getStatus()).isEqualTo(SampleStatus.REJECTED);
        assertThat(sample.getRejectionReason()).isEqualTo("Hemolizli");
    }

    @Test
    void reject_onCompletedSample_throwsInvalidTransition() {
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample(1L, SampleStatus.COMPLETED)));
        SampleRejectRequest request = new SampleRejectRequest("gerekce");

        assertThatThrownBy(() -> service.reject(1L, request)).isInstanceOf(InvalidStateTransitionException.class);
    }

    @Test
    void start_movesReceivedToInProgress() {
        Sample sample = sample(1L, SampleStatus.RECEIVED);
        when(sampleRepository.findById(1L)).thenReturn(Optional.of(sample));

        service.start(1L);

        assertThat(sample.getStatus()).isEqualTo(SampleStatus.IN_PROGRESS);
    }

    @Test
    void getDetail_unknownId_throwsNotFound() {
        when(sampleRepository.findDetailedById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getDetail(99L)).isInstanceOf(ResourceNotFoundException.class);
    }
}
