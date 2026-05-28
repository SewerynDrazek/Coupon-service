package com.example.coupon.domain.port;

import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;

import java.time.LocalDateTime;

public interface CouponUsageRepository {

    boolean existsUsage(Code couponCode, UserId userId);

    void saveUsage(Code couponCode, UserId userId, LocalDateTime usedAt);
}
