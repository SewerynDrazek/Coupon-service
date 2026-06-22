package com.example.coupon.domain.service;

import com.example.coupon.domain.exception.CouponAlreadyUsedException;
import com.example.coupon.domain.exception.CouponCountryMismatchException;
import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.exception.CouponNotFoundException;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.CouponUsageRepository;
import org.assertj.core.api.ThrowableAssert;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

class CouponUsageServiceTest extends CouponTest {

    @Mock
    private CouponRepository couponRepository;

    @Mock
    private CouponUsageRepository couponUsageRepository;

    @InjectMocks
    private CouponUsageService couponUsageService;

    @Test
    void shouldThrowWhenCountryMismatch() {
        //given:
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        //when:
        ThrowableAssert.ThrowingCallable action = () -> couponUsageService.useCoupon(code, userId, "US");

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponCountryMismatchException.class);
    }

    @Test
    void shouldThrowWhenCouponAlreadyUsed() {
        //given:
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        doThrow(new CouponAlreadyUsedException(RAW_CODE, RAW_USER_ID))
                .when(couponUsageRepository).saveUsage(eq(code), eq(userId), any());

        //when:
        ThrowableAssert.ThrowingCallable action = () -> couponUsageService.useCoupon(code, userId, COUNTRY);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponAlreadyUsedException.class);
    }

    @Test
    void shouldThrowWhenCouponExhausted() {
        //given:
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        when(couponRepository.incrementSpentIfAvailable(code)).thenReturn(0);

        //when:
        ThrowableAssert.ThrowingCallable action = () -> couponUsageService.useCoupon(code, userId, COUNTRY);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponExhaustedException.class);
    }

    @Test
    void shouldThrowWhenCouponNotFound() {
        //given:
        when(couponRepository.findByCode(code)).thenReturn(Optional.empty());

        //when:
        ThrowableAssert.ThrowingCallable action = () -> couponUsageService.useCoupon(code, userId, COUNTRY);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponNotFoundException.class);
    }
}