package ru.practicum.shareit.exception;

public class NotValidOwnerForUpdateException extends RuntimeException {
    public NotValidOwnerForUpdateException(String message) {
        super(message);
    }
}