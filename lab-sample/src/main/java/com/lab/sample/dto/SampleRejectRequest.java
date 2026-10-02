package com.lab.sample.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Numune reddetme isteği")
public record SampleRejectRequest(
        @Schema(description = "Red gerekçesi (zorunlu)", example = "Numune hemolizli, yeniden alınmalı")
        @NotBlank @Size(max = 500) String reason) {
}
