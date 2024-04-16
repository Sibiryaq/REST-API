package ru.aston.exception;

public class HttpException extends RuntimeException {
    public HttpException(String message) {
        super(message);
    }
}
