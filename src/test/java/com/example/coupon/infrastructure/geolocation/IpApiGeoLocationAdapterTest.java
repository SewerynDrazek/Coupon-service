package com.example.coupon.infrastructure.geolocation;

import com.example.coupon.domain.exception.CountryResolutionException;
import com.example.coupon.domain.exception.GeoLocationServiceException;
import com.example.coupon.domain.model.Country;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IpApiGeoLocationAdapterTest {

    @RegisterExtension
    static WireMockExtension wm = WireMockExtension.newInstance()
            .options(wireMockConfig().usingFilesUnderClasspath("wiremock"))
            .configureStaticDsl(true)
            .build();

    private IpApiGeoLocationAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = buildAdapter(CircuitBreaker.ofDefaults("geoLocation-" + System.nanoTime()));
    }

    private IpApiGeoLocationAdapter buildAdapter(CircuitBreaker circuitBreaker) {
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost:" + wm.getPort())
                .build();
        return new IpApiGeoLocationAdapter(restClient, circuitBreaker);
    }

    @Test
    void shouldReturnCountryCodeOnSuccess() {
        //when:
        Country result = adapter.getCountry("8.8.8.8");

        //then:
        assertThat(result).isEqualTo(Country.US);
    }

    @Test
    void shouldThrowGeoLocationServiceExceptionWhenApiReturnsFailStatus() {
        //when:
        ThrowingCallable action = () -> adapter.getCountry("9.9.9.9");

        //then:
        assertThatThrownBy(action).isInstanceOf(GeoLocationServiceException.class);
    }

    @Test
    void shouldThrowGeoLocationServiceExceptionOnServerError() {
        assertThatThrownBy(() -> adapter.getCountry("1.2.3.4"))
                .isInstanceOf(GeoLocationServiceException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"127.0.0.1", "::1", "192.168.0.1", "10.0.0.1"})
    void shouldThrowCountryResolutionExceptionForPrivateIps(String ip) {
        assertThatThrownBy(() -> adapter.getCountry(ip)).isInstanceOf(CountryResolutionException.class);
    }

    @Test
    void shouldThrowCallNotPermittedAndMakeNoHttpCallWhenCircuitBreakerOpen() {
        //given:
        int windowSize = 2;
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(windowSize)
                .minimumNumberOfCalls(windowSize)
                .failureRateThreshold(100)
                .waitDurationInOpenState(Duration.ofMinutes(10))
                .build();
        IpApiGeoLocationAdapter localAdapter = buildAdapter(CircuitBreaker.of("geoLocation-open-test", config));

        for (int i = 0; i < windowSize; i++) {
            try {
                localAdapter.getCountry("5.5.5.5");
            } catch (GeoLocationServiceException e) {
                System.out.println("Caught GeoLocationServiceException");
            }
        }

        //when:
        ThrowingCallable action = () -> localAdapter.getCountry("5.5.5.5");

        //then:
        assertThatThrownBy(action).isInstanceOf(CallNotPermittedException.class);
    }
}
