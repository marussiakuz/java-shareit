package ru.practicum.shareit.errorHandler;

public class ErrorResponse {
    private final String message;

    public ErrorResponse(String message) {
        this.message = message;
    }

    public String getError() {
        return message;
    }
}
