package com.example.coupon.api.exception;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

public record ErrorResponse(
        @Schema(example = "404") int status,
        @Schema(example = "Coupon not found: SUMMER10") String message,
        @Schema(example = "2025-01-01T12:00:00") LocalDateTime timestamp
) {

    public ErrorResponse(int status, String message) {
        this(status, message, LocalDateTime.now());
    }
}
