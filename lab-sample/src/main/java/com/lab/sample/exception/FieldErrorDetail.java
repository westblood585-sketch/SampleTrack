package com.lab.sample.exception;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tek bir alan icin dogrulama hatasi")
public record FieldErrorDetail(
        @Schema(description = "Hatali alanin adi", example = "email") String field,
        @Schema(description = "Hata aciklamasi", example = "geçerli bir e-posta adresi olmalıdır") String message) {
}
