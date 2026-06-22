package com.example.coupon.domain.service;

import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.exception.CouponNotFoundException;
import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.domain.port.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class CouponUsageService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Transactional
    public void useCoupon(Code code, UserId userId, String clientCountry) {
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> {
                    log.warn("Coupon not found: code={}", code.value());
                    return new CouponNotFoundException(code.value());
                });

        coupon.validateCountry(clientCountry);
        couponUsageRepository.saveUsage(code, userId, LocalDateTime.now());
        incrementSpent(code);
        log.info("Coupon used successfully: code={}, userId={}", code.value(), userId.value());
    }

    private void incrementSpent(Code code) {
        if (couponRepository.incrementSpentIfAvailable(code) == 0) {
            log.warn("Coupon exhausted: code={}", code.value());
            throw new CouponExhaustedException(code.value());
        }
    }
}
