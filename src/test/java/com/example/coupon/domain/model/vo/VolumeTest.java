package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidVolumeException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VolumeTest {

    @Test
    void shouldAcceptPositiveValue() {
        assertThat(new Volume(10L).value()).isEqualTo(10L);
    }

    @Test
    void shouldThrowForZero() {
        assertThatThrownBy(() -> new Volume(0L))
                .isInstanceOf(InvalidVolumeException.class);
    }

    @Test
    void shouldThrowForNegativeValue() {
        assertThatThrownBy(() -> new Volume(-1L))
                .isInstanceOf(InvalidVolumeException.class);
    }

    @Test
    void shouldThrowForNull() {
        assertThatThrownBy(() -> new Volume(null))
                .isInstanceOf(InvalidVolumeException.class);
    }
}
