package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Müşteri oluşturma / güncelleme isteği")
public record CustomerRequest(
        @Schema(description = "Müşteri (klinik, hastane vb.) adı", example = "Acıbadem Kadıköy Hastanesi")
        @NotBlank @Size(max = 150) String name,
        @Schema(description = "İletişim e-postası (benzersiz)", example = "lab@acibadem.example")
        @NotBlank @Email @Size(max = 150) String email,
        @Schema(description = "Telefon numarası", example = "+90 216 000 00 00")
        @Size(max = 30) String phone) {
}
