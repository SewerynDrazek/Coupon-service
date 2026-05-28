package com.example.coupon.infrastructure.geolocation;

import com.example.coupon.domain.port.GeoLocationPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("local")
@Component
public class IpApiGeoLocationAdapterMock implements GeoLocationPort {

    @Override
    public String getCountry(String ip) {
        return "PL";
    }
}
