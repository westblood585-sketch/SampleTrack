package com.lab.sample.service;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.dto.SampleCreateRequest;
import com.lab.sample.dto.SampleDetailResponse;
import com.lab.sample.dto.SampleRejectRequest;
import com.lab.sample.dto.SampleSummaryResponse;
import com.lab.sample.dto.SampleTestResponse;
import com.lab.sample.dto.TestResultRequest;
import com.lab.sample.entity.SampleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SampleService {

    SampleDetailResponse create(SampleCreateRequest request);

    SampleDetailResponse getDetail(Long id);

    Page<SampleSummaryResponse> search(SampleStatus status, Long customerId, String barcode, Pageable pageable);

    SampleDetailResponse start(Long id);

    SampleDetailResponse reject(Long id, SampleRejectRequest request);

    SampleDetailResponse complete(Long id);

    SampleTestResponse enterResult(Long sampleId, Long sampleTestId, TestResultRequest request);
}
