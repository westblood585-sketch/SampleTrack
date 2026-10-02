package com.lab.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Test tanımı")
public record TestDefinitionResponse(
        @Schema(description = "Test tanımı kimliği", example = "1") Long id,
        @Schema(description = "Test kodu", example = "GLU") String code,
        @Schema(description = "Test adı", example = "Glukoz") String name,
        @Schema(description = "Ölçüm birimi", example = "mg/dL") String unit,
        @Schema(description = "Referans alt sınır", example = "70") BigDecimal refMin,
        @Schema(description = "Referans üst sınır", example = "100") BigDecimal refMax,
        @Schema(description = "Yeni numunelere atanabilir mi", example = "true") boolean active) {
}
