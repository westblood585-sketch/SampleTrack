package com.lab.sample.entity;

import lombok.extern.slf4j.Slf4j;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "test_result")
@Getter
@Setter
@NoArgsConstructor
public class TestResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "result_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal resultValue;

    @Column(name = "entered_at", nullable = false, updatable = false)
    private LocalDateTime enteredAt;

    @Column(name = "entered_by", nullable = false, length = 100)
    private String enteredBy;

    @PrePersist
    void onCreate() {
        if (enteredAt == null) {
            enteredAt = LocalDateTime.now();
        }
    }
}
