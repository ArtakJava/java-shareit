package ru.practicum.shareit.exception;

public class NotValidOwnerException extends RuntimeException {
    public NotValidOwnerException(String message) {
        super(message);
    }
}