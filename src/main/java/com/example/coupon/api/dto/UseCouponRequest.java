package com.example.coupon.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record UseCouponRequest(
        @Schema(description = "Unique user identifier", example = "user-123")
        @NotBlank(message = "User ID must not be blank") String userId
) {
}
