package com.lab.sample.controller;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.dto.SampleCreateRequest;
import com.lab.sample.dto.SampleDetailResponse;
import com.lab.sample.dto.SampleRejectRequest;
import com.lab.sample.dto.SampleSummaryResponse;
import com.lab.sample.dto.SampleTestResponse;
import com.lab.sample.dto.TestResultRequest;
import com.lab.sample.entity.SampleStatus;
import com.lab.sample.exception.ErrorResponse;
import com.lab.sample.service.SampleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Numuneler", description = "Numune kabul, aşama geçişleri (başlat/reddet/tamamla) ve sonuç girişi")
@Slf4j
@RestController
@RequestMapping("/api/samples")
@RequiredArgsConstructor
public class SampleController {

    private final SampleService sampleService;

    @Operation(summary = "Numune kabul et",
            description = "Barkod benzersiz olmalı; en az bir aktif test tanımı ile numune oluşturulur, durum RECEIVED olur.")
    @ApiResponse(responseCode = "201", description = "Numune oluşturuldu")
    @ApiResponse(responseCode = "404", description = "Müşteri veya test tanımı bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Barkod zaten kayıtlı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Pasif test tanımı atanmaya çalışıldı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<SampleDetailResponse> create(@Valid @RequestBody SampleCreateRequest request) {
        log.info("Received sample creation/intake request");
        return ResponseEntity.status(HttpStatus.CREATED).body(sampleService.create(request));
    }

    @Operation(summary = "Numune detayını getir",
            description = "Müşteri, atanmış testler, sonuçlar ve aşama geçmişini içerir.")
    @ApiResponse(responseCode = "200", description = "Numune bulundu")
    @ApiResponse(responseCode = "404", description = "Numune bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<SampleDetailResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(sampleService.getDetail(id));
    }

    @Operation(summary = "Numuneleri filtreli ve sayfalı listele")
    @ApiResponse(responseCode = "200", description = "Numune sayfası")
    @GetMapping
    public ResponseEntity<Page<SampleSummaryResponse>> search(
            @Parameter(description = "Numune durumuna göre filtre") @RequestParam(required = false) SampleStatus status,
            @Parameter(description = "Müşteri kimliğine göre filtre") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Barkoda göre kısmi eşleşme") @RequestParam(required = false) String barcode,
            @Parameter(description = "Sayfalama ve sıralama") @ParameterObject
            @PageableDefault(size = 20, sort = "receivedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(sampleService.search(status, customerId, barcode, pageable));
    }

    @Operation(summary = "Numuneyi işleme al", description = "RECEIVED → IN_PROGRESS geçişi.")
    @ApiResponse(responseCode = "200", description = "Numune işleme alındı")
    @ApiResponse(responseCode = "404", description = "Numune bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Geçersiz durum geçişi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{id}/start")
    public ResponseEntity<SampleDetailResponse> start(@PathVariable Long id) {
        return ResponseEntity.ok(sampleService.start(id));
    }

    @Operation(summary = "Numuneyi reddet", description = "Red gerekçesi zorunludur; reddedilen numuneye sonuç girilemez.")
    @ApiResponse(responseCode = "200", description = "Numune reddedildi")
    @ApiResponse(responseCode = "404", description = "Numune bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Geçersiz durum geçişi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{id}/reject")
    public ResponseEntity<SampleDetailResponse> reject(@PathVariable Long id,
                                                        @Valid @RequestBody SampleRejectRequest request) {
        return ResponseEntity.ok(sampleService.reject(id, request));
    }

    @Operation(summary = "Numuneyi tamamla",
            description = "Tüm testler tamamlanmadan numune COMPLETED olamaz (iş kuralı).")
    @ApiResponse(responseCode = "200", description = "Numune tamamlandı")
    @ApiResponse(responseCode = "404", description = "Numune bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Tamamlanmamış test var veya geçersiz durum geçişi",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{id}/complete")
    public ResponseEntity<SampleDetailResponse> complete(@PathVariable Long id) {
        return ResponseEntity.ok(sampleService.complete(id));
    }

    @Operation(summary = "Numune testine sonuç gir",
            description = "Reddedilmiş veya tamamlanmış numuneye sonuç girilemez; bir teste yalnızca bir kez sonuç girilebilir.")
    @ApiResponse(responseCode = "200", description = "Sonuç kaydedildi")
    @ApiResponse(responseCode = "404", description = "Numune veya numune testi bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "409", description = "Bu teste zaten sonuç girilmiş",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @ApiResponse(responseCode = "422", description = "Reddedilmiş numuneye veya tamamlanmış numuneye sonuç girilemez",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping("/{sampleId}/tests/{sampleTestId}/result")
    public ResponseEntity<SampleTestResponse> enterResult(@PathVariable Long sampleId,
                                                          @PathVariable Long sampleTestId,
                                                          @Valid @RequestBody TestResultRequest request) {
        return ResponseEntity.ok(sampleService.enterResult(sampleId, sampleTestId, request));
    }
}
