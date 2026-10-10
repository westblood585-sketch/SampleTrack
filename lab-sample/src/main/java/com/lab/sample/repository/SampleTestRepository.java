package com.lab.sample.repository;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleTest;
import com.lab.sample.entity.SampleTestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SampleTestRepository extends JpaRepository<SampleTest, Long> {

    List<SampleTest> findBySampleId(Long sampleId);

    Optional<SampleTest> findByIdAndSampleId(Long id, Long sampleId);

    /** Is kurali 3: tamamlanmamis test sayisi 0 degilse numune COMPLETED olamaz. */
    long countBySampleIdAndStatusNot(Long sampleId, SampleTestStatus status);

    /** Is kurali 4: bu test tanimina ait sonucu girilmis herhangi bir kayit var mi? */
    boolean existsByTestDefinitionIdAndResultIsNotNull(Long testDefinitionId);
}
