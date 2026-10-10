package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Numune liste satırı")
public record SampleSummaryResponse(
        @Schema(description = "Numune kimliği", example = "1") Long id,
        @Schema(description = "Barkod", example = "BC-2026-000123") String barcode,
        @Schema(description = "Müşteri kimliği", example = "1") Long customerId,
        @Schema(description = "Müşteri adı", example = "Acıbadem Kadıköy Hastanesi") String customerName,
        @Schema(description = "Numune durumu") SampleStatus status,
        @Schema(description = "Kabul zamanı") LocalDateTime receivedAt) {
}
