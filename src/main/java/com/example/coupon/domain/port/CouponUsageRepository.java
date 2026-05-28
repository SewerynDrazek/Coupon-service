package com.example.coupon.domain.port;

import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;

import java.time.LocalDateTime;

public interface CouponUsageRepository {

    void saveUsage(Code couponCode, UserId userId, LocalDateTime usedAt);

    void deleteUsage(Code couponCode, UserId userId);
}
