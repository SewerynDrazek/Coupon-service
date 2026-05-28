package com.example.coupon.domain.exception;

public class InvalidCouponCodeException extends RuntimeException {

    public InvalidCouponCodeException() {
        super("Coupon code must not be null or empty");
    }
}
