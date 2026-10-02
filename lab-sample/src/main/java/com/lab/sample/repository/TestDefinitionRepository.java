package com.lab.sample.repository;

import com.lab.sample.entity.TestDefinition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TestDefinitionRepository extends JpaRepository<TestDefinition, Long> {

    boolean existsByCode(String code);

    Optional<TestDefinition> findByCode(String code);

    List<TestDefinition> findByActiveTrueOrderByCodeAsc();

    List<TestDefinition> findAllByIdIn(List<Long> ids);
}
