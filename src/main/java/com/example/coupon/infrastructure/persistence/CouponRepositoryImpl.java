package com.example.coupon.infrastructure.persistence;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.port.CouponRepository;
import com.example.coupon.infrastructure.persistence.mapper.CouponPersistenceMapper;
import com.example.coupon.infrastructure.persistence.repository.CouponJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CouponRepositoryImpl implements CouponRepository {

    private final CouponJpaRepository couponJpaRepository;
    private final CouponPersistenceMapper mapper;

    @Override
    public Coupon save(Coupon coupon) {
        return mapper.toDomain(couponJpaRepository.save(mapper.toEntity(coupon)));
    }

    @Override
    public Optional<Coupon> findByCode(Code code) {
        return couponJpaRepository.findByCode(code.value()).map(mapper::toDomain);
    }

    @Override
    public int incrementSpentIfAvailable(Code code) {
        return couponJpaRepository.incrementSpentIfAvailable(code.value());
    }
}
