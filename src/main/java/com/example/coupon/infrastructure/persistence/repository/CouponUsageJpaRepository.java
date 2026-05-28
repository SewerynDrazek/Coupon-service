package com.example.coupon.infrastructure.persistence.repository;

import com.example.coupon.infrastructure.persistence.entity.CouponUsageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CouponUsageJpaRepository extends JpaRepository<CouponUsageEntity, UUID> {

    void deleteByCouponCodeAndUserId(String couponCode, String userId);
}
