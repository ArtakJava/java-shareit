package ru.practicum.shareit.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jsonpatch.JsonPatchException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.CustomJsonPatch;
import ru.practicum.shareit.exception.NotValidDataForUpdateException;
import ru.practicum.shareit.messageManager.ErrorMessage;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.user.dao.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.UserEntity;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final UserMapper mapper;

    @Override
    public UserDto create(UserDto userDto) {
        UserEntity userEntity = mapper.mapToUserEntity(userDto);
        UserDto result = mapper.mapToUserDto(repository.create(userEntity));
        log.info(InfoMessage.SUCCESS_CREATE, result);
        return result;
    }

    @Override
    public UserDto get(long userId) {
        UserEntity userEntity = repository.get(userId);
        log.info(InfoMessage.SUCCESS_GET, userId);
        return mapper.mapToUserDto(userEntity);
    }

    @Override
    public List<UserDto> getAll() {
        List<UserDto> users = repository.getAll().stream()
                .map(mapper::mapToUserDto)
                .collect(Collectors.toList());
        log.info(InfoMessage.SUCCESS_GET_ALL);
        return users;
    }

    @Override
    public UserDto update(long userId, CustomJsonPatch patch) {
        UserDto oldUserDto = mapper.mapToUserDto(repository.get(userId));
        UserDto userDtoPatched = applyPatchToUser(patch, oldUserDto);
        UserEntity result = repository.update(
                mapper.mapToUserEntity(oldUserDto), mapper.mapToUserEntity(userDtoPatched));
        log.info(InfoMessage.SUCCESS_UPDATE, userDtoPatched);
        return mapper.mapToUserDto(result);
    }

    @Override
    public void delete(long userId) {
        repository.delete(userId);
        log.info(InfoMessage.SUCCESS_DELETE, userId);
    }

    @Override
    public UserDto applyPatchToUser(CustomJsonPatch patch, UserDto oldUserDto) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode patched = patch.apply(objectMapper.convertValue(oldUserDto, JsonNode.class));
            return objectMapper.treeToValue(patched, UserDto.class);
        } catch (JsonPatchException | IOException e) {
            throw new NotValidDataForUpdateException(
                    String.format(ErrorMessage.DATA_NOT_VALID_FOR_UPDATE, patch, oldUserDto));
        }
    }
}