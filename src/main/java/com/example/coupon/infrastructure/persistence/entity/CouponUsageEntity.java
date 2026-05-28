package com.example.coupon.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "coupon_usages",
        uniqueConstraints = @UniqueConstraint(columnNames = {"coupon_code", "user_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class CouponUsageEntity {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "coupon_code", nullable = false, length = 50)
    private String couponCode;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "used_at", nullable = false)
    private LocalDateTime usedAt;
}
