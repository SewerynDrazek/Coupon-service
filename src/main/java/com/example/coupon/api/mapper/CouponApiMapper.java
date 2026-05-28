package com.example.coupon.api.mapper;

import com.example.coupon.api.dto.CouponResponse;
import com.example.coupon.domain.model.Coupon;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponApiMapper {

    @Mapping(target = "code", source = "code.value")
    @Mapping(target = "volume", source = "volume.value")
    CouponResponse toResponse(Coupon coupon);
}
