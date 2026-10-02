package com.lab.sample.mapper;

import com.lab.sample.dto.CustomerRequest;
import com.lab.sample.dto.CustomerResponse;
import com.lab.sample.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "samples", ignore = true)
    Customer toEntity(CustomerRequest request);

    CustomerResponse toResponse(Customer customer);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "samples", ignore = true)
    void updateEntity(CustomerRequest request, @MappingTarget Customer customer);
}
