package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.entity.SampleTestStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(description = "Numuneye atanmış tek bir test ve varsa sonucu")
public record SampleTestResponse(
        @Schema(description = "Numune-test kimliği", example = "5") Long id,
        @Schema(description = "Test tanımı kimliği", example = "1") Long testDefinitionId,
        @Schema(description = "Test kodu", example = "GLU") String code,
        @Schema(description = "Test adı", example = "Glukoz") String name,
        @Schema(description = "Ölçüm birimi", example = "mg/dL") String unit,
        @Schema(description = "Referans alt sınır", example = "70") BigDecimal refMin,
        @Schema(description = "Referans üst sınır", example = "100") BigDecimal refMax,
        @Schema(description = "Test durumu") SampleTestStatus status,
        @Schema(description = "Sonuç; girilmediyse null") TestResultResponse result) {
}
