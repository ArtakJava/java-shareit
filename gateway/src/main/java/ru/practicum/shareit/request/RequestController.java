package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.messageManager.MessageHolder;
import ru.practicum.shareit.request.dto.RequestDto;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@Controller
@RequestMapping(path = "/requests")
@RequiredArgsConstructor
@Slf4j
@Validated
public class RequestController {
	public static final String DEFAULT_SIZE_OF_PAGE = "10";
	private final RequestClient requestClient;

	@PostMapping
	public ResponseEntity<Object> createRequest(@RequestHeader("X-Sharer-User-Id") long userId,
												@Valid @RequestBody RequestDto requestDto) {
		log.info(String.format(MessageHolder.GET_CREATE_REQUEST), requestDto);
		return requestClient.createRequest(userId, requestDto);
	}

	@GetMapping("/{requestId}")
	public ResponseEntity<Object> getRequest(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long requestId) {
		log.info(String.format(MessageHolder.GET_REQUEST), requestId);
		return requestClient.getRequest(userId, requestId);
	}

	@GetMapping("/all")
	public ResponseEntity<Object> getRequests(@RequestHeader("X-Sharer-User-Id") long userId,
											  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
											  @Positive @RequestParam(defaultValue = DEFAULT_SIZE_OF_PAGE) Integer size) {
		log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
		return requestClient.getRequests(userId, from, size);
	}

	@GetMapping
	public ResponseEntity<Object> getOwnRequests(@RequestHeader("X-Sharer-User-Id") long userId) {
		log.info(String.format(MessageHolder.GET_OWN_REQUESTS), userId);
		return requestClient.getOwnRequests(userId);
	}
}