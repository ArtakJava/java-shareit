package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.messageManager.InfoMessage;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
@Slf4j
public class ItemController {
    private final ItemService service;

    @PostMapping
    public ItemDto create(@RequestHeader("X-Sharer-User-Id") long userId, @Valid @RequestBody ItemDtoWithOutBooking itemDto) {
        log.info(InfoMessage.GET_CREATE_REQUEST, itemDto);
        return service.create(userId, itemDto);
    }

    @GetMapping("/{itemId}")
    public ItemDto get(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info(InfoMessage.GET_REQUEST, itemId);
        return service.get(userId, itemId);
    }

    @GetMapping
    public List<ItemDto> get(@RequestHeader("X-Sharer-User-Id") long userId) {
        log.info(InfoMessage.GET_ALL_BY_USER_REQUEST, userId);
        return service.getAllByUser(userId);
    }

    @PatchMapping("/{itemId}")
    public ItemDto update(@RequestHeader("X-Sharer-User-Id") long userId,
                                        @PathVariable long itemId,
                                        @RequestBody ItemDtoWithOutBooking itemDtoPatch) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, itemDtoPatch);
        return service.update(userId, itemId, itemDtoPatch);
    }

    @DeleteMapping("/{itemId}")
    public void delete(@RequestHeader("X-Sharer-User-Id") long userId, @PathVariable long itemId) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, itemId);
        service.delete(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> search(@RequestParam String text) {
        log.info(InfoMessage.SEARCH_ITEMS_REQUEST, text);
        return service.search(text);
    }

    @PostMapping("/{itemId}/comment")
    public CommentDto createComment(@RequestHeader("X-Sharer-User-Id") long userId,
                                    @PathVariable long itemId,
                                    @Valid @RequestBody CommentDto commentDto) {
        log.info(InfoMessage.GET_UPDATE_REQUEST, itemId);
        return service.createComment(userId, itemId, commentDto);
    }
}