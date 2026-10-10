package com.lab.sample.repository;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.Sample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Filtreli liste sorgusu icin JpaSpecificationExecutor kullanilir (Faz 2'de SampleSpecifications);
 * opsiyonel parametreli JPQL, PostgreSQL'de null tip cikarimi sorunu yaratabildigi icin tercih edilmedi.
 */
public interface SampleRepository extends JpaRepository<Sample, Long>, JpaSpecificationExecutor<Sample> {

    boolean existsByBarcode(String barcode);

    Optional<Sample> findByBarcode(String barcode);

    long countByCustomerId(Long customerId);

    /** Detay ekrani: musteri + testler + test tanimlari + sonuclar tek sorguda (N+1 yok). */
    @Query("""
            select distinct s from Sample s
            join fetch s.customer
            left join fetch s.tests t
            left join fetch t.testDefinition
            left join fetch t.result
            where s.id = :id
            """)
    Optional<Sample> findDetailedById(@Param("id") Long id);
}
