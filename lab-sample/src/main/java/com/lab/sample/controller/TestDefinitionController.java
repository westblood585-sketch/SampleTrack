package com.lab.sample.controller;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.dto.TestDefinitionRequest;
import com.lab.sample.dto.TestDefinitionResponse;
import com.lab.sample.exception.ErrorResponse;
import com.lab.sample.service.TestDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Test Tanımları", description = "Laboratuvar test kataloğu (kod, ad, birim, referans aralığı)")
@Slf4j
@RestController
@RequestMapping("/api/test-definitions")
@RequiredArgsConstructor
public class TestDefinitionController {

    private final TestDefinitionService testDefinitionService;

    @Operation(summary = "Yeni test tanımı oluştur")
    @ApiResponse(responseCode = "201", description = "Test tanımı oluşturuldu")
    @ApiResponse(responseCode = "400", description = "Referans aralığı geçersiz",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Test kodu zaten kayıtlı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<TestDefinitionResponse> create(@Valid @RequestBody TestDefinitionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(testDefinitionService.create(request));
    }

    @Operation(summary = "Test tanımını güncelle",
            description = "Sonucu girilmiş bir teste bağlı test tanımı güncellenemez (iş kuralı).")
    @ApiResponse(responseCode = "200", description = "Test tanımı güncellendi")
    @ApiResponse(responseCode = "404", description = "Test tanımı bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Sonucu girilmiş test tanımı değiştirilemez",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<TestDefinitionResponse> update(@PathVariable Long id,
                                                          @Valid @RequestBody TestDefinitionRequest request) {
        return ResponseEntity.ok(testDefinitionService.update(id, request));
    }

    @Operation(summary = "Test tanımını aktif/pasif yap",
            description = "Pasif test tanımları yeni numunelere atanamaz; mevcut kayıtları etkilemez.")
    @ApiResponse(responseCode = "200", description = "Durum güncellendi")
    @PatchMapping("/{id}/active")
    public ResponseEntity<TestDefinitionResponse> setActive(@PathVariable Long id,
                                                            @RequestParam boolean active) {
        return ResponseEntity.ok(testDefinitionService.setActive(id, active));
    }

    @Operation(summary = "Test tanımı detayını getir")
    @ApiResponse(responseCode = "200", description = "Test tanımı bulundu")
    @ApiResponse(responseCode = "404", description = "Test tanımı bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<TestDefinitionResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(testDefinitionService.get(id));
    }

    @Operation(summary = "Test tanımlarını listele")
    @ApiResponse(responseCode = "200", description = "Test tanımı listesi")
    @GetMapping
    public ResponseEntity<List<TestDefinitionResponse>> list(
            @Parameter(description = "true ise sadece aktif tanımlar döner")
            @RequestParam(defaultValue = "false") boolean onlyActive) {
        return ResponseEntity.ok(testDefinitionService.list(onlyActive));
    }
}
