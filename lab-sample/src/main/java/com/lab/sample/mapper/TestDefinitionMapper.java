package com.lab.sample.mapper;

import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.dto.TestDefinitionResponse;
import com.lab.sample.entity.TestDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface TestDefinitionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    TestDefinition toEntity(TestDefinitionRequest request);

    TestDefinitionResponse toResponse(TestDefinition definition);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", ignore = true)
    void updateEntity(TestDefinitionRequest request, @MappingTarget TestDefinition definition);
}
