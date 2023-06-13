package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.messageManager.MessageHolder;

import javax.validation.Valid;

@Controller
@RequestMapping(path = "/items")
@RequiredArgsConstructor
@Slf4j
@Validated
public class ItemController {
	private final ItemClient itemClient;

	@PostMapping
	public ResponseEntity<Object> createItem(@RequestHeader("X-Sharer-User-Id") long userId,
											 @Valid @RequestBody ItemDtoWithOutBooking itemDto) {
		log.info(MessageHolder.GET_CREATE_REQUEST, itemDto);
		return itemClient.createItem(userId, itemDto);
	}

	@GetMapping("/{itemId}")
	public ResponseEntity<Object> getItem(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable Long itemId) {
		log.info(MessageHolder.GET_REQUEST, itemId);
		return itemClient.getItem(userId, itemId);
	}

	@GetMapping
	public ResponseEntity<Object> getAllByUser(@RequestHeader("X-Sharer-User-Id") long userId) {
		log.info(MessageHolder.GET_ALL_BY_USER_REQUEST, userId);
		return itemClient.getAllByUser(userId);
	}

	@PatchMapping("/{itemId}")
	public ResponseEntity<Object> update(@RequestHeader("X-Sharer-User-Id") long userId,
											 @PathVariable Long itemId,
											 @RequestBody ItemDtoWithOutBooking itemDtoPatch) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, itemDtoPatch);
		return itemClient.update(userId, itemId, itemDtoPatch);
	}

	@DeleteMapping("/{itemId}")
	public ResponseEntity<Object> delete(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, itemId);
		return itemClient.delete(userId, itemId);
	}

	@GetMapping("/search")
	public ResponseEntity<Object> search(@RequestParam String text) {
		log.info(MessageHolder.SEARCH_ITEMS_REQUEST, text);
		return itemClient.search(text);
	}

	@PostMapping("/{itemId}/comment")
	public ResponseEntity<Object> createComment(@RequestHeader("X-Sharer-User-Id") long userId,
												@PathVariable long itemId,
												@Valid @RequestBody CommentDto commentDto) {
		log.info(MessageHolder.GET_UPDATE_REQUEST, itemId);
		return itemClient.createComment(userId, itemId, commentDto);
	}
}