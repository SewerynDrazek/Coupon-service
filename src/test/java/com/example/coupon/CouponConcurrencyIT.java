package com.example.coupon;

import com.example.coupon.domain.port.GeoLocationPort;
import com.example.coupon.domain.service.CouponService;
import com.example.coupon.infrastructure.persistence.repository.CouponJpaRepository;
import com.example.coupon.infrastructure.persistence.repository.CouponUsageJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class CouponConcurrencyIT {

    private static final String CLIENT_IP = "8.8.8.8";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private CouponService couponService;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private CouponUsageJpaRepository couponUsageJpaRepository;
    @MockitoBean
    private GeoLocationPort geoLocationPort;

    @BeforeEach
    void setUp() {
        when(geoLocationPort.getCountry(any())).thenReturn("PL");
    }

    @AfterEach
    void cleanup() {
        couponUsageJpaRepository.deleteAll();
        couponJpaRepository.deleteAll();
    }

    @Test
    void shouldNotExceedVolumeLimitUnderConcurrentRequests() throws InterruptedException {
        //given:
        int volume = 5;
        int threads = 20;
        String code = "RACE-VOLUME";
        couponService.createCoupon(code, (long) volume, "PL");

        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            String userId = "user-" + i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    couponService.useCoupon(code, userId, CLIENT_IP);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        //when:
        startLatch.countDown();

        //then:
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(volume);
    }

    @Test
    void shouldAllowOnlyOneUsagePerUserUnderConcurrentRequests() throws InterruptedException {
        //given:
        int threads = 10;
        String code = "RACE-USER";
        String sharedUserId = "shared-user";
        couponService.createCoupon(code, 100L, "PL");

        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    couponService.useCoupon(code, sharedUserId, CLIENT_IP);
                    successCount.incrementAndGet();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        //when:
        startLatch.countDown();

        //then:
        assertThat(doneLatch.await(5, TimeUnit.SECONDS)).isTrue();
        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(1);
    }
}
