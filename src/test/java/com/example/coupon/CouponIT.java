package com.example.coupon;

import com.example.coupon.domain.port.GeoLocationPort;
import com.example.coupon.infrastructure.persistence.repository.CouponJpaRepository;
import com.example.coupon.infrastructure.persistence.repository.CouponUsageJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;

import com.example.coupon.domain.exception.CountryResolutionException;
import com.example.coupon.domain.exception.GeoLocationServiceException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class CouponIT {
    
    private static final String BASE_URL = "/api/v1/coupons";

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private TestRestTemplate restTemplate;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private CouponUsageJpaRepository couponUsageJpaRepository;
    @MockitoBean
    private GeoLocationPort geoLocationPort;

    @AfterEach
    void cleanup() {
        couponUsageJpaRepository.deleteAll();
        couponJpaRepository.deleteAll();
    }

    @Test
    void shouldCreateCouponAndReturn201() {
        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL,
                Map.of("code", "SUMMER10", "volume", 100, "country", "PL"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .containsEntry("code", "SUMMER10")
                .containsEntry("volume", 100)
                .containsEntry("spent", 0)
                .containsEntry("country", "PL");
    }

    @Test
    void shouldReturn400WhenCreateRequestMissingCode() {
        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL,
                Map.of("volume", 100, "country", "PL"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400WhenCreateRequestMissingVolume() {
        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL,
                Map.of("code", "SUMMER10", "country", "PL"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldReturn400WhenCountryCodeInvalid() {
        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL,
                Map.of("code", "SUMMER10", "volume", 100, "country", "INVALID"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void shouldAcceptLowercaseCountryAndNormalize() {
        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL,
                Map.of("code", "LOWER10", "volume", 10, "country", "pl"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("country", "PL");
    }

    @Test
    void shouldUseCouponAndReturn204() {
        //given:
        when(geoLocationPort.getCountry(any())).thenReturn("PL");
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "USE10", "volume", 5, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Void> response = restTemplate.postForEntity(
                BASE_URL + "/USE10/use",
                Map.of("userId", "user-1"),
                Void.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    void shouldReturn404WhenCouponNotFound() {
        //given:
        when(geoLocationPort.getCountry(any())).thenReturn("PL");

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/NOTEXIST/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).containsKey("message");
    }

    @Test
    void shouldReturn409WhenCouponExhausted() {
        //given:
        when(geoLocationPort.getCountry(any())).thenReturn("PL");
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "EXHAUST", "volume", 1, "country", "PL"), Map.class);
        restTemplate.postForEntity(BASE_URL + "/EXHAUST/use",
                Map.of("userId", "user-1"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/EXHAUST/use",
                Map.of("userId", "user-2"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn409WhenCouponAlreadyUsed() {
        //given:
        when(geoLocationPort.getCountry(any())).thenReturn("PL");
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "DUPUSE", "volume", 10, "country", "PL"), Map.class);
        restTemplate.postForEntity(BASE_URL + "/DUPUSE/use",
                Map.of("userId", "user-1"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/DUPUSE/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void shouldReturn403WhenCountryMismatch() {
        //given:
        when(geoLocationPort.getCountry(any())).thenReturn("DE");
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "GEOMIS", "volume", 10, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/GEOMIS/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn403WhenCountryCannotBeResolved() {
        //given:
        when(geoLocationPort.getCountry(any())).thenThrow(new CountryResolutionException("127.0.0.1"));
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "PRIVIP", "volume", 10, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/PRIVIP/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void shouldReturn503WhenGeoLocationServiceFails() {
        //given:
        when(geoLocationPort.getCountry(any())).thenThrow(new GeoLocationServiceException("8.8.8.8", new RuntimeException("timeout")));
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "GEOFAIL", "volume", 10, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/GEOFAIL/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturn503WhenCircuitBreakerOpen() {
        //given:
        when(geoLocationPort.getCountry(any()))
                .thenThrow(CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults("test")));
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "CB503", "volume", 10, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/CB503/use",
                Map.of("userId", "user-1"),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldReturn400WhenUserIdBlank() {
        //given:
        restTemplate.postForEntity(BASE_URL,
                Map.of("code", "VALID", "volume", 10, "country", "PL"), Map.class);

        //when:
        ResponseEntity<Map> response = restTemplate.postForEntity(
                BASE_URL + "/VALID/use",
                Map.of("userId", ""),
                Map.class
        );

        //then:
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
