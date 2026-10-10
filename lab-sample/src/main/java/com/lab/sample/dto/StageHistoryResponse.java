package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Numune aşama geçmişi kaydı")
public record StageHistoryResponse(
        @Schema(description = "Kayıt kimliği", example = "1") Long id,
        @Schema(description = "Önceki durum; ilk kayıtta null") SampleStatus fromStatus,
        @Schema(description = "Yeni durum") SampleStatus toStatus,
        @Schema(description = "Değişiklik zamanı") LocalDateTime changedAt,
        @Schema(description = "Açıklama", example = "Numune kabul edildi") String note) {
}
