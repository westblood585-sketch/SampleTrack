package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(description = "Test sonucu")
public record TestResultResponse(
        @Schema(description = "Sonuç kimliği", example = "10") Long id,
        @Schema(description = "Ölçülen değer", example = "92.5") BigDecimal value,
        @Schema(description = "Giriş zamanı") LocalDateTime enteredAt,
        @Schema(description = "Sonucu giren kişi", example = "Teknisyen Ayşe") String enteredBy,
        @Schema(description = "Referans aralığına göre durum") ResultFlag flag) {
}
