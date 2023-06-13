package ru.practicum.shareit.messageManager;

public class MessageHolder {
    public static final String ITEM_EMPTY_NAME = "Название вещи не может быть пустым.";
    public static final String COMMENT_EMPTY_TEXT = "Текст комментария не может быть пустым.";
    public static final String ITEM_EMPTY_DESCRIPTION = "Описание вещи не может быть пустым.";
    public static final String USER_EMAIL = "Некорректный email.";
    public static final String UNSUPPORTED_STATUS = "Unknown state: %s";
    public static final String GET_CREATE_REQUEST = "Получен запрос на добавление: {}.";
    public static final String GET_UPDATE_REQUEST = "Получен запрос на обновление: {}.";
    public static final String GET_ALL_REQUEST = "Получен запрос на получение всех данных.";
    public static final String GET_ALL_BY_USER_REQUEST = "Получен запрос на получение всех вещей для пользователя с ID = {}.";
    public static final String GET_REQUEST = "Получен запрос на получение данных для ID = {}.";
    public static final String GET_OWN_REQUESTS = "Получен запрос на получение собственных запросов для пользователя ID = {}.";
    public static final String SEARCH_ITEMS_REQUEST = "Запрос на поиск вещей по следующим словам: \"{}\".";
}