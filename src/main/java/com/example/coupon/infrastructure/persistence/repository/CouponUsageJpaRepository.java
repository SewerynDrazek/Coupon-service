package com.example.coupon.infrastructure.persistence.repository;

import com.example.coupon.infrastructure.persistence.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsageEntity, UUID> {

    boolean existsByCouponCodeAndUserId(String couponCode, String userId);
}
