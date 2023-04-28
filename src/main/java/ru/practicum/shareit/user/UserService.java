package ru.practicum.shareit.user;

import ru.practicum.shareit.CustomJsonPatch;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto create(UserDto userDto);

    UserDto get(long userId);

    List<UserDto> getAll();

    UserDto update(long userId, CustomJsonPatch userDtoPatched);

    void delete(long userId);

    UserDto applyPatchToUser(CustomJsonPatch patch, UserDto oldUserDto);
}