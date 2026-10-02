package com.lab.sample.repository;

import com.lab.sample.entity.Sample;
import com.lab.sample.entity.SampleStatus;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Numune listesi icin dinamik filtreler (JPA Criteria API; native SQL yok). */
public final class SampleSpecifications {

    private SampleSpecifications() {
    }

    public static Specification<Sample> withFilters(SampleStatus status, Long customerId, String barcode) {
        return (root, query, cb) -> {
            // Sayfalama sayim sorgusunda fetch join kullanilamaz; liste sorgusunda musteriyi tek seferde getir (N+1 yok).
            if (query != null && !Long.class.equals(query.getResultType())
                    && !long.class.equals(query.getResultType())) {
                root.fetch("customer", JoinType.LEFT);
            }
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (customerId != null) {
                predicates.add(cb.equal(root.get("customer").get("id"), customerId));
            }
            if (barcode != null && !barcode.isBlank()) {
                String pattern = "%" + barcode.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.like(cb.lower(root.get("barcode")), pattern));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
