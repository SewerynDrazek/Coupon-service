package com.example.coupon.infrastructure.persistence.repository;

import com.example.coupon.infrastructure.persistence.entity.CouponEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface CouponJpaRepository extends JpaRepository<CouponEntity, UUID> {

    Optional<CouponEntity> findByCode(String code);

    @Modifying
    @Query("UPDATE CouponEntity c SET c.spent = c.spent + 1 WHERE c.code = :code AND c.spent < c.volume")
    int incrementSpentIfAvailable(@Param("code") String code);
}
