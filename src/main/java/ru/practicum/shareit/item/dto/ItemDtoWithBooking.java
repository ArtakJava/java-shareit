package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.comment.dto.CommentDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithBooking {
    private long id;
    private String name;
    private String description;
    private Boolean available;
    private BookingDtoWithBooker lastBooking;
    private BookingDtoWithBooker nextBooking;
    private Long requestId;
    private List<CommentDto> comments = new ArrayList<>();
}