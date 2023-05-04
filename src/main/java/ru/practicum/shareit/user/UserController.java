package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.messageManager.InfoMessage;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService service;

    @PostMapping
    public UserDto create(@Valid @RequestBody UserDto userDto) {
        log.info(InfoMessage.GET_CREATE_REQUEST, userDto);
        return service.create(userDto);
    }

    @GetMapping("/{userId}")
    public UserDto get(@PathVariable long userId) {
        log.info(InfoMessage.GET_REQUEST, userId);
        return service.get(userId);
    }

    @GetMapping
    public List<UserDto> getAll() {
        log.info(InfoMessage.GET_ALL_REQUEST);
        return service.getAll();
    }

    @PatchMapping("/{userId}")
    public UserDto update(@PathVariable long userId, @RequestBody UserDto userDto) throws NoSuchFieldException, IllegalAccessException {
        log.info(InfoMessage.GET_UPDATE_REQUEST, userDto);
        return service.update(userId, userDto);
    }

    @DeleteMapping("/{userId}")
    public void delete(@PathVariable long userId) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, userId);
        service.delete(userId);
    }
}