package ru.practicum.shareit.item;

import org.springframework.stereotype.Component;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.ItemEntity;

@Component
public class ItemMapper {

    public ItemDto mapToItemDto(ItemEntity itemEntity) {
        return ItemDto.builder()
                .id(itemEntity.getId())
                .name(itemEntity.getName())
                .description(itemEntity.getDescription())
                .isAvailable(itemEntity.isAvailable())
                .ownerId(itemEntity.getOwnerId())
                .build();
    }

    public ItemEntity mapToItemEntity(ItemDto itemDto) {
        return ItemEntity.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .isAvailable(itemDto.isAvailable())
                .ownerId(itemDto.getOwnerId())
                .build();
    }
}