package com.example.coupon.domain.model;

import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.Country;
import com.example.coupon.domain.model.vo.Volume;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    @Test
    void shouldIncrementSpentOnUse() {
        Coupon coupon = buildCoupon(5L, 2L);

        Coupon used = coupon.use();

        assertThat(used.getSpent()).isEqualTo(3L);
    }

    @Test
    void shouldNotMutateOriginalOnUse() {
        Coupon coupon = buildCoupon(5L, 0L);

        coupon.use();

        assertThat(coupon.getSpent()).isEqualTo(0L);
    }

    @Test
    void shouldThrowWhenExhausted() {
        Coupon coupon = buildCoupon(3L, 3L);

        assertThatThrownBy(coupon::use)
                .isInstanceOf(CouponExhaustedException.class);
    }

    private Coupon buildCoupon(Long volume, Long spent) {
        return Coupon.builder()
                .code(new Code("SPRING20"))
                .createdDate(LocalDate.now())
                .volume(new Volume(volume))
                .spent(spent)
                .country(Country.PL)
                .build();
    }
}
