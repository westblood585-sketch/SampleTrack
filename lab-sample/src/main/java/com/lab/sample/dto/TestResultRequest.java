package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Test sonucu giriş isteği")
public record TestResultRequest(
        @Schema(description = "Ölçülen değer", example = "92.5")
        @NotNull @Digits(integer = 8, fraction = 4) BigDecimal value,
        @Schema(description = "Sonucu giren kişi", example = "Teknisyen Ayşe")
        @NotBlank @Size(max = 100) String enteredBy) {
}
