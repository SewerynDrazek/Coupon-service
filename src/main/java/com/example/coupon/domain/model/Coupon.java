package com.example.coupon.domain.model;

import com.example.coupon.domain.exception.CouponCountryMismatchException;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.Volume;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Builder
@Getter
public class Coupon {

    private final Code code;
    private final LocalDate createdDate;
    private final Volume volume;
    private final Long spent;
    private final String country;

    public void validateCountry(String clientCountry) {
        if (!this.country.equals(clientCountry)) {
            throw new CouponCountryMismatchException(code.value(), clientCountry, this.country);
        }
    }
}
