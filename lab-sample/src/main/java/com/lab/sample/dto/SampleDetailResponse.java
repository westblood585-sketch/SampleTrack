package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Numune detayı: müşteri, testler, sonuçlar ve aşama geçmişi")
public record SampleDetailResponse(
        @Schema(description = "Numune kimliği", example = "1") Long id,
        @Schema(description = "Barkod", example = "BC-2026-000123") String barcode,
        @Schema(description = "Numunenin müşterisi") CustomerResponse customer,
        @Schema(description = "Numune durumu") SampleStatus status,
        @Schema(description = "Kabul zamanı") LocalDateTime receivedAt,
        @Schema(description = "Red gerekçesi; reddedilmediyse null") String rejectionReason,
        @Schema(description = "Numuneye atanmış testler") List<SampleTestResponse> tests,
        @Schema(description = "Aşama geçmişi (eskiden yeniye)") List<StageHistoryResponse> history) {
}
