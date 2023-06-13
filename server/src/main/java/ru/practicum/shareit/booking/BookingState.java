package ru.practicum.shareit.booking;

public enum BookingState {
    WAITING("Ожидание"),
    APPROVED("Бронирование одобрено"),
    REJECTED("Бронирование отклонено"),
    CURRENT("Текущий статус бронирования"),
    FUTURE("Статус для будущих бронирований"),
    PAST("Статус для прошлых бронирований"),
    ALL("Все статусы бронирований");

    BookingState(String description) {
    }
}