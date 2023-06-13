package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
	private final UserClient userClient;

	@PostMapping
	public ResponseEntity<Object> createUser(@Valid @RequestBody UserDto userDto) {
		log.info(MessageHolder.GET_CREATE_REQUEST, userDto);
		return userClient.createUser(userDto);
	}

	@GetMapping("/{userId}")
	public ResponseEntity<Object> getUser(@PathVariable Long userId) {
		log.info(MessageHolder.GET_REQUEST, userId);
		return userClient.getUser(userId);
	}

	@GetMapping
	public ResponseEntity<Object> getUsers() {
		log.info(MessageHolder.GET_ALL_REQUEST);
		return userClient.getUsers();
	}

	@PatchMapping("/{userId}")
	public ResponseEntity<Object> update(@PathVariable long userId, @RequestBody UserDto userDto) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, userDto);
		return userClient.update(userId, userDto);
	}

	@DeleteMapping("/{userId}")
	public ResponseEntity<Object> delete(@PathVariable long userId) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, userId);
		return userClient.delete(userId);
	}
}