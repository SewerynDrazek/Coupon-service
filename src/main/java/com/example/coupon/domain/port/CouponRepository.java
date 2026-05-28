package com.example.coupon.domain.port;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;

import java.util.Optional;

public interface CouponRepository {

    Coupon save(Coupon coupon);

    Optional<Coupon> findByCode(Code code);

    int incrementSpentIfAvailable(Code code);
}
