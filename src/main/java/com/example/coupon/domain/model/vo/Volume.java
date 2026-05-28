package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidVolumeException;

public record Volume(Long value) {

    public Volume {
        if (value == null || value <= 0) {
            throw new InvalidVolumeException();
        }
    }
}
