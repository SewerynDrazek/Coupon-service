package com.example.coupon.domain.exception;

import com.example.coupon.domain.model.Country;

public class CouponCountryMismatchException extends RuntimeException {

    public CouponCountryMismatchException(String code, Country clientCountry, Country couponCountry) {
        super("Coupon '%s' is restricted to country %s, but request originated from %s"
                .formatted(code, couponCountry, clientCountry));
    }
}
