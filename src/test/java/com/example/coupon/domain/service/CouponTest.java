package com.example.coupon.domain.service;

import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.UserId;
import com.example.coupon.domain.model.vo.Volume;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
public abstract class CouponTest {

    protected static final String RAW_CODE = "spring20";
    protected static final String RAW_USER_ID = "user-1";
    protected static final String COUNTRY = "PL";

    protected Coupon coupon;
    protected Code code;
    protected UserId userId;


    @BeforeEach
    void setUp() {
        code = new Code(RAW_CODE);
        userId = new UserId(RAW_USER_ID);
        coupon = Coupon.builder()
                .code(code)
                .createdDate(LocalDate.now())
                .volume(new Volume(10L))
                .spent(0L)
                .country("PL")
                .build();
    }
}
