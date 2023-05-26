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
        UserDto result = UserMapper.mapToUserDto(repository.save(user));
        log.info(InfoMessage.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public UserDto get(long userId) {
        User user = repository.getReferenceById(userId);
        log.info(InfoMessage.SUCCESS_GET, userId);
        return UserMapper.mapToUserDto(user);
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = repository.findAll().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_GET_ALL);
        return users;
    }

    @Override
    public UserDto update(long userId, UserDto userDtoPatch) {
        User oldUser = repository.getReferenceById(userId);
        User result = repository.save(getUpdatedUser(oldUser, UserMapper.mapToUserEntity(userDtoPatch)));
        UserDto resultDto = UserMapper.mapToUserDto(result);
        log.info(InfoMessage.SUCCESS_UPDATE, resultDto);
        return resultDto;
    }

    @Override
    public void delete(long userId) {
        repository.delete(repository.getReferenceById(userId));
        log.info(InfoMessage.SUCCESS_DELETE, userId);
    }

    @Override
    public User getUpdatedUser(User user, User userPatch) {
        if (userPatch.getName() != null) {
            user.setName(userPatch.getName());
        }
        if (userPatch.getEmail() != null) {
            user.setEmail(userPatch.getEmail());
        }
        return user;
    }
}