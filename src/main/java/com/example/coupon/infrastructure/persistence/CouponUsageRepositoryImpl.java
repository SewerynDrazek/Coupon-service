package com.example.coupon.infrastructure.persistence;

import com.example.coupon.domain.exception.CouponAlreadyUsedException;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.port.CouponUsageRepository;
import com.example.coupon.infrastructure.persistence.entity.CouponUsageEntity;
import com.example.coupon.infrastructure.persistence.repository.CouponUsageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
@RequiredArgsConstructor
public class CouponUsageRepositoryImpl implements CouponUsageRepository {

    private final CouponUsageJpaRepository couponUsageJpaRepository;

    @Override
    public void saveUsage(Code couponCode, UserId userId, LocalDateTime usedAt) {
        try {
            CouponUsageEntity entity = new CouponUsageEntity();
            entity.setCouponCode(couponCode.value());
            entity.setUserId(userId.value());
            entity.setUsedAt(usedAt);
            couponUsageJpaRepository.saveAndFlush(entity);
        } catch (DataIntegrityViolationException e) {
            throw new CouponAlreadyUsedException(couponCode.value(), userId.value());
        }
    }

    @Override
    public void deleteUsage(Code couponCode, UserId userId) {
        couponUsageJpaRepository.deleteByCouponCodeAndUserId(couponCode.value(), userId.value());
    }
}
