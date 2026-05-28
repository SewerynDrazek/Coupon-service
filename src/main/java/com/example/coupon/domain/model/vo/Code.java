package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidCouponCodeException;

public record Code(String value) {

    public Code (String value) {
        if (value == null || value.isEmpty()) {
            throw new InvalidCouponCodeException();
        }

        this.value = value.toUpperCase();
    }
}
