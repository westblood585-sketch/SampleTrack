package com.lab.sample.dto;

import lombok.extern.slf4j.Slf4j;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Sonucun referans aralığına göre durumu")
public enum ResultFlag {
    NORMAL,
    LOW,
    HIGH,
    NOT_APPLICABLE
}
