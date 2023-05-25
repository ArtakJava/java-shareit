package ru.practicum.shareit.messageManager;

public class ErrorMessage {
    public static final String ITEM_EMPTY_NAME = "Название вещи не может быть пустым.";
    public static final String COMMENT_EMPTY_TEXT = "Текст комментария не может быть пустым.";
    public static final String USER_EMPTY_NAME = "Имя пользователя не может быть пустым.";
    public static final String ITEM_EMPTY_DESCRIPTION = "Описание вещи не может быть пустым.";
    public static final String USER_EMAIL = "Некорректный email.";
    public static final String DATA_ALREADY_EXIST = "Данный %s уже существует.";
    public static final String USER_ID_NOT_VALID = "Пользователь %s не является владельцем для вещи с ID = %s.";
    public static final String USER_ID_NOT_FOUND = "Пользователь %s не найден.";
    public static final String OWNER_ITEM = "Владелец %s не может у себя забронировать.";
    public static final String OWNER_ID_NOT_FOUND_FOR_ITEM = "Владелец с ID = %s не найден для вещи с ID = %s.";
    public static final String AVAILABLE_NOT_AVAILABLE = "Вещь с ID = %s недоступна для аренды.";
    public static final String START_IN_PAST = "Дата начала бронирования указана в прошлом.";
    public static final String END_IN_PAST = "Дата окончания бронирования указана в прошлом.";
    public static final String END_BEFORE_START = "Дата окончания бронирования указана раньше времени даты начала.";
    public static final String START_EQUAL_END = "Указаны одинаковые даты начала и окончания бронирования.";
    public static final String START_IS_NUll = "Дата начала бронирования не указана.";
    public static final String END_IS_NUll = "Дата окончания бронирования не указана.";
    public static final String UNSUPPORTED_STATUS = "Unknown state: %s";
    public static final String BOOKER_OR_OWNER_ID_NOT_VALID = "Пользователь %s не является владельцем или арендатором для вещи с ID = %s.";
    public static final String BOOKING_ALREADY_APPROVED = "Бронирование с ID = %s уже одобрено.";
    public static final String AUTHOR_NOT_BOOKING = "Автор c ID = %s не закончил бронирование вещи с ID = %s для оставления комментария.";
}