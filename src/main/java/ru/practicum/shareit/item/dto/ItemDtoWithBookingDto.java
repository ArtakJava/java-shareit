package ru.practicum.shareit.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.shareit.booking.dto.BookingDtoWithBooker;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDtoWithBookingDto implements ItemDto {
    private long itemId;
    private BookingDtoWithBooker booking;
}