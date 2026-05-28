package com.example.coupon.api.controller;

import com.example.coupon.api.dto.CouponResponse;
import com.example.coupon.api.dto.CreateCouponRequest;
import com.example.coupon.api.dto.UseCouponRequest;
import com.example.coupon.api.exception.ErrorResponse;
import com.example.coupon.api.mapper.CouponApiMapper;
import com.example.coupon.api.resolver.ClientIp;
import com.example.coupon.domain.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Coupons", description = "Coupon management")
@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final CouponApiMapper mapper;

    @Operation(summary = "Create a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Coupon created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Coupon code already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CreateCouponRequest request) {
        return mapper.toResponse(couponService.createCoupon(request.code(), request.volume(), request.country()));
    }

    @Operation(summary = "Use a coupon")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Coupon used successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Request originates from a country not allowed by the coupon",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Coupon not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Coupon exhausted or already used by this user",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "503", description = "Geo-location service unavailable (circuit breaker open)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{code}/use")
    public CouponResponse use(
            @Parameter(description = "Coupon code", example = "SUMMER10") @PathVariable String code,
            @Valid @RequestBody UseCouponRequest request,
            @ClientIp String clientIp
    ) {
        return mapper.toResponse(couponService.useCoupon(code, request.userId(), clientIp));
    }
}
