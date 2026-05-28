package com.example.coupon.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record CreateCouponRequest(
        @Schema(description = "Unique coupon code (case-insensitive)", example = "SUMMER10")
        @NotBlank(message = "Code must not be blank") String code,

        @Schema(description = "Maximum number of uses", example = "100")
        @NotNull(message = "Volume is required") @Min(value = 1, message = "Volume must be at least 1") Long volume,

        @Schema(description = "ISO 3166-1 alpha-2 country code", example = "PL")
        @NotBlank(message = "Country is required")
        @Pattern(regexp = "[A-Za-z]{2}", message = "Country must be a valid ISO 3166-1 alpha-2 code")
        String country
) {
}
