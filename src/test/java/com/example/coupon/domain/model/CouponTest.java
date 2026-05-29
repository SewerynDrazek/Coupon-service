package com.example.coupon.domain.model;

import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.Volume;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    void shouldThrowCouponCountryMismatchException() {
        //given:
        Coupon coupon = buildCoupon(100L, 0L);

        //when:
        assertThatThrownBy(() -> coupon.validateCountry("US"));
    }

    private Coupon buildCoupon(Long volume, Long spent) {
        return Coupon.builder()
                .code(new Code("SPRING20"))
                .createdDate(LocalDate.now())
                .volume(new Volume(volume))
                .spent(spent)
                .country("PL")
                .build();
    }
}
