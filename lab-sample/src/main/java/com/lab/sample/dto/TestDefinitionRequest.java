package com.lab.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Schema(description = "Test tanımı oluşturma / güncelleme isteği")
public record TestDefinitionRequest(
        @Schema(description = "Benzersiz test kodu", example = "GLU") @NotBlank @Size(max = 30) String code,
        @Schema(description = "Test adı", example = "Glukoz") @NotBlank @Size(max = 150) String name,
        @Schema(description = "Ölçüm birimi", example = "mg/dL") @Size(max = 30) String unit,
        @Schema(description = "Referans aralığı alt sınırı", example = "70") BigDecimal refMin,
        @Schema(description = "Referans aralığı üst sınırı", example = "100") BigDecimal refMax) {
}
