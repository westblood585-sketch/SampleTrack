package com.lab.sample.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Bir numune ile bir test tanimi arasindaki iliski; kendi durumu ve sonucu vardir. */
@Entity
@Table(name = "sample_test",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_sample_test_sample_definition",
                columnNames = {"sample_id", "test_definition_id"}))
@Getter
@Setter
@NoArgsConstructor
public class SampleTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sample_id", nullable = false)
    private Sample sample;

    /** updatable = false: sonuc girildikten sonra test tanimi degistirilemesin (is kurali 4, DB/JPA seviyesi). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "test_definition_id", nullable = false, updatable = false)
    private TestDefinition testDefinition;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SampleTestStatus status = SampleTestStatus.PENDING;

    /** FK bu tabloda tutulur; boylece LAZY gercekten calisir. Sonuc yoksa null. */
    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "result_id", unique = true)
    private TestResult result;
}
