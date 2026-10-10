package com.lab.sample.entity;

import lombok.extern.slf4j.Slf4j;

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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sample")
@Getter
@Setter
@NoArgsConstructor
public class Sample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Is kurali 1: ayni barkod iki kez olusturulamaz (DB seviyesinde unique). */
    @Column(nullable = false, unique = true, length = 64)
    private String barcode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SampleStatus status = SampleStatus.RECEIVED;

    @Column(name = "received_at", nullable = false, updatable = false)
    private LocalDateTime receivedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SampleTest> tests = new ArrayList<>();

    @OneToMany(mappedBy = "sample", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("changedAt ASC")
    private List<SampleStageHistory> history = new ArrayList<>();

    @PrePersist
    void onCreate() {
        if (receivedAt == null) {
            receivedAt = LocalDateTime.now();
        }
    }

    /** Iki yonlu iliskiyi tutarli tutar. */
    public void addTest(SampleTest test) {
        tests.add(test);
        test.setSample(this);
    }

    /** Durum degisikligini gecmise ekler. Durumu kendisi degistirmez; gecis kurallari serviste (Faz 2). */
    public void addHistory(SampleStatus from, SampleStatus to, String note) {
        SampleStageHistory entry = new SampleStageHistory();
        entry.setSample(this);
        entry.setFromStatus(from);
        entry.setToStatus(to);
        entry.setNote(note);
        history.add(entry);
    }
}
