package com.example.coupon.domain.port;

import com.example.coupon.domain.model.Country;

public interface GeoLocationPort {

    Country getCountry(String ip);
}
