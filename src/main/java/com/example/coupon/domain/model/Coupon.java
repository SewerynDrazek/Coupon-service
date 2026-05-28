package com.example.coupon.domain.model;

import com.example.coupon.domain.exception.CouponCountryMismatchException;
import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.Country;
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
    private final Country country;

    public void validateCountry(Country clientCountry) {
        if (this.country != clientCountry) {
            throw new CouponCountryMismatchException(code.value(), clientCountry, this.country);
        }
    }

    public Coupon use() {
        if (spent >= volume.value()) {
            throw new CouponExhaustedException(code.value());
        }
        return Coupon.builder()
                .code(code)
                .createdDate(createdDate)
                .volume(volume)
                .spent(spent + 1)
                .country(country)
                .build();
    }
}
