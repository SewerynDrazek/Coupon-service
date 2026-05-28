package com.example.coupon.domain.exception;

public class CountryResolutionException extends RuntimeException {

    public CountryResolutionException(String ip) {
        super("Cannot determine country for IP address: " + ip);
    }
}
