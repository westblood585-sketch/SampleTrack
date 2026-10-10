package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Müşteri bilgisi")
public record CustomerResponse(
        @Schema(description = "Müşteri kimliği", example = "1") Long id,
        @Schema(description = "Müşteri adı", example = "Acıbadem Kadıköy Hastanesi") String name,
        @Schema(description = "E-posta", example = "lab@acibadem.example") String email,
        @Schema(description = "Telefon", example = "+90 216 000 00 00") String phone,
        @Schema(description = "Kayıt zamanı") LocalDateTime createdAt) {
}
