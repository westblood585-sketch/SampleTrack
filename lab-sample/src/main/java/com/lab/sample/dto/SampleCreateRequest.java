package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

@Schema(description = "Numune kabul isteği")
public record SampleCreateRequest(
        @Schema(description = "Benzersiz numune barkodu", example = "BC-2026-000123")
        @NotBlank @Size(max = 64) String barcode,
        @Schema(description = "Numunenin ait olduğu müşteri kimliği", example = "1")
        @NotNull Long customerId,
        @Schema(description = "Numuneye atanacak test tanımı kimlikleri (en az bir tane)", example = "[1, 2, 3]")
        @NotEmpty Set<@NotNull Long> testDefinitionIds) {
}
