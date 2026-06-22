package com.example.coupon.infrastructure.geolocation;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "geolocation")
public record GeoLocationProperties (String url, int readTimeout, int connectTimeout)
{ }
