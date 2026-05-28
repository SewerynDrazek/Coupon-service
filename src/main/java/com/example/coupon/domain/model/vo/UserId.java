package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidUserIdException;

public record UserId(String value) {

    public UserId {
        if (value == null || value.isBlank()) {
            throw new InvalidUserIdException();
        }
    }
}
