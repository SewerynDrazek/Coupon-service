package com.example.coupon.domain.exception;

public class CouponExhaustedException extends RuntimeException {

    public CouponExhaustedException(String code) {
        super("Coupon has reached its maximum usage limit: " + code);
    }
}
