package com.example.coupon.domain.service;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.model.vo.Volume;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.GeoLocationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageService couponUsageService;
    private final GeoLocationPort geoLocationPort;

    public Coupon createCoupon(String code, Long volume, String country) {
        String normalizedCountry = country.toUpperCase();
        log.info("Creating coupon: code={}, volume={}, country={}", code, volume, normalizedCountry);
        Coupon coupon = Coupon.builder()
                .code(new Code(code))
                .createdDate(LocalDate.now())
                .volume(new Volume(volume))
                .spent(0L)
                .country(normalizedCountry)
                .build();

        Coupon saved = couponRepository.save(coupon);
        log.info("Coupon created: code={}", saved.getCode().value());
        return saved;
    }

    public void useCoupon(String rawCode, String rawUserId, String clientIp) {
        String clientCountry = geoLocationPort.getCountry(clientIp);
        Code code = new Code(rawCode);
        UserId userId = new UserId(rawUserId);
        log.info("Coupon use attempt: code={}, userId={}, clientIp={}", code.value(), userId.value(), clientIp);
        couponUsageService.useCoupon(code, userId, clientCountry);
    }
}
