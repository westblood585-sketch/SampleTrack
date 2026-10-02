package com.lab.sample.repository;

import com.lab.sample.entity.Customer;
import com.lab.sample.entity.Sample;
import com.lab.sample.entity.SampleStatus;
import com.lab.sample.entity.SampleTest;
import com.lab.sample.entity.SampleTestStatus;
import com.lab.sample.entity.TestDefinition;
import com.lab.sample.entity.TestResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class SampleRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SampleRepository sampleRepository;
    @Autowired
    private SampleTestRepository sampleTestRepository;
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TestDefinitionRepository testDefinitionRepository;
    @Autowired
    private TestEntityManager em;

    private Customer customer;
    private TestDefinition glucose;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setName("Test Klinigi");
        customer.setEmail("klinik-" + System.nanoTime() + "@test.com");
        customer = customerRepository.saveAndFlush(customer);

        glucose = new TestDefinition();
        glucose.setCode("GLU-" + System.nanoTime());
        glucose.setName("Glukoz");
        glucose.setUnit("mg/dL");
        glucose.setRefMin(new BigDecimal("70"));
        glucose.setRefMax(new BigDecimal("100"));
        glucose = testDefinitionRepository.saveAndFlush(glucose);
    }

    private Sample newSample(String barcode) {
        Sample sample = new Sample();
        sample.setBarcode(barcode);
        sample.setCustomer(customer);
        return sample;
    }

    private SampleTest newSampleTest() {
        SampleTest test = new SampleTest();
        test.setTestDefinition(glucose);
        return test;
    }

    @Test
    void existsByBarcode_returnsTrueAfterSave() {
        sampleRepository.saveAndFlush(newSample("BC-001"));

        assertThat(sampleRepository.existsByBarcode("BC-001")).isTrue();
        assertThat(sampleRepository.existsByBarcode("BC-XXX")).isFalse();
    }

    @Test
    void duplicateBarcode_violatesUniqueConstraint() {
        sampleRepository.saveAndFlush(newSample("BC-DUP"));
        Sample duplicate = newSample("BC-DUP");

        assertThatThrownBy(() -> sampleRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void savingSample_cascadesTestsAndHistory_andDetailedFetchLoadsThem() {
        Sample sample = newSample("BC-002");
        sample.addTest(newSampleTest());
        sample.addHistory(null, SampleStatus.RECEIVED, "Kabul edildi");
        Sample saved = sampleRepository.saveAndFlush(sample);
        em.clear();

        Sample found = sampleRepository.findDetailedById(saved.getId()).orElseThrow();

        assertThat(found.getStatus()).isEqualTo(SampleStatus.RECEIVED);
        assertThat(found.getReceivedAt()).isNotNull();
        assertThat(found.getTests()).hasSize(1);
        assertThat(found.getTests().get(0).getTestDefinition().getName()).isEqualTo("Glukoz");
        assertThat(found.getHistory()).hasSize(1);
    }

    @Test
    void countBySampleIdAndStatusNot_countsOnlyUnfinishedTests() {
        Sample sample = newSample("BC-003");
        SampleTest test = newSampleTest();
        sample.addTest(test);
        Sample saved = sampleRepository.saveAndFlush(sample);

        assertThat(sampleTestRepository
                .countBySampleIdAndStatusNot(saved.getId(), SampleTestStatus.COMPLETED)).isEqualTo(1);

        test.setStatus(SampleTestStatus.COMPLETED);
        sampleTestRepository.saveAndFlush(test);

        assertThat(sampleTestRepository
                .countBySampleIdAndStatusNot(saved.getId(), SampleTestStatus.COMPLETED)).isZero();
    }

    @Test
    void existsByTestDefinitionIdAndResultIsNotNull_trueOnlyAfterResultEntered() {
        Sample sample = newSample("BC-004");
        SampleTest test = newSampleTest();
        sample.addTest(test);
        sampleRepository.saveAndFlush(sample);

        assertThat(sampleTestRepository
                .existsByTestDefinitionIdAndResultIsNotNull(glucose.getId())).isFalse();

        TestResult result = new TestResult();
        result.setResultValue(new BigDecimal("92.5"));
        result.setEnteredBy("teknisyen");
        test.setResult(result);
        sampleTestRepository.saveAndFlush(test);

        assertThat(sampleTestRepository
                .existsByTestDefinitionIdAndResultIsNotNull(glucose.getId())).isTrue();
    }
}
