package com.example.coupon.api.controller;

import com.example.coupon.api.config.WebMvcConfig;
import com.example.coupon.api.mapper.CouponApiMapperImpl;
import com.example.coupon.domain.exception.CouponAlreadyUsedException;
import com.example.coupon.domain.exception.CouponCountryMismatchException;
import com.example.coupon.domain.exception.CouponExhaustedException;
import com.example.coupon.domain.exception.CouponNotFoundException;
import com.example.coupon.domain.model.Coupon;
import com.example.coupon.domain.model.vo.Code;
import com.example.coupon.domain.model.vo.Volume;
import com.example.coupon.domain.service.CouponService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CouponController.class)
@Import({CouponApiMapperImpl.class, WebMvcConfig.class})
class CouponControllerTest {

    private static final String BASE_URL = "/api/v1/coupons";

    private static final String CODE = "SPRING20";
    private static final String USER_ID = "user-1";

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private CouponService couponService;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @Test
    void shouldCreateCouponAndReturn201() throws Exception {
        //given:
        when(couponService.createCoupon(eq(CODE), eq(100L), eq("PL")))
                .thenReturn(buildCoupon(100L, 0L));

        //when/then:
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", CODE,
                                "volume", 100,
                                "country", "PL"
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(CODE))
                .andExpect(jsonPath("$.volume").value(100))
                .andExpect(jsonPath("$.spent").value(0))
                .andExpect(jsonPath("$.country").value("PL"));
    }

    @Test
    void shouldReturn400WhenCreateRequestMissingCode() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "volume", 100,
                                "country", "PL"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCreateRequestMissingVolume() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", CODE,
                                "country", "PL"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenCountryCodeInvalid() throws Exception {
        mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "code", CODE,
                                "volume", 100,
                                "country", "INVALID"
                        ))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldUseCouponAndReturn204() throws Exception {
        //given:
        doNothing().when(couponService).useCoupon(eq(CODE), eq(USER_ID), any());

        //when/then:
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", USER_ID))))
                .andExpect(status().isNoContent());
    }

    @Test
    void shouldReturn404WhenCouponNotFound() throws Exception {
        //given:
        doThrow(new CouponNotFoundException(CODE))
                .when(couponService).useCoupon(eq(CODE), eq(USER_ID), any());

        //when/then:
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", USER_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void shouldReturn409WhenCouponExhausted() throws Exception {
        //given:
        doThrow(new CouponExhaustedException(CODE))
                .when(couponService).useCoupon(eq(CODE), eq(USER_ID), any());

        //when/then:
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", USER_ID))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn403WhenCountryMismatch() throws Exception {
        //given:
        doThrow(new CouponCountryMismatchException(CODE, "DE", "PL"))
                .when(couponService).useCoupon(eq(CODE), eq(USER_ID), any());

        //when/then:
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", USER_ID))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn409WhenCouponAlreadyUsed() throws Exception {
        //given:
        doThrow(new CouponAlreadyUsedException(CODE, USER_ID))
                .when(couponService).useCoupon(eq(CODE), eq(USER_ID), any());

        //when/then:
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", USER_ID))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenUserIdBlank() throws Exception {
        mockMvc.perform(post(BASE_URL + "/{code}/use", CODE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("userId", ""))))
                .andExpect(status().isBadRequest());
    }

    private Coupon buildCoupon(Long volume, Long spent) {
        return Coupon.builder()
                .code(new Code(CODE))
                .createdDate(LocalDate.now())
                .volume(new Volume(volume))
                .spent(spent)
                .country("PL")
                .build();
    }
}
