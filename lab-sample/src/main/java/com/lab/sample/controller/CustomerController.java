package com.lab.sample.controller;

import lombok.extern.slf4j.Slf4j;

import com.lab.sample.dto.CustomerRequest;
import com.lab.sample.dto.CustomerResponse;
import com.lab.sample.exception.ErrorResponse;
import com.lab.sample.service.CustomerService;
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

@Tag(name = "Müşteriler", description = "Numune gönderen müşteri (klinik/hastane) kayıtları")
@Slf4j
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Yeni müşteri oluştur")
    @ApiResponse(responseCode = "201", description = "Müşteri oluşturuldu")
    @ApiResponse(responseCode = "409", description = "E-posta zaten kayıtlı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.create(request));
    }

    @Operation(summary = "Müşteriyi güncelle")
    @ApiResponse(responseCode = "200", description = "Müşteri güncellendi")
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> update(@PathVariable Long id,
                                                   @Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @Operation(summary = "Müşteri detayını getir")
    @ApiResponse(responseCode = "200", description = "Müşteri bulundu")
    @ApiResponse(responseCode = "404", description = "Müşteri bulunamadı",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.get(id));
    }

    @Operation(summary = "Müşterileri sayfalı listele")
    @ApiResponse(responseCode = "200", description = "Müşteri sayfası")
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> list(
            @Parameter(description = "Sayfalama ve sıralama") @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return ResponseEntity.ok(customerService.list(pageable));
    }
}
