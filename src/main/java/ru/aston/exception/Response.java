package ru.aston.exception;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class Response {
    private final String message;

    public Response(String message) {
        this.message = message;
    }
}
