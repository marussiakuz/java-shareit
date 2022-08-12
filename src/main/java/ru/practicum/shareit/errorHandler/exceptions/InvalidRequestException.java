package ru.practicum.shareit.errorHandler.exceptions;

public class InvalidRequestException extends RuntimeException {
    private String message;

    public InvalidRequestException(String message) {
        super(message);
    }
}
