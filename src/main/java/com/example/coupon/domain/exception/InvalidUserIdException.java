package com.example.coupon.domain.exception;

public class InvalidUserIdException extends RuntimeException {

    public InvalidUserIdException() {
        super("User ID must not be null or blank");
    }
}
