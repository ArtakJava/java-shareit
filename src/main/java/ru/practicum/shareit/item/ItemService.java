package ru.practicum.shareit.item;

import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.CustomJsonPatch;

import java.util.List;

public interface ItemService {

    ItemDto create(long userId, ItemDto itemDto);

    ItemDto get(long userId, long itemId);

    List<ItemDto> getAllByUser(long userId);

    ItemDto update(long userId, long itemId, CustomJsonPatch patch);

    void delete(long userId, long itemId);

    List<ItemDto> search(String text);

    ItemDto applyPatchToItem(CustomJsonPatch patch, ItemDto oldItemDto);
}