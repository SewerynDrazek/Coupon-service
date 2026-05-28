package com.example.coupon.infrastructure.geolocation;

import com.example.coupon.domain.exception.CountryResolutionException;
import com.example.coupon.domain.exception.GeoLocationServiceException;
import com.example.coupon.domain.port.GeoLocationPort;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Slf4j
@Component
@RequiredArgsConstructor
public class IpApiGeoLocationAdapter implements GeoLocationPort {

    private final RestClient geoLocationRestClient;
    private final CircuitBreaker geoLocationCircuitBreaker;

    @Override
    public String getCountry(String ip) {
        if (isLocalOrPrivateIp(ip)) {
            log.warn("Blocking request from private/local IP: {}", ip);
            throw new CountryResolutionException(ip);
        }
        try {
            return geoLocationCircuitBreaker.executeSupplier(() -> callApi(ip));
        } catch (CallNotPermittedException e) {
            log.warn("Circuit breaker OPEN, rejecting request for IP {}", ip);
            throw e;
        } catch (RestClientException e) {
            log.warn("Geo-location API call failed for IP {}: {}", ip, e.getMessage());
            throw new GeoLocationServiceException(ip, e);
        }
    }

    private String callApi(String ip) {
        IpApiResponse response = geoLocationRestClient.get()
                .uri("/json/{ip}?fields=status,countryCode", ip)
                .retrieve()
                .body(IpApiResponse.class);

        if (response != null && "success".equals(response.status()) && response.countryCode() != null) {
            String code = response.countryCode().toUpperCase();
            if (code.length() != 2) {
                log.warn("Geo-location API returned invalid country code for IP {}: {}", ip, response.countryCode());
                throw new GeoLocationServiceException(ip, null);
            }
            return code;
        }
        log.warn("Geo-location lookup returned non-success for IP {}: {}", ip, response);
        throw new GeoLocationServiceException(ip, null);
    }

    private boolean isLocalOrPrivateIp(String ip) {
        if (ip == null || ip.isBlank()) return true;
        return ip.equals("127.0.0.1")
                || ip.equals("::1")
                || ip.equals("0:0:0:0:0:0:0:1")
                || ip.startsWith("10.")
                || ip.startsWith("192.168.")
                || ip.startsWith("172.16.") || ip.startsWith("172.17.") || ip.startsWith("172.18.")
                || ip.startsWith("172.19.") || ip.startsWith("172.2") || ip.startsWith("172.30.")
                || ip.startsWith("172.31.");
    }
}
