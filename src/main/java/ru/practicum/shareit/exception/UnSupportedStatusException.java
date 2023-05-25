package ru.practicum.shareit.exception;

public class UnSupportedStatusException extends RuntimeException {
    public UnSupportedStatusException(String message) {
        super(message);
    }
}