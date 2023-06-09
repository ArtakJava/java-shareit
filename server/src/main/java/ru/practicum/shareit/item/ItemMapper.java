package ru.practicum.shareit.item;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemDtoWithBooking;
import ru.practicum.shareit.item.dto.ItemDtoWithOutBooking;
import ru.practicum.shareit.item.model.Item;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ItemMapper {

    public static ItemDtoWithBooking mapToItemDto(Item item) {
        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .comments(new ArrayList<>())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }

    public static ItemDtoWithBooking mapToItemDtoWithBookingsAndComments(
            Item item,
            BookingDtoWithBooker lastBooking,
            BookingDtoWithBooker nextBooking,
            List<CommentDto> comments) {
        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    public static ItemDtoWithBooking mapToItemDtoWithBookingsAndComments(
            ItemDtoWithBooking item,
            BookingDtoWithBooker lastBooking,
            BookingDtoWithBooker nextBooking,
            List<CommentDto> comments) {
        return ItemDtoWithBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .lastBooking(lastBooking)
                .nextBooking(nextBooking)
                .comments(comments)
                .build();
    }

    public static ItemDtoWithOutBooking mapToItemDtoWithOutBooking(Item item) {
        return ItemDtoWithOutBooking.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest().getId())
                .build();
    }

    public static Item mapToItemEntity(ItemDtoWithOutBooking itemDto) {
        return Item.builder()
                .id(itemDto.getId())
                .name(itemDto.getName())
                .description(itemDto.getDescription())
                .available(itemDto.getAvailable())
                .build();
    }
}