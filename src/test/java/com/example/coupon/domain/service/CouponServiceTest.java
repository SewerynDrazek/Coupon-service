package com.example.coupon.domain.service;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.GeoLocationPort;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CouponServiceTest extends CouponTest {

    private static final String CLIENT_IP = "8.8.8.8";

    @Mock
    private  CouponRepository couponRepository;
    @Mock
    private  CouponUsageService couponUsageService;
    @Mock
    private  GeoLocationPort geoLocationPort;
    @InjectMocks
    private CouponService couponService;

    @Test
    void shouldCreateCoupon() {
        //given:
        when(couponRepository.save(any())).thenReturn(coupon);

        //when:
        Coupon result = couponService.createCoupon(RAW_CODE, 10L, COUNTRY);

        //then:
        assertThat(result).isEqualTo(coupon);
        verify(couponRepository).save(any(Coupon.class));
    }

    @Test
    void shouldUseCouponSuccessfully() {
        //given:
        when(geoLocationPort.getCountry(CLIENT_IP)).thenReturn(COUNTRY);

        //when:
        couponService.useCoupon(RAW_CODE, RAW_USER_ID, CLIENT_IP);

        //then:
        verify(couponUsageService).useCoupon(eq(code), eq(userId), any());
    }
}
