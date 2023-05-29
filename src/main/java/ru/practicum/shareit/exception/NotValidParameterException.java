package ru.practicum.shareit.exception;

public class NotValidParameterException extends RuntimeException {
    public NotValidParameterException(String message) {
        super(message);
    }
}