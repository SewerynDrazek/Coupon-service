package com.example.coupon.infrastructure.persistence.mapper;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.Volume;
import com.example.coupon.infrastructure.persistence.entity.CouponEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CouponPersistenceMapper {

    @Mapping(target = "code", source = "code.value")
    @Mapping(target = "volume", source = "volume.value")
    @Mapping(target = "id", ignore = true)
    CouponEntity toEntity(Coupon coupon);

    @Mapping(target = "code", source = "code")
    @Mapping(target = "volume", source = "volume")
    Coupon toDomain(CouponEntity entity);

    default Code toCode(String code) {
        return new Code(code);
    }

    default Volume toVolume(Long value) {
        return new Volume(value);
    }
}
