package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public UserDto create(UserDto userDto) {
        User user = UserMapper.mapToUserEntity(userDto);
        UserDto result = UserMapper.mapToUserDto(repository.create(user));
        log.info(InfoMessage.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public UserDto get(long userId) {
        User user = repository.get(userId);
        log.info(InfoMessage.SUCCESS_GET, userId);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = repository.getAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_GET_ALL);
        return users;
    }

    @Override
    public UserDto update(long userId, UserDto userDtoPatch) throws NoSuchFieldException, IllegalAccessException {
        User oldUser = repository.get(userId);
        User result = repository.update(oldUser, UserMapper.mapToUserEntity(userDtoPatch), User.class);
        UserDto resultDto = UserMapper.mapToUserDto(result);
        log.info(InfoMessage.SUCCESS_UPDATE, resultDto);
        return resultDto;
    }

    @Override
    public void delete(long userId) {
        repository.delete(userId);
        log.info(InfoMessage.SUCCESS_DELETE, userId);
    }
}