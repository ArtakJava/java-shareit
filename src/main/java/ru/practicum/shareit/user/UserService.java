package ru.practicum.shareit.user;

import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto get(long userId);

    List<UserDto> getAll();

    UserDto update(long userId, UserDto userDto);

    void delete(long userId);

    User getUpdatedUser(User user, User userPatch);
}