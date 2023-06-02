package ru.practicum.shareit.item;

import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface ItemService {

    ItemDtoWithBooking create(long userId, ItemDtoWithOutBooking itemDto);

    ItemDtoWithBooking get(long userId, long itemId);

    List<ItemDtoWithBooking> getAllByUser(long userId);

    ItemDtoWithBooking update(long userId, long itemId, ItemDtoWithOutBooking itemDtoPatch);

    void delete(long userId, long itemId);

    List<ItemDtoWithBooking> search(String text);

    Item getUpdatedItem(Item item, Item itemPatch);

    CommentDto createComment(long authorId, long itemId, CommentDto commentDto);

    User getUser(long userId);
}