package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidCouponCodeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CodeTest {

    @Test
    void shouldStoreValueAsUpperCase() {
        assertThat(new Code("spring20").value()).isEqualTo("SPRING20");
    }

    @Test
    void shouldAcceptAlreadyUpperCaseValue() {
        assertThat(new Code("SPRING20").value()).isEqualTo("SPRING20");
    }

    @Test
    void shouldThrowForNullValue() {
        assertThatThrownBy(() -> new Code(null))
                .isInstanceOf(InvalidCouponCodeException.class);
    }

    @Test
    void shouldThrowForEmptyValue() {
        assertThatThrownBy(() -> new Code(""))
                .isInstanceOf(InvalidCouponCodeException.class);
    }
}
