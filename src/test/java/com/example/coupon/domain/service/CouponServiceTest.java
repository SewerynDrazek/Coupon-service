package com.example.coupon.domain.service;

import com.example.coupon.domain.exception.CouponAlreadyUsedException;
import com.example.coupon.domain.exception.CouponCountryMismatchException;
import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.exception.CouponNotFoundException;
import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.model.vo.Volume;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.CouponUsageRepository;
import com.example.coupon.domain.port.GeoLocationPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CouponServiceTest {

    private static final String RAW_CODE = "spring20";
    private static final String RAW_USER_ID = "user-1";
    private static final String CLIENT_IP = "8.8.8.8";

    @Mock CouponRepository couponRepository;
    @Mock CouponUsageRepository couponUsageRepository;
    @Mock GeoLocationPort geoLocationPort;
    @InjectMocks CouponService couponService;

    private Coupon coupon;
    private Code code;
    private UserId userId;

    @BeforeEach
    void setUp() {
        code = new Code(RAW_CODE);
        userId = new UserId(RAW_USER_ID);
        coupon = Coupon.builder()
                .code(code)
                .createdDate(LocalDate.now())
                .volume(new Volume(10L))
                .spent(0L)
                .country("PL")
                .build();
    }

    @Test
    void shouldCreateCoupon() {
        //given:
        when(couponRepository.save(any())).thenReturn(coupon);

        //when:
        Coupon result = couponService.createCoupon(RAW_CODE, 10L, "PL");

        //then:
        assertThat(result).isEqualTo(coupon);
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void shouldUseCouponSuccessfully() {
        //given:
        when(geoLocationPort.getCountry(CLIENT_IP)).thenReturn("PL");
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        when(couponRepository.incrementSpentIfAvailable(code)).thenReturn(1);

        //when:
        couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        verify(couponUsageRepository).saveUsage(eq(code), eq(userId), any());
    }

    @Test
    void shouldThrowWhenCouponNotFound() {
        //given:
        when(couponRepository.findByCode(code)).thenReturn(Optional.empty());

        //when:
        ThrowingCallable action = () -> couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCountryMismatch() {
        //given:
        when(geoLocationPort.getCountry(CLIENT_IP)).thenReturn("DE");
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));

        //when:
        ThrowingCallable action = () -> couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponCountryMismatchException.class);
    }

    @Test
    void shouldThrowWhenCouponAlreadyUsed() {
        //given:
        when(geoLocationPort.getCountry(CLIENT_IP)).thenReturn("PL");
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        doThrow(new CouponAlreadyUsedException(RAW_CODE, RAW_USER_ID))
                .when(couponUsageRepository).saveUsage(eq(code), eq(userId), any());

        //when:
        ThrowingCallable action = () -> couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponAlreadyUsedException.class);
    }

    @Test
    void shouldThrowWhenCouponExhausted() {
        //given:
        when(geoLocationPort.getCountry(CLIENT_IP)).thenReturn("PL");
        when(couponRepository.findByCode(code)).thenReturn(Optional.of(coupon));
        when(couponRepository.incrementSpentIfAvailable(code)).thenReturn(0);

        //when:
        ThrowingCallable action = () -> couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        assertThatThrownBy(action).isInstanceOf(CouponExhaustedException.class);
    }
}
