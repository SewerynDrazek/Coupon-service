package com.example.coupon.domain.model.vo;

import com.example.coupon.domain.exception.InvalidUserIdException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserIdTest {

    @Test
    void shouldAcceptValidValue() {
        assertThat(new UserId("user-123").value()).isEqualTo("user-123");
    }

    @Test
    void shouldThrowForNull() {
        assertThatThrownBy(() -> new UserId(null))
                .isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    void shouldThrowForBlank() {
        assertThatThrownBy(() -> new UserId("   "))
                .isInstanceOf(InvalidUserIdException.class);
    }

    @Test
    void shouldThrowForEmpty() {
        assertThatThrownBy(() -> new UserId(""))
                .isInstanceOf(InvalidUserIdException.class);
    }
}
