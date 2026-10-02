package com.lab.sample.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;

/** Tum hatalarda donen standart yanit govdesi. */
@Schema(description = "Standart hata yaniti")
public record ErrorResponse(
        @Schema(description = "Hatanin olustugu an (UTC)") Instant timestamp,
        @Schema(description = "HTTP durum kodu", example = "409") int status,
        @Schema(description = "HTTP durum metni", example = "Conflict") String error,
        @Schema(description = "Makine-okunur hata kodu", example = "DUPLICATE_BARCODE") String code,
        @Schema(description = "Kullaniciya gosterilebilir aciklama", example = "Bu barkod zaten kayıtlı: BC-001") String message,
        @Schema(description = "Istegin yolu", example = "/api/samples") String path,
        @Schema(description = "Dogrulama hatalarinda alan bazli detaylar; yoksa bos liste") List<FieldErrorDetail> fieldErrors) {

    public static ErrorResponse of(HttpStatus status, String code, String message, String path,
                                   List<FieldErrorDetail> fieldErrors) {
        return new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(),
                code, message, path, fieldErrors);
    }
}
