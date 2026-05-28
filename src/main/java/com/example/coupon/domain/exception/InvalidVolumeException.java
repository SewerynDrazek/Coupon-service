package com.example.coupon.domain.exception;

public class InvalidVolumeException extends RuntimeException {

    public InvalidVolumeException() {
        super("Volume must be a positive number");
    }
}
