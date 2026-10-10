package com.lab.sample.repository;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleStageHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SampleStageHistoryRepository extends JpaRepository<SampleStageHistory, Long> {

    List<SampleStageHistory> findBySampleIdOrderByChangedAtAsc(Long sampleId);
}
