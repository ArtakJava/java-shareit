package ru.practicum.shareit.exception;

public class NotValidDataForUpdateException extends RuntimeException {
    public NotValidDataForUpdateException(String message) {
        super(message);
    }
}