package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;
import ru.practicum.shareit.comment.dto.CommentDto;
import ru.practicum.shareit.messageManager.ErrorMessage;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithBooking {
    private long id;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_NAME)
    private String name;
    @NotBlank(message = ErrorMessage.ITEM_EMPTY_DESCRIPTION)
    private String description;
    @NotNull
    private Boolean available;
    private BookingDtoWithBooker lastBooking;
    private BookingDtoWithBooker nextBooking;
    private Long requestId;
    private List<CommentDto> comments = new ArrayList<>();
}