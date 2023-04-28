package ru.practicum.shareit.messageManager;

public class ErrorMessage {
    public static final String ITEM_EMPTY_NAME = "Название вещи не может быть пустым.";
    public static final String ITEM_EMPTY_DESCRIPTION = "Описание вещи не может быть пустым.";
    public static final String USER_EMAIL = "Некорректный email.";
    public static final String DATA_ALREADY_EXIST = "Данный %s уже существует.";
    public static final String EMAIL_ALREADY_EXIST = "Данный email %s уже существует.";
    public static final String DATA_NOT_VALID_FOR_UPDATE = "Данные %s для обновления %s заданы неверно.";
    public static final String USER_ID_NOT_VALID = "Пользователь %s не является владельцем для вещи с ID = %s.";
    public static final String OWNER_ID_NOT_FOUND = "Владелец с ID = %s не найден для вещи с ID = %s.";
}