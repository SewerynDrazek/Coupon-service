package com.example.coupon.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

public record CouponResponse(
        @Schema(example = "SUMMER10") String code,
        @Schema(example = "2025-01-01") LocalDate createdDate,
        @Schema(example = "100") Long volume,
        @Schema(example = "42") Long spent,
        @Schema(example = "PL") String country
) {
}
