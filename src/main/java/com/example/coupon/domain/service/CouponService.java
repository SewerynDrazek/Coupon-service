package com.example.coupon.domain.service;

import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.exception.CouponNotFoundException;
import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.model.vo.Volume;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.CouponUsageRepository;
import com.example.coupon.domain.port.GeoLocationPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final GeoLocationPort geoLocationPort;

    @Transactional
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

    @Transactional
    public void useCoupon(String rawCode, String rawUserId, String clientIp) {
        Code code = new Code(rawCode);
        UserId userId = new UserId(rawUserId);
        log.info("Coupon use attempt: code={}, userId={}, clientIp={}", code.value(), userId.value(), clientIp);

        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Coupon not found: code={}", code.value());
                    return new CouponNotFoundException(code.value());
                });

        String clientCountry = geoLocationPort.getCountry(clientIp);

        coupon.validateCountry(clientCountry);
        couponUsageRepository.saveUsage(code, userId, LocalDateTime.now());
        incrementSpent(code, userId);
        log.info("Coupon used successfully: code={}, userId={}", code.value(), userId.value());
    }

    private void incrementSpent(Code code, UserId userId) {
        if (couponRepository.incrementSpentIfAvailable(code) == 0) {
            log.warn("Coupon exhausted: code={}", code.value());
            couponUsageRepository.deleteUsage(code, userId);
            throw new CouponExhaustedException(code.value());
        }
    }
}
