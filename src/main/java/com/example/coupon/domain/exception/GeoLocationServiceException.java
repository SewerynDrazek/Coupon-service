package com.example.coupon.domain.exception;

public class GeoLocationServiceException extends RuntimeException {

    public GeoLocationServiceException(String ip, Throwable cause) {
        super("Geo-location service unavailable for IP: " + ip, cause);
    }
}
