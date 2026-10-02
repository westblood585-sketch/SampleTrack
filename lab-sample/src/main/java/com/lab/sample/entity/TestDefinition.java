package com.lab.sample.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "test_definition")
@Getter
@Setter
@NoArgsConstructor
public class TestDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 30)
    private String unit;

    @Column(name = "ref_min", precision = 12, scale = 4)
    private BigDecimal refMin;

    @Column(name = "ref_max", precision = 12, scale = 4)
    private BigDecimal refMax;

    @Column(nullable = false)
    private boolean active = true;
}
