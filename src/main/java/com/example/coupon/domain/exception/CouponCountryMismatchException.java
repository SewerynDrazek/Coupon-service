package com.example.coupon.domain.exception;

public class CouponCountryMismatchException extends RuntimeException {

    public CouponCountryMismatchException(String code, String clientCountry, String couponCountry) {
        super("Coupon '%s' is restricted to country %s, but request originated from %s"
                .formatted(code, couponCountry, clientCountry));
    }
}
