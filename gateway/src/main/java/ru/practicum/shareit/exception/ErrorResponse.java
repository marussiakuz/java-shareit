package ru.practicum.shareit.exception;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    @JsonProperty("Error message")
    public String getMessage() {
        return message;
    }
}
