package com.lab.sample.repository;

import com.lab.sample.entity.SampleStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SampleStageHistoryRepository extends JpaRepository<SampleStageHistory, Long> {

    List<SampleStageHistory> findBySampleIdOrderByChangedAtAsc(Long sampleId);
}
