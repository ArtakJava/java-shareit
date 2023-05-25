package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDtoWithOutBooking itemDto);

    ItemDto get(long userId, long itemId);

    List<ItemDto> getAllByUser(long userId);

    ItemDto update(long userId, long itemId, ItemDtoWithOutBooking itemDtoPatch);

    void delete(long userId, long itemId);

    List<ItemDto> search(String text);

    Item getUpdatedItem(Item item, Item itemPatch);

    CommentDto createComment(long authorId, long itemId, CommentDto commentDto);
}