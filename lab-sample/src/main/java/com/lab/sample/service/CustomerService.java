package com.lab.sample.service;

import com.lab.sample.dto.CustomerRequest;
import com.lab.sample.dto.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomerService {

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(Long id, CustomerRequest request);

    CustomerResponse get(Long id);

    Page<CustomerResponse> list(Pageable pageable);
}
