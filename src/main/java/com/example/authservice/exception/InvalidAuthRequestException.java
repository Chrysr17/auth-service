package com.example.authservice.exception;

public class InvalidAuthRequestException extends RuntimeException {

    public InvalidAuthRequestException(String message) {
        super(message);
    }
}
